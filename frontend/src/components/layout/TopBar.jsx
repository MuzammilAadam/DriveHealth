import { useState, useRef, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  HelpCircle,
  Settings as SettingsIcon,
  Grid3x3,
  User,
  LogOut,
  HardDrive,
  ChevronDown,
} from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import { formatBytes } from '../../services/driveHealthApi'

export default function TopBar() {
  const { user, activeAccount, logout } = useAuth()
  const navigate = useNavigate()
  const [menuOpen, setMenuOpen] = useState(false)
  const menuRef = useRef(null)

  const displayName = activeAccount?.name || user?.name || user?.email || 'User'
  const email = activeAccount?.email || user?.email || ''
  const pictureUrl = activeAccount?.pictureUrl
  const initial = displayName.charAt(0).toUpperCase()

  const usageBytes = activeAccount?.storageQuotaUsage ?? 0
  const limitBytes = activeAccount?.storageQuotaLimit ?? 16106127360

  // Close menu on click outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setMenuOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  return (
    <header className="fixed top-0 left-0 right-0 z-50 h-16 flex items-center px-4 bg-white border-b border-[#e0e0e0]">
      {/* Left: Logo */}
      <div
        onClick={() => navigate('/')}
        className="flex items-center gap-2.5 w-60 shrink-0 cursor-pointer select-none"
      >
        <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
          <path d="M16 4L28 24H4L16 4Z" fill="#0F9D58" opacity="0.85" />
          <path d="M4 24L10 14H28L22 24H4Z" fill="#4285F4" opacity="0.85" />
          <path d="M10 14L16 4L22 14H10Z" fill="#FBBC04" opacity="0.9" />
        </svg>
        <span className="text-lg font-medium text-[#202124] tracking-tight hover:text-[#1a73e8] transition-colors">
          Drive Health
        </span>
      </div>

      {/* Center: Search */}
      <div className="flex-1 max-w-2xl mx-auto">
        <div className="relative flex items-center">
          <svg
            className="absolute left-4 text-[#5f6368]"
            width="20"
            height="20"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
          >
            <circle cx="11" cy="11" r="8" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
          <input
            type="text"
            onKeyDown={(e) => {
              if (e.key === 'Enter') {
                navigate('/files')
              }
            }}
            placeholder="Search files or hygiene findings in Drive…"
            className="w-full pl-12 pr-4 py-2.5 bg-[#f1f3f4] rounded-full text-[14px] text-[#202124] placeholder-[#5f6368] border border-transparent focus:outline-none focus:bg-white focus:border-[#1a73e8] focus:shadow-xs transition-all"
          />
        </div>
      </div>

      {/* Right: Action icons & Account menu */}
      <div className="flex items-center gap-1.5 ml-4">
        <button
          onClick={() => navigate('/settings')}
          title="Settings"
          className="p-2.5 rounded-full hover:bg-[#f1f3f4] text-[#5f6368] hover:text-[#202124] transition-colors"
        >
          <SettingsIcon size={20} />
        </button>

        <button
          onClick={() => navigate('/reports')}
          title="Reports"
          className="p-2.5 rounded-full hover:bg-[#f1f3f4] text-[#5f6368] hover:text-[#202124] transition-colors"
        >
          <Grid3x3 size={20} />
        </button>

        {/* User Profile dropdown menu */}
        <div className="relative ml-2 pl-2 border-l border-[#e0e0e0]" ref={menuRef}>
          <button
            onClick={() => setMenuOpen(!menuOpen)}
            className="flex items-center gap-2 p-1 rounded-full hover:bg-[#f1f3f4] transition-colors"
          >
            {pictureUrl ? (
              <img
                src={pictureUrl}
                alt={displayName}
                className="w-8 h-8 rounded-full object-cover ring-1 ring-[#dadce0]"
              />
            ) : (
              <div className="w-8 h-8 rounded-full bg-[#1a73e8] text-white flex items-center justify-center text-sm font-medium select-none shadow-xs">
                {initial}
              </div>
            )}
            <ChevronDown size={14} className="text-[#5f6368] mr-0.5" />
          </button>

          {menuOpen && (
            <div className="absolute right-0 mt-2 w-72 bg-white rounded-2xl shadow-xl border border-[#e0e0e0] py-3 z-50 animate-in fade-in slide-in-from-top-2 duration-150">
              {/* Account header */}
              <div className="px-4 pb-3 border-b border-[#f1f3f4] flex items-center gap-3">
                {pictureUrl ? (
                  <img
                    src={pictureUrl}
                    alt={displayName}
                    className="w-11 h-11 rounded-full object-cover"
                  />
                ) : (
                  <div className="w-11 h-11 rounded-full bg-[#1a73e8] text-white flex items-center justify-center text-lg font-medium">
                    {initial}
                  </div>
                )}
                <div className="min-w-0">
                  <p className="text-sm font-semibold text-[#202124] truncate">{displayName}</p>
                  <p className="text-xs text-[#5f6368] truncate">{email}</p>
                </div>
              </div>

              {/* Storage quick status */}
              <div className="px-4 py-2.5 bg-[#f8fafd] border-b border-[#f1f3f4] text-xs">
                <div className="flex items-center justify-between text-[#5f6368] mb-1 font-medium">
                  <span className="flex items-center gap-1">
                    <HardDrive size={13} className="text-[#1a73e8]" />
                    <span>Drive Storage</span>
                  </span>
                  <span>{limitBytes > 0 ? `${((usageBytes / limitBytes) * 100).toFixed(0)}%` : ''}</span>
                </div>
                <p className="text-[#202124] font-medium text-[11px]">
                  {formatBytes(usageBytes)} of {limitBytes > 0 ? formatBytes(limitBytes) : 'Unlimited'} used
                </p>
              </div>

              {/* Menu items */}
              <div className="py-1">
                <button
                  onClick={() => {
                    setMenuOpen(false)
                    navigate('/profile')
                  }}
                  className="w-full px-4 py-2.5 text-left text-sm text-[#3c4043] hover:bg-[#f1f3f4] flex items-center gap-2.5 transition-colors"
                >
                  <User size={16} className="text-[#5f6368]" />
                  <span>My Profile & Quota</span>
                </button>

                <button
                  onClick={() => {
                    setMenuOpen(false)
                    navigate('/settings')
                  }}
                  className="w-full px-4 py-2.5 text-left text-sm text-[#3c4043] hover:bg-[#f1f3f4] flex items-center gap-2.5 transition-colors"
                >
                  <SettingsIcon size={16} className="text-[#5f6368]" />
                  <span>Settings & Rules</span>
                </button>
              </div>

              <div className="pt-1 border-t border-[#f1f3f4]">
                <button
                  onClick={() => {
                    setMenuOpen(false)
                    logout()
                  }}
                  className="w-full px-4 py-2 text-left text-sm text-[#d93025] hover:bg-[#fce8e6] flex items-center gap-2.5 transition-colors"
                >
                  <LogOut size={16} />
                  <span>Sign out</span>
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </header>
  )
}
