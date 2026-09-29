import { useState, useEffect } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import {
  Home,
  FolderOpen,
  AlertTriangle,
  ScanLine,
  BarChart2,
  Settings,
  User,
  Plus,
  RefreshCw,
  ExternalLink,
  HelpCircle,
} from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import { getStorageQuota, formatBytes } from '../../services/driveHealthApi'

const NAV_ITEMS = [
  { to: '/', label: 'Home', icon: Home, end: true },
  { to: '/files', label: 'Files Explorer', icon: FolderOpen },
  { to: '/findings', label: 'Findings Hub', icon: AlertTriangle },
  { to: '/scan-results', label: 'Scan Results', icon: ScanLine },
  { to: '/reports', label: 'Reports', icon: BarChart2 },
  { to: '/settings', label: 'Settings', icon: Settings },
  { to: '/profile', label: 'Profile', icon: User },
  { to: '/docs', label: 'Help & Docs', icon: HelpCircle },
]

export default function Sidebar() {
  const { activeAccount, activeAccountId } = useAuth()
  const navigate = useNavigate()
  const [quota, setQuota] = useState(null)
  const [loadingQuota, setLoadingQuota] = useState(false)

  useEffect(() => {
    let isMounted = true

    const loadQuota = async () => {
      // If active account already carries quota, use it immediately
      if (activeAccount?.storageQuotaLimit != null && activeAccount?.storageQuotaUsage != null) {
        setQuota({
          limitBytes: activeAccount.storageQuotaLimit,
          usageBytes: activeAccount.storageQuotaUsage,
          usageInDriveBytes: activeAccount.storageQuotaUsageInDrive,
          usageInDriveTrashBytes: activeAccount.storageQuotaUsageInDriveTrash,
        })
      }

      if (!activeAccountId) return

      try {
        setLoadingQuota(true)
        const liveQuota = await getStorageQuota(activeAccountId)
        if (isMounted && liveQuota) {
          setQuota(liveQuota)
        }
      } catch (err) {
        console.warn('Could not fetch storage quota:', err)
      } finally {
        if (isMounted) setLoadingQuota(false)
      }
    }

    loadQuota()
    return () => {
      isMounted = false
    }
  }, [activeAccountId, activeAccount?.storageQuotaLimit, activeAccount?.storageQuotaUsage])

  // Calculate real values from Google Drive account
  const usageBytes = quota?.usageBytes ?? activeAccount?.storageQuotaUsage ?? 0
  const limitBytes = quota?.limitBytes ?? activeAccount?.storageQuotaLimit ?? 16106127360 // default 15GB if unset
  const percentUsed = limitBytes > 0 ? Math.min(100, (usageBytes / limitBytes) * 100) : 0

  const usageFormatted = formatBytes(usageBytes)
  const limitFormatted = limitBytes > 0 ? formatBytes(limitBytes) : 'Unlimited'

  return (
    <aside className="fixed top-16 left-0 bottom-0 w-60 bg-white flex flex-col overflow-y-auto z-40 pt-3 pb-4 border-r border-[#e5e7eb]">
      {/* New / Action button */}
      <div className="px-3 mb-3">
        <button
          onClick={() => navigate('/scan-results')}
          className="flex items-center justify-center gap-2.5 px-4 py-2.5 rounded-2xl border border-[#dadce0] text-[#1a73e8] bg-[#f8fafd] hover:bg-[#e8f0fe] hover:shadow-sm text-sm font-medium transition-all w-full shadow-xs"
        >
          <Plus size={18} className="stroke-[2.5]" />
          <span>New Scan</span>
        </button>
      </div>

      {/* Navigation */}
      <nav className="flex-1 px-3 space-y-0.5">
        {NAV_ITEMS.map(({ to, label, icon: Icon, end }) => (
          <NavLink
            key={to}
            to={to}
            end={end}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3.5 py-2.5 rounded-full text-[13px] font-medium transition-colors ${
                isActive
                  ? 'bg-[#e8f0fe] text-[#1a73e8] font-semibold'
                  : 'text-[#3c4043] hover:bg-[#f1f3f4] hover:text-[#202124]'
              }`
            }
          >
            {({ isActive }) => (
              <>
                <Icon
                  size={18}
                  strokeWidth={isActive ? 2.5 : 2}
                  className={isActive ? 'text-[#1a73e8]' : 'text-[#5f6368]'}
                />
                <span className="truncate">{label}</span>
              </>
            )}
          </NavLink>
        ))}
      </nav>

      {/* Storage section in bottom-left corner with real Google Drive size */}
      <div className="px-4 pt-3 mt-auto border-t border-[#f1f3f4]">
        <div className="flex items-center justify-between text-xs text-[#5f6368] mb-1.5 font-medium">
          <span>Storage</span>
          {loadingQuota && <RefreshCw size={11} className="animate-spin text-[#1a73e8]" />}
        </div>

        {/* Real usage progress bar */}
        <div className="w-full h-1.5 bg-[#e8eaed] rounded-full overflow-hidden mb-2">
          <div
            className={`h-full rounded-full transition-all duration-500 ${
              percentUsed > 90
                ? 'bg-[#d93025]'
                : percentUsed > 75
                ? 'bg-[#f29900]'
                : 'bg-[#1a73e8]'
            }`}
            style={{ width: `${Math.max(percentUsed, 1)}%` }}
          />
        </div>

        {/* Real Storage details */}
        <p className="text-xs text-[#3c4043] mb-1 font-medium">
          {usageFormatted} of {limitFormatted} used
        </p>

        <p className="text-[11px] text-[#5f6368] mb-2.5">
          {percentUsed.toFixed(1)}% of your Google Drive
        </p>

        <a
          href="https://one.google.com/storage"
          target="_blank"
          rel="noopener noreferrer"
          className="flex items-center justify-center gap-1.5 w-full py-1.5 px-3 rounded-full border border-[#dadce0] text-[#1a73e8] hover:bg-[#f6fafe] text-xs font-medium transition-colors"
        >
          <span>Get more storage</span>
          <ExternalLink size={12} />
        </a>
      </div>
    </aside>
  )
}
