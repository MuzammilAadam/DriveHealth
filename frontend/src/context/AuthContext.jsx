import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { getAuthUrl, getUserProfile } from '../services/driveHealthApi'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [activeAccountId, setActiveAccountId] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const applyProfile = useCallback((profile) => {
    const normalizedProfile = {
      ...profile,
      googleAccounts: profile.googleAccounts || profile.accounts || [],
    }

    setUser(normalizedProfile)
    localStorage.setItem('drivehealth_user_id', profile.id)

    const accounts = normalizedProfile.googleAccounts
    if (accounts.length === 0) {
      setActiveAccountId(null)
      localStorage.removeItem('drivehealth_active_account_id')
      return profile
    }

    const storedAccountId = localStorage.getItem('drivehealth_active_account_id')
    const matched = accounts.find((account) => String(account.id) === storedAccountId)
    const chosenId = matched ? matched.id : accounts[0].id
    setActiveAccountId(chosenId)
    localStorage.setItem('drivehealth_active_account_id', chosenId)
    return normalizedProfile
  }, [])

  useEffect(() => {
    const initAuth = async () => {
      setLoading(true)
      const storedUserId = localStorage.getItem('drivehealth_user_id')

      if (!storedUserId) {
        setLoading(false)
        return
      }

      try {
        const profile = await getUserProfile(storedUserId)
        applyProfile(profile)
      } catch {
        localStorage.removeItem('drivehealth_user_id')
        localStorage.removeItem('drivehealth_active_account_id')
        setUser(null)
        setActiveAccountId(null)
      } finally {
        setLoading(false)
      }
    }

    initAuth()
  }, [applyProfile])

  const loginWithOAuth = useCallback(async () => {
    setError(null)
    try {
      const { authUrl, authorizationUrl } = await getAuthUrl()
      const googleAuthUrl = authUrl || authorizationUrl
      if (!googleAuthUrl) {
        throw new Error('Google OAuth URL could not be generated')
      }
      window.location.href = googleAuthUrl
    } catch (e) {
      setError(e.message)
      throw e
    }
  }, [])

  const handleOAuthCallback = useCallback(
    async (userId) => {
      setError(null)
      try {
        const profile = await getUserProfile(userId)
        return applyProfile(profile)
      } catch (e) {
        setError(e.message)
        throw e
      }
    },
    [applyProfile]
  )

  const switchAccount = useCallback((accountId) => {
    setActiveAccountId(accountId)
    localStorage.setItem('drivehealth_active_account_id', accountId)
  }, [])

  const logout = useCallback(() => {
    setUser(null)
    setActiveAccountId(null)
    localStorage.removeItem('drivehealth_user_id')
    localStorage.removeItem('drivehealth_active_account_id')
  }, [])

  const refreshUser = useCallback(async () => {
    if (!user?.id) return
    try {
      const profile = await getUserProfile(user.id)
      applyProfile(profile)
    } catch {
      logout()
    }
  }, [applyProfile, logout, user?.id])

  const activeAccount = useMemo(() => {
    const accounts = user?.googleAccounts || []
    return accounts.find((account) => account.id === activeAccountId) || accounts[0] || null
  }, [activeAccountId, user?.googleAccounts])

  const value = {
    user,
    activeAccount,
    activeAccountId: activeAccount?.id || null,
    isAuthenticated: Boolean(user && activeAccount),
    loading,
    error,
    loginWithOAuth,
    handleOAuthCallback,
    switchAccount,
    logout,
    refreshUser,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
