export const normalizeEntityId = (value) => String(value ?? '')
export const deduplicateCollectionIds = (ids) => [...new Set((ids || []).map(normalizeEntityId).filter(Boolean))]
export const getActiveCollections = (collections) => [...(collections || [])].filter((item) => item.status === 'ACTIVE').sort((a, b) => a.displayOrder - b.displayOrder)
export const getHomeCollections = (collections) => getActiveCollections(collections).filter((item) => item.showOnHome)
export const getCollectionsForHotel = (hotel, collections) => deduplicateCollectionIds(hotel?.collectionIds).map((id) => collections.find((item) => normalizeEntityId(item.id) === id)).filter(Boolean)
export const hotelHasCollection = (hotel, collectionId) => deduplicateCollectionIds(hotel?.collectionIds).includes(normalizeEntityId(collectionId))
export const getHotelsForCollection = (hotels, collectionId) => (hotels || []).filter((hotel) => hotelHasCollection(hotel, collectionId))
export const getCollectionHotelCount = (hotels, collectionId) => getHotelsForCollection(hotels, collectionId).length
