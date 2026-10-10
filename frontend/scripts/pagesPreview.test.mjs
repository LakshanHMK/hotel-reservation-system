import assert from 'node:assert/strict'
import { readFile, readdir } from 'node:fs/promises'

const root = new URL('../', import.meta.url)
const indexHtml = await readFile(new URL('dist/index.html', root), 'utf8')
const fallbackHtml = await readFile(new URL('dist/404.html', root), 'utf8')

assert.equal(fallbackHtml, indexHtml, 'GitHub Pages fallback must load the same app shell as the homepage')
assert.match(indexHtml, /\/hotel-reservation-system\/assets\//, 'production assets must use the repository base path')
assert.doesNotMatch(indexHtml, /localhost:8080/i, 'the generated app shell must not point at a localhost backend')
const assetFiles = await readdir(new URL('dist/assets/', root))
const scripts = await Promise.all(assetFiles.filter((file) => file.endsWith('.js')).map((file) => readFile(new URL(`dist/assets/${file}`, root), 'utf8')))
const bundle = scripts.join('\n')
assert.doesNotMatch(bundle, /localhost:8080/i, 'static preview JavaScript must not contain a localhost backend URL')
assert.match(bundle, /unavailable in the frontend preview/i, 'backend-dependent actions must be explicitly blocked in preview mode')
console.log('PASS GitHub Pages base path, SPA fallback, and no localhost script references')
