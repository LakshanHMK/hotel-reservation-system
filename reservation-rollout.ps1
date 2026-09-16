param([switch]$DryRun, [string]$RunDate)

$ErrorActionPreference = 'Stop'
$repo = Join-Path $PSScriptRoot '.target-repo'
$logPath = Join-Path $PSScriptRoot 'reservation-rollout.log'
if ($RunDate -and -not $DryRun) { throw 'RunDate is only allowed with DryRun.' }
$today = if ($RunDate) { $RunDate } else { (Get-Date).ToString('yyyy-MM-dd') }
$baseCommit = '34686b539399044e5b6274b1642b735441f3a275'

$batches = @{
    '2026-09-15' = @(
        @{ Hash = '8de84ca1f0a25576aae4d106845e4b5687bf7d52'; Message = 'Define availability and reservation response DTOs' },
        @{ Hash = '37d65cda3680e19ff55dc1c585d30846cdf5ad40'; Message = 'Require an active customer session for bookings' },
        @{ Hash = 'cf101ca7c88bc4172f9ca152ccf1c43d072045c9'; Message = 'Implement customer booking and pricing service' }
    )
    '2026-09-16' = @(
        @{ Hash = '5cd54c610f4fe7f4dbaa181d91b26b4146df4ce1'; Message = 'Expose customer reservation endpoints' },
        @{ Hash = '5871b20c3a7d51b8e36ecf2ae0444863cf987d46'; Message = 'Implement staff reservation management' },
        @{ Hash = '94f0eb8d684d206454bc18fa0758c3a7893d284b'; Message = 'Expose staff reservation endpoints and audit events' }
    )
    '2026-09-17' = @(
        @{ Hash = '952ae42332aae0907e79d5311f820c4a22637abb'; Message = 'Test reservation lifecycle scenarios' },
        @{ Hash = '25b36256d645b7281156496fd7b41e1f020a342f'; Message = 'Test reservation pricing scenarios' },
        @{ Hash = '3d3685230a9fd155df62286d2939e5a850e14a0f'; Message = 'Support physical room assignment for reservations' }
    )
}

function Log-Result([string]$message) {
    $line = "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss zzz') $message"
    Add-Content -LiteralPath $logPath -Value $line
    Write-Output $line
}

if (-not $batches.ContainsKey($today)) {
    Set-Location -LiteralPath $repo
    $env:GIT_TERMINAL_PROMPT = '0'
    $remoteProbe = & git -c credential.interactive=never ls-remote --heads origin reservation-backend
    if ($LASTEXITCODE -ne 0 -or -not $remoteProbe) {
        Log-Result "No commits scheduled for $today; remote access check failed."
        exit 1
    }
    Log-Result "No commits scheduled for $today; remote access verified."
    exit 0
}

$dayIndex = [int]([datetime]::ParseExact($today, 'yyyy-MM-dd', $null).Day - 14)
$expectedCount = 3 * $dayIndex
$batch = $batches[$today]
if ($DryRun) {
    Log-Result "Dry run for ${today}: expected prior count $expectedCount; next commits: $($batch.Message -join '; ')"
    exit 0
}

try {
    Set-Location -LiteralPath $repo
    $env:GIT_TERMINAL_PROMPT = '0'
    if ((& git branch --show-current).Trim() -ne 'reservation-backend') { throw 'Wrong Git branch.' }
    if (@(& git status --porcelain).Count -ne 0) { throw 'Checkout has uncommitted files.' }
    $priorCount = [int]((& git rev-list --count "$baseCommit..HEAD").Trim())
    $oldHead = (& git rev-parse HEAD).Trim()
    $remoteLine = & git ls-remote --heads origin reservation-backend
    if ($LASTEXITCODE -ne 0 -or -not $remoteLine) { throw 'Could not read the remote branch.' }
    $remoteHead = ($remoteLine -split '\s+')[0]
    if ($remoteHead -ne $oldHead) { throw 'Remote branch moved; leaving it unchanged.' }

    if ($priorCount -eq $expectedCount + 3) {
        $actualSubjects = @(& git log -3 --format=%s)
        $expectedSubjects = @($batch | ForEach-Object { $_.Message })
        [array]::Reverse($expectedSubjects)
        if (($actualSubjects -join '|') -ne ($expectedSubjects -join '|')) {
            throw 'Three commits are present, but they are not the scheduled batch.'
        }
        Log-Result "The three commits for $today were already pushed; nothing to do."
        exit 0
    }
    if ($priorCount -ne $expectedCount) { throw "Expected $expectedCount prior commits, found $priorCount." }

    foreach ($item in $batch) {
        & git cherry-pick --no-commit $item.Hash
        if ($LASTEXITCODE -ne 0) { throw "Could not apply $($item.Hash)." }
        & git -c user.name=LakshanHMK -c user.email=kavinadalakshan29@gmail.com commit -m $item.Message
        if ($LASTEXITCODE -ne 0) { throw "Could not commit $($item.Message)." }
    }

    $nonJava = @(& git diff --name-only $oldHead HEAD | Where-Object { $_ -notlike '*.java' })
    if ($nonJava.Count -ne 0) { throw "Non-Java changes detected: $($nonJava -join ', ')." }
    & git -c credential.interactive=never push origin reservation-backend
    if ($LASTEXITCODE -ne 0) { throw 'Push failed; the new commits remain local for review.' }
    Log-Result "Pushed three reservation commits for $today."
} catch {
    Log-Result "Stopped without forcing remote changes: $($_.Exception.Message)"
    exit 1
}
