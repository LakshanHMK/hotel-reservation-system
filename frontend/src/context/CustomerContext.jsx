import { useCallback, useEffect, useMemo, useState } from 'react'
import initialCustomer from '../data/customer.js'
import CustomerContext from './customerContext.js'
import { authApi } from '../services/authApi.js'

export function CustomerProvider({ children }) {
  const [customer, setCustomer] = useState({ ...initialCustomer, isLoggedIn: false })
  const [loading, setLoading] = useState(true)

  const refreshCustomer = useCallback(async () => {
    try {
      const data = await authApi.customerMe()
      if (data && data.email) {
        setCustomer({
          ...initialCustomer,
          ...data,
          memberSince: data.createdAt ? data.createdAt.slice(0, 10) : '2026-01-01',
          isLoggedIn: true,
        })
        return data
      }
    } catch {
      setCustomer((prev) => ({ ...prev, isLoggedIn: false }))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    refreshCustomer()
  }, [refreshCustomer])

  const loginCustomer = useCallback(async (credentials) => {
    const data = await authApi.customerLogin(credentials)
    const activeCustomer = {
      ...initialCustomer,
      ...data,
      memberSince: data.createdAt ? data.createdAt.slice(0, 10) : '2026-01-01',
      isLoggedIn: true,
    }
    setCustomer(activeCustomer)
    return activeCustomer
  }, [])

  const registerCustomer = useCallback(async (registrationData) => {
    const data = await authApi.customerRegister(registrationData)
    return data
  }, [])

  const logoutCustomer = useCallback(async () => {
    try {
      await authApi.customerLogout()
    } finally {
      setCustomer({ ...initialCustomer, isLoggedIn: false })
    }
  }, [])

  const updateCustomer = useCallback((updates) => {
    setCustomer((current) => ({ ...current, ...updates }))
    return { success: true }
  }, [])

  const value = useMemo(
    () => ({
      customer,
      loading,
      loginCustomer,
      registerCustomer,
      logoutCustomer,
      updateCustomer,
      refreshCustomer,
    }),
    [customer, loading, loginCustomer, registerCustomer, logoutCustomer, updateCustomer, refreshCustomer]
  )

  return <CustomerContext.Provider value={value}>{children}</CustomerContext.Provider>
}
