import { Outlet } from 'react-router-dom'
import TopBar from './TopBar'
import Sidebar from './Sidebar'

export default function MainLayout() {
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
