import { BrowserRouter, Routes, Route } from 'react-router-dom'
import MainLayout from './components/layout/MainLayout'
import Home from './pages/Home'
import Login from './pages/Login'
import OAuthCallback from './pages/OAuthCallback'
import ScanResults from './pages/ScanResults'
import Reports from './pages/Reports'
import Settings from './pages/Settings'
import Profile from './pages/Profile'
import FilesExplorer from './pages/FilesExplorer'
import FindingsHub from './pages/FindingsHub'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="login" element={<Login />} />
        <Route path="oauth/callback" element={<OAuthCallback />} />
        <Route element={<MainLayout />}>
          <Route index element={<Home />} />
          <Route path="files" element={<FilesExplorer />} />
          <Route path="findings" element={<FindingsHub />} />
          <Route path="scan-results" element={<ScanResults />} />
          <Route path="reports" element={<Reports />} />
          <Route path="settings" element={<Settings />} />
          <Route path="profile" element={<Profile />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
