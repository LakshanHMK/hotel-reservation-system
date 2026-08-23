package com.lankastay.backend.dto.auth;

public record CsrfTokenResponse(String token, String headerName) {
    // Security: framework debug logging may call toString(); never render the token.
    @Override public String toString() { return "CsrfTokenResponse[token=<redacted>, headerName=" + headerName + "]"; }
}
