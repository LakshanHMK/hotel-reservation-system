import assert from 'node:assert/strict'
import { destinationApi } from '../src/services/destinationApi.js'

const requests = []
globalThis.fetch = async (url, options = {}) => {
  requests.push({ url: String(url), options })
  if (String(url).endsWith('/api/v1/auth/csrf')) {
    return { ok: true, json: async () => ({ token: 'test-csrf', headerName: 'X-XSRF-TOKEN' }) }
  }
  return {
    ok: true,
    status: 200,
    text: async () => JSON.stringify(String(url).endsWith('/api/media/upload')
      ? { url: '/uploads/destinations/dest_test.png', filename: 'dest_test.png', size: 12, mimeType: 'image/png' }
      : { id: 42, name: 'Test Destination', mainImage: '/uploads/destinations/dest_test.png', status: 'DRAFT' }),
  }
}

await destinationApi.create({ name: 'Test Destination', shortDescription: 'Short', region: 'Southern Province', district: 'Galle', latitude: 6.03, longitude: 80.21, status: 'DRAFT', themeKeys: ['COAST'], highlights: [], attractions: [] })
const createRequest = requests.find(({ url }) => url.endsWith('/api/management/destinations'))
assert.ok(createRequest, 'creating a Destination must call the backend management API')
assert.equal(createRequest.options.method, 'POST')
assert.equal(createRequest.options.credentials, 'include', 'management requests must carry the authenticated session')
assert.equal(createRequest.options.headers.get('X-XSRF-TOKEN'), 'test-csrf', 'mutations must carry CSRF protection')
assert.equal(JSON.parse(createRequest.options.body).name, 'Test Destination')

await destinationApi.update(42, { name: 'Updated Destination', shortDescription: 'Updated', region: 'Southern Province', district: 'Galle', latitude: 6.03, longitude: 80.21, status: 'DRAFT', themeKeys: [], highlights: [], attractions: [] })
const updateRequest = requests.find(({ url }) => url.endsWith('/api/management/destinations/42'))
assert.ok(updateRequest, 'updating a Destination must call the backend management API')
assert.equal(updateRequest.options.method, 'PUT')
assert.equal(JSON.parse(updateRequest.options.body).name, 'Updated Destination')

const file = new Blob(['image-bytes'], { type: 'image/png' })
await destinationApi.uploadImage(file)
const uploadRequest = requests.find(({ url }) => url.endsWith('/api/media/upload'))
assert.ok(uploadRequest, 'image selection must upload to the backend media API')
assert.equal(uploadRequest.options.method, 'POST')
assert.equal(uploadRequest.options.credentials, 'include')
assert.equal(uploadRequest.options.headers.get('X-XSRF-TOKEN'), 'test-csrf')
assert.ok(uploadRequest.options.body instanceof FormData, 'image request must use browser FormData')
assert.equal(uploadRequest.options.body.get('file').type, 'image/png', 'multipart field name must match the backend file parameter')
assert.equal(uploadRequest.options.headers.has('Content-Type'), false, 'the browser must set the multipart boundary')

console.log('PASS Destination create/update API requests and authenticated multipart image upload')
