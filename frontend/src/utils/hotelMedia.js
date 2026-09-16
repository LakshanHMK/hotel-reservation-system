const fallbackImage = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1600 900"%3E%3Crect width="1600" height="900" fill="%23062844"/%3E%3Cpath d="M0 720L420 350l290 260 220-180 670 470H0z" fill="%232e637b"/%3E%3Ctext x="800" y="190" text-anchor="middle" fill="%23efbd43" font-size="72" font-family="Georgia"%3ELankaStay%3C/text%3E%3C/svg%3E'
const apiBaseUrl = (import.meta.env?.VITE_API_URL || 'http://localhost:8080').replace(/\/$/, '')
// Older seeded room records omitted this directory. Keep the API values
// compatible without copying the source images into public/.
const legacyImagePaths = {
  'deluxe-ocean-view-room.png': 'home/ocean - bay/deluxe-ocean-view-room.png',
  'family-ocean-room.png': 'home/ocean - bay/family-ocean-room.png',
  'premium-ocean-suite.png': 'home/ocean - bay/premium-ocean-suite.png',
}
const bundledImageUrls = typeof import.meta.glob === 'function'
  ? import.meta.glob('../assets/images/**/*.{png,jpg,jpeg,webp,gif,avif,svg}', {
      eager: true,
      import: 'default',
      query: '?url',
    })
  : {}

export function resolveCatalogImageUrl(value) {
  if (typeof value !== 'string') return value
  if (value.startsWith('/uploads/')) return `${apiBaseUrl}${value}`
  if (value.startsWith('/assets/images/')) {
    const relativePath = value.slice('/assets/images/'.length)
    let decodedPath = relativePath
    try { decodedPath = decodeURIComponent(relativePath) } catch { /* keep the supplied path */ }
    const sourcePath = legacyImagePaths[decodedPath] || decodedPath
    return bundledImageUrls[`../assets/images/${sourcePath}`] || value
  }
  return value
}

export const normalizeId = (value) => String(value ?? '')

export function getHotelMainImage(hotel) {
  const value = hotel?.mainImage || hotel?.image
  return typeof value === 'object' ? resolveCatalogImageUrl(value?.src) || fallbackImage : resolveCatalogImageUrl(value) || fallbackImage
}

export function getHotelGallery(hotel) {
  const main = getHotelMainImage(hotel)
  return [...new Set((hotel?.gallery || []).map((item) => resolveCatalogImageUrl(typeof item === 'object' ? item?.src : item)).filter((src) => src && src !== main))]
}

export const getHotelGalleryImages = getHotelGallery

export function getHotelDisplayImages(hotel) {
  return [getHotelMainImage(hotel), ...getHotelGallery(hotel)]
}

export function applyImageFallback(event) {
  if (event.currentTarget.src === fallbackImage) return
  event.currentTarget.src = fallbackImage
}

export function getHotelMedia(hotel) {
  return [{ id: 'main', src: getHotelMainImage(hotel), label: 'Main Photo' }, ...getHotelGallery(hotel).map((src, index) => ({ id: `gallery:${src}`, src, label: `Gallery Photo ${index + 1}` }))]
}

export function resolveHotelMedia(hotel, mediaReference) {
  const media = getHotelMedia(hotel)
  return media.find((item) => item.id === mediaReference)?.src || getHotelMainImage(hotel)
}

export function isPublicHotel(hotel) {
  return Boolean(hotel && (hotel.publicationStatus || hotel.status) === 'ACTIVE' && hotel.setupStatus === 'COMPLETE')
}
