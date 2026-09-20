import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Files, Copy, Clock, HardDrive, Share2,
  ScanLine, FileText, Database, Settings, ChevronRight
} from 'lucide-react'
import SummaryCard from '../components/dashboard/SummaryCard'
import Loading from '../components/common/Loading'
import ErrorMessage from '../components/common/ErrorMessage'
import {
  getDashboardSummary,
  scanDrive,
  getScans,
  formatBytes,
} from '../services/driveHealthApi'

const QUICK_ACTIONS = [
  { label: 'View full report', icon: FileText, to: '/reports' },
  { label: 'Scan now', icon: ScanLine, action: 'scan' },
  { label: 'Manage storage', icon: Database, to: '/scan-results' },
  { label: 'Settings', icon: Settings, to: '/settings' },
]

export default function Home() {
  const navigate = useNavigate()
  const [summary, setSummary] = useState(null)
  const [lastScan, setLastScan] = useState(null)
  const [loading, setLoading] = useState(true)
  const [scanning, setScanning] = useState(false)
  const [error, setError] = useState(null)

  const fetchData = async () => {
    setLoading(true)
    setError(null)
    try {
      const [sum, scans] = await Promise.all([
        getDashboardSummary(),
        getScans().catch(() => []),
      ])
      setSummary(sum)
      if (scans && scans.length > 0) {
        setLastScan(scans[0])
      }
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchData() }, [])

  const handleScan = async () => {
    setScanning(true)
    setError(null)
    try {
      await scanDrive(false)
      await fetchData()
      navigate('/scan-results')
    } catch (e) {
      setError(e.message)
    } finally {
      setScanning(false)
    }
  }

  const handleQuickAction = (action) => {
    if (action.action === 'scan') handleScan()
    else if (action.to) navigate(action.to)
  }

  const cards = summary
    ? [
        { label: 'Total Files', value: summary.totalFiles?.toLocaleString(), icon: <Files size={18} />, color: '#4285F4' },
        { label: 'Duplicates', value: summary.duplicateFiles?.toLocaleString(), icon: <Copy size={18} />, color: '#EA4335' },
        { label: 'Old Files', value: summary.oldFiles?.toLocaleString(), icon: <Clock size={18} />, color: '#F4B400' },
        { label: 'Large Files', value: summary.largeFiles?.toLocaleString(), icon: <HardDrive size={18} />, color: '#1A73E8' },
        { label: 'Shared Externally', value: summary.externalShares?.toLocaleString(), icon: <Share2 size={18} />, color: '#0F9D58' },
      ]
    : []

  return (
    <div className="flex h-[calc(100vh-64px)]">
      {/* Main content */}
      <div className="flex-1 overflow-y-auto px-8 py-8">
        {/* Header */}
        <div className="flex items-start justify-between mb-8">
          <div>
            <div className="flex items-center gap-2 mb-1">
              <svg width="24" height="24" viewBox="0 0 32 32" fill="none">
                <path d="M16 4L28 24H4L16 4Z" fill="#0F9D58" opacity="0.9" />
                <path d="M4 24L10 14H28L22 24H4Z" fill="#4285F4" opacity="0.9" />
                <path d="M10 14L16 4L22 14H10Z" fill="#FBBC04" />
              </svg>
              <h1 className="text-xl font-medium text-[#202124]">Drive Health</h1>
            </div>
            <p className="text-sm text-[#5f6368]">
              Keep your Google Drive organized, secure and running smoothly.
            </p>
            {lastScan && (
              <p className="text-xs text-[#5f6368] mt-1">
                Last scan:{' '}
                {new Date(lastScan.completedAt || lastScan.startedAt).toLocaleDateString('en-US', {
                  month: 'short', day: 'numeric', year: 'numeric',
                  hour: 'numeric', minute: '2-digit',
                })}
              </p>
            )}
          </div>

          <button
            onClick={handleScan}
            disabled={scanning}
            className="flex items-center gap-2 px-5 py-2.5 bg-[#1a73e8] hover:bg-[#1557b0] text-white text-sm font-medium rounded-full transition-colors disabled:opacity-60 disabled:cursor-not-allowed"
          >
            {scanning ? (
              <>
                <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                Scanning…
              </>
            ) : (
              <>
                <ScanLine size={16} />
                Scan now
              </>
            )}
          </button>
        </div>

        {error && (
          <div className="mb-6">
            <ErrorMessage message={error} onRetry={fetchData} />
          </div>
        )}

        {loading ? (
          <Loading message="Loading dashboard…" />
        ) : (
          <>
            {/* Summary cards */}
            <div className="grid grid-cols-5 gap-3 mb-8">
              {cards.map((card) => (
                <SummaryCard
                  key={card.label}
                  icon={card.icon}
                  label={card.label}
                  value={card.value}
                  color={card.color}
                />
              ))}
            </div>

            {/* Storage info */}
            {summary && (
              <div className="flex items-center gap-6 py-4 px-5 bg-[#f8f9fa] rounded-lg border border-[#e0e0e0] text-sm text-[#5f6368]">
                <span>
                  <strong className="text-[#202124]">{summary.totalFiles?.toLocaleString()}</strong> total files
                </span>
                <span className="text-[#e0e0e0]">|</span>
                <span>
                  <strong className="text-[#202124]">{formatBytes(summary.totalStorageBytes)}</strong> used
                </span>
                {summary.trashedFiles > 0 && (
                  <>
                    <span className="text-[#e0e0e0]">|</span>
                    <span>
                      <strong className="text-[#202124]">{summary.trashedFiles?.toLocaleString()}</strong> in trash
                    </span>
                  </>
                )}
                {summary.openFindings > 0 && (
                  <>
                    <span className="text-[#e0e0e0]">|</span>
                    <span>
                      <strong className="text-[#EA4335]">{summary.openFindings?.toLocaleString()}</strong> open issues
                    </span>
                  </>
                )}
              </div>
            )}
          </>
        )}
      </div>

      {/* Right panel */}
      <div className="w-72 shrink-0 border-l border-[#e0e0e0] px-5 py-6">
        {/* Drive Health info */}
        <div className="mb-6">
          <div className="flex items-center gap-2 mb-1">
            <svg width="18" height="18" viewBox="0 0 32 32" fill="none">
              <path d="M16 4L28 24H4L16 4Z" fill="#0F9D58" opacity="0.9" />
              <path d="M4 24L10 14H28L22 24H4Z" fill="#4285F4" opacity="0.9" />
              <path d="M10 14L16 4L22 14H10Z" fill="#FBBC04" />
            </svg>
            <h2 className="text-sm font-medium text-[#202124]">Drive Health</h2>
          </div>
          <p className="text-xs text-[#5f6368]">
            Keep your Drive organized, secure and running smoothly.
          </p>
        </div>

        {/* Quick actions */}
        <div className="space-y-1 mb-6">
          {QUICK_ACTIONS.map((action) => (
            <button
              key={action.label}
              onClick={() => handleQuickAction(action)}
              className="flex items-center justify-between w-full px-3 py-2.5 rounded-lg text-sm text-[#202124] hover:bg-[#f1f3f4] transition-colors group"
            >
              <div className="flex items-center gap-3">
                <action.icon size={18} className="text-[#5f6368]" />
                {action.label}
              </div>
              <ChevronRight size={16} className="text-[#5f6368]" />
            </button>
          ))}
        </div>

        {/* Tips */}
        <div className="bg-[#e8f0fe] rounded-lg p-4">
          <div className="flex items-center gap-2 mb-2">
            <div className="w-5 h-5 rounded-full bg-[#1a73e8] flex items-center justify-center">
              <span className="text-white text-xs font-bold">i</span>
            </div>
            <span className="text-sm font-medium text-[#202124]">Quick tips</span>
          </div>
          <p className="text-xs text-[#5f6368] leading-relaxed">
            Remove duplicates, delete old files and manage large files to free up
            space and keep your Drive healthy.
          </p>
        </div>
      </div>
    </div>
  )
}
