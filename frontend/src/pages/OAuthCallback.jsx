import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { AlertCircle, CheckCircle2 } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import api from '../services/api'

export default function OAuthCallback() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const { handleOAuthCallback } = useAuth()
  const [status, setStatus] = useState('processing')
  const [message, setMessage] = useState('Completing Google OAuth sign in...')

  useEffect(() => {
    const processCallback = async () => {
      const error = searchParams.get('error')
      if (error) {
        setStatus('error')
        setMessage(decodeURIComponent(error))
        return
      }

      const userId = searchParams.get('userId')
      const code = searchParams.get('code')

      try {
        if (userId) {
          await handleOAuthCallback(userId)
        } else if (code) {
          const res = await api.get('/auth/google/callback', { params: { code } })
          if (!res.data?.user?.id) {
            throw new Error('User profile was not returned by OAuth callback')
          }
          await handleOAuthCallback(res.data.user.id)
        } else {
          throw new Error('No authorization code was returned by Google')
        }

        setStatus('success')
        setMessage('Google account connected.')
        navigate('/', { replace: true })
      } catch (err) {
        setStatus('error')
        setMessage(err.message || 'OAuth authentication failed')
      }
    }

    processCallback()
  }, [searchParams, handleOAuthCallback, navigate])

  return (
    <div className="min-h-screen bg-white flex items-center justify-center p-4">
      <main className="bg-white rounded-lg border border-[#dadce0] p-6 max-w-md w-full text-center">
        {status === 'processing' && (
          <div>
            <h1 className="text-xl font-semibold text-[#202124] mb-2">Connecting to Google</h1>
            <p className="text-sm text-[#5f6368]">{message}</p>
          </div>
        )}

        {status === 'success' && (
          <div>
            <div className="w-12 h-12 rounded-lg bg-emerald-50 text-emerald-700 flex items-center justify-center mx-auto mb-4 border border-emerald-100">
              <CheckCircle2 size={28} />
            </div>
            <h1 className="text-xl font-semibold text-[#202124] mb-2">Signed in</h1>
            <p className="text-sm text-[#5f6368]">{message}</p>
          </div>
        )}

        {status === 'error' && (
          <div>
            <div className="w-12 h-12 rounded-lg bg-red-50 text-red-700 flex items-center justify-center mx-auto mb-4 border border-red-100">
              <AlertCircle size={28} />
            </div>
            <h1 className="text-xl font-semibold text-[#202124] mb-2">Connection failed</h1>
            <p className="text-sm text-red-700 mb-5 bg-red-50 p-3 rounded-lg border border-red-100">
              {message}
            </p>
            <button
              type="button"
              onClick={() => navigate('/login')}
              className="w-full py-2.5 px-4 bg-[#1a73e8] hover:bg-[#1557b0] text-white text-sm font-medium rounded-lg"
            >
              Return to sign in
            </button>
          </div>
        )}
      </main>
    </div>
  )
}
