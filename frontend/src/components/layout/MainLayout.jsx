import { Navigate, Outlet, useLocation } from 'react-router-dom'
import TopBar from './TopBar'
import Sidebar from './Sidebar'
import { useAuth } from '../../context/AuthContext'

export default function MainLayout() {
  const location = useLocation()
  const { isAuthenticated, loading } = useAuth()

  if (loading) {
    return (
      <div className="min-h-screen bg-white flex items-center justify-center text-sm text-[#5f6368]">
        Loading Drive Health...
      </div>
    )
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  return (
    <div className="min-h-screen bg-white">
      <TopBar />
      <Sidebar />
      <main className="ml-56 mt-16 min-h-[calc(100vh-64px)]">
        <Outlet />
      </main>
    </div>
  )
}
