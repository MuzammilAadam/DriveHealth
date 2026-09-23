import { useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { AlertCircle, ShieldCheck } from 'lucide-react'
import { useAuth } from '../context/AuthContext'

export default function Login() {
  const [searchParams] = useSearchParams()
  const { loginWithOAuth, error: authError } = useAuth()
  const [oauthLoading, setOauthLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState(searchParams.get('error') || null)

  const handleOAuthLogin = async () => {
    setOauthLoading(true)
    setErrorMessage(null)
    try {
      await loginWithOAuth()
    } catch (err) {
      setErrorMessage(err.message || 'Google OAuth failed')
      setOauthLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-white flex items-center justify-center px-4 py-10">
      <main className="w-full max-w-md border border-[#dadce0] rounded-lg bg-white p-6">
        <div className="flex items-center gap-3 mb-6">
          <div className="w-10 h-10 rounded-lg border border-[#dadce0] flex items-center justify-center">
            <ShieldCheck size={22} className="text-[#1a73e8]" />
          </div>
          <div>
            <h1 className="text-xl font-semibold text-[#202124]">Drive Health</h1>
            <p className="text-sm text-[#5f6368]">Sign in with Google to continue.</p>
          </div>
        </div>

        {(errorMessage || authError) && (
          <div className="mb-5 p-3 border border-red-200 rounded-lg flex items-start gap-2 text-sm text-red-700 bg-red-50">
            <AlertCircle size={18} className="shrink-0 text-red-600 mt-0.5" />
            <div>{errorMessage || authError}</div>
          </div>
        )}

        <button
          type="button"
          onClick={handleOAuthLogin}
          disabled={oauthLoading}
          className="w-full flex items-center justify-center gap-3 px-4 py-3 bg-white hover:bg-[#f8f9fa] text-[#202124] text-sm font-medium rounded-lg border border-[#dadce0] disabled:opacity-60 disabled:cursor-not-allowed"
        >
          <svg width="20" height="20" viewBox="0 0 24 24" aria-hidden="true">
            <path
              fill="#EA4335"
              d="M12 5c1.6 0 3 .6 4.1 1.6l3.1-3.1C17.3 1.7 14.8 1 12 1 7.5 1 3.7 3.6 1.9 7.3l3.7 2.9C6.5 7.4 9 5 12 5z"
            />
            <path
              fill="#4285F4"
              d="M23.5 12.3c0-.8-.1-1.6-.2-2.3H12v4.5h6.5c-.3 1.5-1.1 2.8-2.4 3.7l3.7 2.9c2.2-2 3.7-5 3.7-8.8z"
            />
            <path
              fill="#FBBC05"
              d="M5.6 14.8c-.2-.8-.4-1.7-.4-2.8s.2-2 .4-2.8L1.9 6.3C.7 8.7 0 10.3 0 12s.7 3.3 1.9 5.7l3.7-2.9z"
            />
            <path
              fill="#34A853"
              d="M12 23c3.2 0 6-1.1 8-3l-3.7-2.9c-1.1.7-2.5 1.2-4.3 1.2-3 0-5.5-2-6.4-4.8L1.9 16.4C3.7 20.1 7.5 23 12 23z"
            />
          </svg>
          <span>{oauthLoading ? 'Opening Google sign in...' : 'Continue with Google'}</span>
        </button>

        <p className="mt-5 text-xs leading-5 text-[#5f6368]">
          Drive Health uses Google OAuth to request Drive metadata access for scans and reports.
        </p>
      </main>
    </div>
  )
}
