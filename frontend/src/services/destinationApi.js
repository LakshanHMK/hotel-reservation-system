import { apiRequest } from './authApi.js'

const apiBaseUrl = (import.meta.env?.VITE_API_URL || 'http://localhost:8080').replace(/\/$/, '')

export function resolveDestinationMediaUrl(value) {
  return typeof value === 'string' && value.startsWith('/uploads/') ? `${apiBaseUrl}${value}` : value
}

function storedMediaUrl(value) {
  return typeof value === 'string' && value.startsWith(`${apiBaseUrl}/uploads/`) ? value.slice(apiBaseUrl.length) : value
}

function imagePosition(point) {
  return `${Number(point?.x ?? 50)}% ${Number(point?.y ?? 50)}%`
}

function attractionPayload(item, displayOrder) {
  return {
    name: item.name?.trim(), type: item.type, shortDescription: item.shortDescription || '',
    latitude: item.latitude, longitude: item.longitude, image: item.image || '',
    estimatedTravelTime: item.estimatedTravelTime || '', source: item.source || '',
    sourceId: item.sourceId || '', status: item.status || 'ACTIVE', displayOrder,
  }
}

export function toDestinationPayload(destination) {
  return {
    name: destination.name?.trim(), slug: destination.slug || undefined,
    shortDescription: destination.shortDescription?.trim(), fullDescription: destination.fullDescription?.trim() || '',
    category: destination.category || '', region: destination.region?.trim(), district: destination.district?.trim(),
    latitude: destination.latitude, longitude: destination.longitude, status: destination.status || 'DRAFT',
    mainImage: storedMediaUrl(destination.mainImage) || '', cardImagePosition: imagePosition(destination.imageFocalPoint),
    heroImagePosition: imagePosition(destination.imageFocalPoint), heroFitMode: destination.heroFitMode || 'cover',
    lastSavedStep: destination.lastSavedStep, lastCompletedStep: destination.lastCompletedStep,
    lastUpdatedSection: destination.lastUpdatedSection || '', version: destination.version,
    themeKeys: [...new Set(destination.themeKeys || [])], highlights: (destination.highlights || []).filter(Boolean),
    attractions: (destination.attractions || []).map(attractionPayload),
  }
}

export const destinationApi = {
  list: () => apiRequest('/api/management/destinations'),
  create: (destination) => apiRequest('/api/management/destinations', { method: 'POST', body: JSON.stringify(toDestinationPayload(destination)) }),
  update: (id, destination) => apiRequest(`/api/management/destinations/${id}`, { method: 'PUT', body: JSON.stringify(toDestinationPayload(destination)) }),
  updateStatus: (id, status) => apiRequest(`/api/management/destinations/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status }) }),
  uploadImage: (file) => {
    const body = new FormData()
    body.append('file', file)
    return apiRequest('/api/media/upload', { method: 'POST', body })
  },
}
