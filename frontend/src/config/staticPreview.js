export const isStaticPreview = typeof __STATIC_PREVIEW__ !== 'undefined'
  ? __STATIC_PREVIEW__
  : import.meta.env?.VITE_STATIC_PREVIEW === 'true'

export const STATIC_PREVIEW_UNAVAILABLE_MESSAGE =
  'This action requires the LankaStay backend and is unavailable in the frontend preview.'
