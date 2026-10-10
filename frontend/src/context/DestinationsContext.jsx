import { useCallback, useEffect, useMemo, useState } from 'react'
import initialDestinations from '../data/destinations.js'
import { destinationApi, resolveDestinationMediaUrl } from '../services/destinationApi.js'
import { isStaticPreview } from '../config/staticPreview.js'
import { DESTINATION_STATUS, normalizeDestination } from '../utils/destinationDomain.js'
import DestinationsContext from './destinationsContext.js'

function normalizeApiDestination(destination) {
  return normalizeDestination({ ...destination, mainImage: resolveDestinationMediaUrl(destination.mainImage), lastUpdatedAt: destination.updatedAt })
}

export function DestinationsProvider({ children }) {
  const [destinations, setDestinations] = useState(() => initialDestinations.map(normalizeDestination))
  const [createDraft, setCreateDraft] = useState(null)
  const [editDrafts, setEditDrafts] = useState({})
  const [destinationsLoading, setDestinationsLoading] = useState(!isStaticPreview)
  const [destinationError, setDestinationError] = useState('')

  useEffect(() => {
    if (isStaticPreview) return undefined
    let active = true
    destinationApi.list().then((items) => {
      if (active) setDestinations((items || []).map(normalizeApiDestination))
    }).catch((error) => {
      if (active) setDestinationError(error.message || 'Unable to load Destinations from the server.')
    }).finally(() => { if (active) setDestinationsLoading(false) })
    return () => { active = false }
  }, [])

  const getDestinationById = useCallback((id) => destinations.find((destination) => String(destination.id) === String(id)), [destinations])
  const getDestinationBySlug = useCallback((slug) => destinations.find((destination) => destination.slug === slug), [destinations])

  const saveDestinationDraft = useCallback(async (id, draft, metadata) => {
    setDestinationError('')
    try {
      const payload = { ...draft, ...metadata }
      const response = id ? await destinationApi.update(id, payload) : await destinationApi.create(payload)
      const saved = normalizeApiDestination(response)
      setDestinations((current) => id
        ? current.map((item) => String(item.id) === String(id) ? saved : item)
        : [...current.filter((item) => String(item.id) !== String(saved.id)), saved])
      if (id) setEditDrafts((current) => { const next = { ...current }; delete next[String(id)]; return next })
      else setCreateDraft(null)
      return saved
    } catch (error) {
      setDestinationError(error.message || 'Unable to save the Destination.')
      throw error
    }
  }, [])

  const updateDestination = useCallback((id, updates) => saveDestinationDraft(id, { ...getDestinationById(id), ...updates }, {}), [getDestinationById, saveDestinationDraft])

  const submitDestinationForReview = useCallback(async (id, draft) => saveDestinationDraft(id, {
    ...draft, status: DESTINATION_STATUS.READY_FOR_REVIEW, lastSavedStep: 6,
    lastCompletedStep: 6, lastUpdatedSection: 'Review & Submit',
  }, {}), [saveDestinationDraft])

  const setDestinationStatus = useCallback(async (id, status) => {
    setDestinationError('')
    try {
      const saved = normalizeApiDestination(await destinationApi.updateStatus(id, status))
      setDestinations((current) => current.map((item) => String(item.id) === String(id) ? saved : item))
      return saved
    } catch (error) {
      setDestinationError(error.message || 'Unable to change Destination status.')
      throw error
    }
  }, [])

  const updateCreateDraft = useCallback((draft) => setCreateDraft((current) => typeof draft === 'function' ? draft(current) : draft), [])
  const clearCreateDraft = useCallback(() => setCreateDraft(null), [])
  const updateEditDraft = useCallback((id, draft) => setEditDrafts((current) => ({ ...current, [String(id)]: typeof draft === 'function' ? draft(current[String(id)]) : draft })), [])
  const clearEditDraft = useCallback((id) => setEditDrafts((current) => { const next = { ...current }; delete next[String(id)]; return next }), [])
  const activeDestinations = useMemo(() => destinations.filter((destination) => destination.status === DESTINATION_STATUS.ACTIVE), [destinations])

  const value = useMemo(() => ({
    destinations, activeDestinations, destinationsLoading, destinationError, getDestinationById, getDestinationBySlug,
    updateDestination, saveDestinationDraft, submitDestinationForReview,
    approveDestination: (id) => setDestinationStatus(id, DESTINATION_STATUS.ACTIVE),
    reactivateDestination: (id) => setDestinationStatus(id, DESTINATION_STATUS.ACTIVE),
    deactivateDestination: (id) => setDestinationStatus(id, DESTINATION_STATUS.INACTIVE), setDestinationStatus,
    createDraft, updateCreateDraft, clearCreateDraft, editDrafts, updateEditDraft, clearEditDraft,
  }), [destinations, activeDestinations, destinationsLoading, destinationError, getDestinationById, getDestinationBySlug,
    updateDestination, saveDestinationDraft, submitDestinationForReview, setDestinationStatus, createDraft,
    updateCreateDraft, clearCreateDraft, editDrafts, updateEditDraft, clearEditDraft])

  return <DestinationsContext.Provider value={value}>{children}</DestinationsContext.Provider>
}
