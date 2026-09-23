import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Files,
  Copy,
  Clock,
  HardDrive,
  Share2,
  ScanLine,
  FileText,
  Database,
  Settings,
  ChevronRight,
  ShieldCheck,
  AlertTriangle,
  RefreshCw,
  FolderOpen,
  Trash2,
  Sparkles,
  ArrowUpRight,
} from 'lucide-react'
import SummaryCard from '../components/dashboard/SummaryCard'
import Loading from '../components/common/Loading'
import ErrorMessage from '../components/common/ErrorMessage'
import { useAuth } from '../context/AuthContext'
import {
  getDashboardSummary,
  scanDrive,
  getScans,
  getFindings,
  formatBytes,
  formatDateTime,
  calculateHealthScore,
  getSeverityBadgeStyle,
} from '../services/driveHealthApi'

export default function Home() {
  const navigate = useNavigate()
  const { activeAccount, activeAccountId } = useAuth()
  const [summary, setSummary] = useState(null)
  const [lastScan, setLastScan] = useState(null)
  const [criticalFindings, setCriticalFindings] = useState([])
  const [loading, setLoading] = useState(true)
  const [scanning, setScanning] = useState(false)
  const [error, setError] = useState(null)

  const fetchData = async () => {
    setLoading(true)
    setError(null)
    try {
      const [sum, scans, findings] = await Promise.all([
        getDashboardSummary(activeAccountId).catch(() => null),
        getScans(activeAccountId).catch(() => []),
        getFindings({ accountId: activeAccountId, status: 'OPEN' }).catch(() => []),
      ])
      setSummary(sum)
      if (scans && scans.length > 0) {
        setLastScan(scans[0])
      }
      if (findings && findings.length > 0) {
        setCriticalFindings(findings.slice(0, 5))
      } else {
        setCriticalFindings([])
      }
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
  }, [activeAccountId])

  const handleScan = async () => {
    setScanning(true)
    setError(null)
    try {
      await scanDrive(false, activeAccountId)
      await fetchData()
      navigate('/scan-results')
    } catch (e) {
      setError(e.message)
    } finally {
      setScanning(false)
    }
  }

  const healthScore = calculateHealthScore(summary)

  const getHealthBadge = (score) => {
    if (score >= 90) return { label: 'Optimal Health', color: 'bg-[#e6f4ea] text-[#137333] border-[#ceead6]' }
    if (score >= 70) return { label: 'Good Standing', color: 'bg-[#e8f0fe] text-[#1a73e8] border-[#d2e3fc]' }
    if (score >= 50) return { label: 'Attention Needed', color: 'bg-[#fef7e0] text-[#b06000] border-[#fce8b2]' }
    return { label: 'Critical Hygiene Issues', color: 'bg-[#fce8e6] text-[#c5221f] border-[#fad2cf]' }
  }

  const badge = getHealthBadge(healthScore)

  const liveUsageBytes = summary?.storageQuotaUsage ?? activeAccount?.storageQuotaUsage ?? summary?.totalStorageBytes ?? 0
  const liveLimitBytes = summary?.storageQuotaLimit ?? activeAccount?.storageQuotaLimit ?? 16106127360
  const livePercent = liveLimitBytes > 0 ? Math.min(100, (liveUsageBytes / liveLimitBytes) * 100) : 0

  const cards = summary
    ? [
        {
          label: 'Total Files',
          value: summary.totalFiles?.toLocaleString(),
          icon: <Files size={18} />,
          color: '#4285F4',
          subtext: `${formatBytes(summary.totalStorageBytes)} indexed`,
        },
        {
          label: 'Duplicates',
          value: summary.duplicateFiles?.toLocaleString(),
          icon: <Copy size={18} />,
          color: '#EA4335',
          subtext: `${summary.duplicateGroups || 0} groups detected`,
        },
        {
          label: 'Old Files',
          value: summary.oldFiles?.toLocaleString(),
          icon: <Clock size={18} />,
          color: '#F4B400',
          subtext: '> 2 years untouched',
        },
        {
          label: 'Large Files',
          value: summary.largeFiles?.toLocaleString(),
          icon: <HardDrive size={18} />,
          color: '#1A73E8',
          subtext: '> 500 MB',
        },
        {
          label: 'Shared Externally',
          value: summary.externalShares?.toLocaleString(),
          icon: <Share2 size={18} />,
          color: '#0F9D58',
          subtext: 'Outside domain / Public',
        },
      ]
    : []

  return (
    <div className="flex h-[calc(100vh-64px)] overflow-hidden">
      {/* Main content */}
      <div className="flex-1 overflow-y-auto px-8 py-8">
        {/* Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
          <div>
            <div className="flex items-center gap-2 mb-1">
              <svg width="26" height="26" viewBox="0 0 32 32" fill="none">
                <path d="M16 4L28 24H4L16 4Z" fill="#0F9D58" opacity="0.9" />
                <path d="M4 24L10 14H28L22 24H4Z" fill="#4285F4" opacity="0.9" />
                <path d="M10 14L16 4L22 14H10Z" fill="#FBBC04" />
              </svg>
              <h1 className="text-2xl font-bold text-[#202124]">Drive Health Dashboard</h1>
            </div>
            <p className="text-sm text-[#5f6368]">
              Continuous hygiene, duplicate reduction, security, and storage optimization for{' '}
              <strong className="text-[#202124]">{activeAccount?.email || 'your Drive'}</strong>
            </p>
          </div>

          <div className="flex items-center gap-3">
            <button
              onClick={fetchData}
              title="Refresh Dashboard"
              className="p-2.5 rounded-full border border-[#dadce0] hover:bg-[#f1f3f4] text-[#5f6368] transition-colors"
            >
              <RefreshCw size={16} />
            </button>

            <button
              onClick={handleScan}
              disabled={scanning}
              className="flex items-center gap-2 px-5 py-2.5 bg-[#1a73e8] hover:bg-[#1557b0] text-white text-sm font-medium rounded-full transition-colors shadow-xs disabled:opacity-60 disabled:cursor-not-allowed"
            >
              {scanning ? (
                <>
                  <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                  <span>Scanning Drive…</span>
                </>
              ) : (
                <>
                  <ScanLine size={16} />
                  <span>Scan Drive Now</span>
                </>
              )}
            </button>
          </div>
        </div>

        {error && (
          <div className="mb-6">
            <ErrorMessage message={error} onRetry={fetchData} />
          </div>
        )}

        {loading ? (
          <Loading message="Compiling hygiene metrics and live storage…" />
        ) : (
          <div className="space-y-6">
            {/* Top Overview Cards: Health Score & Live Quota */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
              {/* Drive Health Score Card */}
              <div className="bg-white border border-[#e0e0e0] rounded-2xl p-5 shadow-xs flex items-center justify-between">
                <div>
                  <span className="text-xs font-medium text-[#5f6368] uppercase tracking-wider">
                    Drive Health Score
                  </span>
                  <div className="flex items-baseline gap-2 mt-1">
                    <span className="text-3xl font-bold text-[#202124]">{healthScore}</span>
                    <span className="text-sm text-[#5f6368]">/ 100</span>
                  </div>
                  <div className="mt-2">
                    <span
                      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${badge.color}`}
                    >
                      {badge.label}
                    </span>
                  </div>
                </div>

                <div className="w-14 h-14 rounded-full bg-[#f8fafd] border-4 border-[#e8f0fe] flex items-center justify-center text-[#1a73e8]">
                  <ShieldCheck size={28} />
                </div>
              </div>

              {/* Actual Google Drive Storage Quota Card */}
              <div className="md:col-span-2 bg-white border border-[#e0e0e0] rounded-2xl p-5 shadow-xs flex flex-col justify-between">
                <div>
                  <div className="flex items-center justify-between mb-1.5">
                    <div className="flex items-center gap-2">
                      <HardDrive size={16} className="text-[#1a73e8]" />
                      <span className="text-xs font-semibold text-[#202124]">
                        Google Drive Account Storage
                      </span>
                    </div>
                    <span className="text-xs font-medium text-[#5f6368]">
                      {livePercent.toFixed(1)}% full
                    </span>
                  </div>

                  {/* Progress Bar */}
                  <div className="w-full h-2.5 bg-[#e8eaed] rounded-full overflow-hidden mb-2">
                    <div
                      className={`h-full rounded-full transition-all duration-500 ${
                        livePercent > 90
                          ? 'bg-[#d93025]'
                          : livePercent > 75
                          ? 'bg-[#f29900]'
                          : 'bg-[#1a73e8]'
                      }`}
                      style={{ width: `${Math.max(livePercent, 1)}%` }}
                    />
                  </div>

                  <div className="flex items-center justify-between text-xs text-[#5f6368]">
                    <span>
                      <strong className="text-[#202124]">{formatBytes(liveUsageBytes)}</strong> of{' '}
                      {liveLimitBytes > 0 ? formatBytes(liveLimitBytes) : 'Unlimited'} used
                    </span>
                    <button
                      onClick={() => navigate('/profile')}
                      className="text-[#1a73e8] hover:underline flex items-center gap-1 font-medium"
                    >
                      <span>View Quota Details</span>
                      <ArrowUpRight size={13} />
                    </button>
                  </div>
                </div>
              </div>
            </div>

            {/* Metric Summary Cards */}
            <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-3.5">
              {cards.map((card) => (
                <SummaryCard
                  key={card.label}
                  icon={card.icon}
                  label={card.label}
                  value={card.value}
                  color={card.color}
                  subtext={card.subtext}
                />
              ))}
            </div>

            {/* Quick Actions & Recent Scan Section */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
              {/* Left 2 Cols: Actionable Insights / Open Findings */}
              <div className="lg:col-span-2 bg-white border border-[#e0e0e0] rounded-2xl p-6 shadow-xs">
                <div className="flex items-center justify-between mb-4">
                  <div className="flex items-center gap-2">
                    <AlertTriangle size={18} className="text-[#ea4335]" />
                    <h2 className="text-base font-semibold text-[#202124]">
                      Hygiene Findings Requiring Attention
                    </h2>
                  </div>
                  <button
                    onClick={() => navigate('/findings')}
                    className="text-xs font-medium text-[#1a73e8] hover:underline flex items-center gap-1"
                  >
                    <span>View all ({summary?.openFindings || 0})</span>
                    <ChevronRight size={13} />
                  </button>
                </div>

                {criticalFindings.length === 0 ? (
                  <div className="py-8 text-center bg-[#f8fafd] rounded-xl border border-dashed border-[#dadce0]">
                    <Sparkles className="mx-auto text-[#1a73e8] mb-2" size={24} />
                    <p className="text-sm font-medium text-[#202124]">No open critical findings</p>
                    <p className="text-xs text-[#5f6368] mt-1">
                      Your Google Drive has no urgent duplicate or permission risks flagged.
                    </p>
                  </div>
                ) : (
                  <div className="divide-y divide-[#f1f3f4]">
                    {criticalFindings.map((finding) => (
                      <div
                        key={finding.id}
                        className="py-3 flex items-center justify-between gap-4 hover:bg-[#fafafa] px-2 rounded-lg transition-colors"
                      >
                        <div className="min-w-0">
                          <div className="flex items-center gap-2 mb-0.5">
                            <span
                              className={`text-[10px] font-semibold px-2 py-0.5 rounded-full border ${getSeverityBadgeStyle(
                                finding.severity
                              )}`}
                            >
                              {finding.findingType.replace(/_/g, ' ')}
                            </span>
                            <span className="text-xs font-medium text-[#202124] truncate max-w-xs sm:max-w-md">
                              {finding.fileName || finding.description}
                            </span>
                          </div>
                          <p className="text-[11px] text-[#5f6368] truncate">{finding.explanation}</p>
                        </div>
                        <button
                          onClick={() => navigate('/findings')}
                          className="shrink-0 text-xs font-medium text-[#1a73e8] hover:bg-[#e8f0fe] px-3 py-1.5 rounded-full transition-colors"
                        >
                          Review
                        </button>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* Right Col: Scan Run Status Card */}
              <div className="bg-white border border-[#e0e0e0] rounded-2xl p-6 shadow-xs flex flex-col justify-between">
                <div>
                  <div className="flex items-center gap-2 mb-4">
                    <ScanLine size={18} className="text-[#1a73e8]" />
                    <h2 className="text-base font-semibold text-[#202124]">Latest Scan Activity</h2>
                  </div>

                  {lastScan ? (
                    <div className="space-y-3 text-xs">
                      <div className="flex justify-between items-center py-1.5 border-b border-[#f1f3f4]">
                        <span className="text-[#5f6368]">Status</span>
                        <span
                          className={`font-semibold px-2 py-0.5 rounded-full text-[11px] ${
                            lastScan.status === 'COMPLETED'
                              ? 'bg-[#e6f4ea] text-[#137333]'
                              : lastScan.status === 'FAILED'
                              ? 'bg-[#fce8e6] text-[#c5221f]'
                              : 'bg-[#e8f0fe] text-[#1a73e8]'
                          }`}
                        >
                          {lastScan.status}
                        </span>
                      </div>
                      <div className="flex justify-between items-center py-1.5 border-b border-[#f1f3f4]">
                        <span className="text-[#5f6368]">Executed At</span>
                        <span className="text-[#202124] font-medium">
                          {formatDateTime(lastScan.completedAt || lastScan.startedAt)}
                        </span>
                      </div>
                      <div className="flex justify-between items-center py-1.5 border-b border-[#f1f3f4]">
                        <span className="text-[#5f6368]">Files Scanned</span>
                        <span className="text-[#202124] font-medium">
                          {lastScan.filesScanned?.toLocaleString() || 0}
                        </span>
                      </div>
                      <div className="flex justify-between items-center py-1.5 border-b border-[#f1f3f4]">
                        <span className="text-[#5f6368]">New Files Indexed</span>
                        <span className="text-[#202124] font-medium">
                          {lastScan.newFiles?.toLocaleString() || 0}
                        </span>
                      </div>
                      <div className="flex justify-between items-center py-1.5">
                        <span className="text-[#5f6368]">Scan Mode</span>
                        <span className="text-[#202124] font-medium">{lastScan.scanType}</span>
                      </div>
                    </div>
                  ) : (
                    <p className="text-xs text-[#5f6368] py-4 text-center">
                      No scan executed yet. Click &quot;Scan Drive Now&quot; to begin.
                    </p>
                  )}
                </div>

                <div className="mt-6 pt-4 border-t border-[#f1f3f4]">
                  <button
                    onClick={() => navigate('/scan-results')}
                    className="w-full py-2 px-3 rounded-full border border-[#dadce0] text-[#1a73e8] text-xs font-medium hover:bg-[#f8fafd] transition-colors flex items-center justify-center gap-1.5"
                  >
                    <span>View Scan History & Files</span>
                    <ChevronRight size={13} />
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Right panel Quick Actions */}
      <div className="w-80 shrink-0 border-l border-[#e0e0e0] px-6 py-8 overflow-y-auto bg-[#fafafa]">
        <div className="mb-6">
          <h2 className="text-sm font-semibold text-[#202124] uppercase tracking-wider mb-1">
            Quick Actions
          </h2>
          <p className="text-xs text-[#5f6368]">One-click management tools for your Drive</p>
        </div>

        <div className="space-y-2 mb-8">
          <button
            onClick={() => navigate('/files')}
            className="flex items-center justify-between w-full p-3 rounded-xl bg-white border border-[#e0e0e0] text-sm text-[#202124] hover:bg-[#f1f3f4] hover:border-[#dadce0] transition-all shadow-2xs group"
          >
            <div className="flex items-center gap-3">
              <div className="p-2 rounded-lg bg-[#e8f0fe] text-[#1a73e8]">
                <FolderOpen size={16} />
              </div>
              <div className="text-left">
                <p className="text-xs font-semibold text-[#202124]">Files Explorer</p>
                <p className="text-[11px] text-[#5f6368]">Browse and filter indexed files</p>
              </div>
            </div>
            <ChevronRight size={15} className="text-[#9aa0a6] group-hover:text-[#202124]" />
          </button>

          <button
            onClick={() => navigate('/findings')}
            className="flex items-center justify-between w-full p-3 rounded-xl bg-white border border-[#e0e0e0] text-sm text-[#202124] hover:bg-[#f1f3f4] hover:border-[#dadce0] transition-all shadow-2xs group"
          >
            <div className="flex items-center gap-3">
              <div className="p-2 rounded-lg bg-[#fce8e6] text-[#ea4335]">
                <AlertTriangle size={16} />
              </div>
              <div className="text-left">
                <p className="text-xs font-semibold text-[#202124]">Findings Hub</p>
                <p className="text-[11px] text-[#5f6368]">Review duplicates & permissions</p>
              </div>
            </div>
            <ChevronRight size={15} className="text-[#9aa0a6] group-hover:text-[#202124]" />
          </button>

          <button
            onClick={() => navigate('/reports')}
            className="flex items-center justify-between w-full p-3 rounded-xl bg-white border border-[#e0e0e0] text-sm text-[#202124] hover:bg-[#f1f3f4] hover:border-[#dadce0] transition-all shadow-2xs group"
          >
            <div className="flex items-center gap-3">
              <div className="p-2 rounded-lg bg-[#e6f4ea] text-[#0f9d58]">
                <FileText size={16} />
              </div>
              <div className="text-left">
                <p className="text-xs font-semibold text-[#202124]">Reports</p>
                <p className="text-[11px] text-[#5f6368]">Audit trends and breakdowns</p>
              </div>
            </div>
            <ChevronRight size={15} className="text-[#9aa0a6] group-hover:text-[#202124]" />
          </button>

          <button
            onClick={() => navigate('/settings')}
            className="flex items-center justify-between w-full p-3 rounded-xl bg-white border border-[#e0e0e0] text-sm text-[#202124] hover:bg-[#f1f3f4] hover:border-[#dadce0] transition-all shadow-2xs group"
          >
            <div className="flex items-center gap-3">
              <div className="p-2 rounded-lg bg-[#fef7e0] text-[#f4b400]">
                <Settings size={16} />
              </div>
              <div className="text-left">
                <p className="text-xs font-semibold text-[#202124]">Hygiene Settings</p>
                <p className="text-[11px] text-[#5f6368]">Custom ignore rules & thresholds</p>
              </div>
            </div>
            <ChevronRight size={15} className="text-[#9aa0a6] group-hover:text-[#202124]" />
          </button>
        </div>

        {/* Informative Tip Box */}
        <div className="p-4 rounded-xl bg-[#e8f0fe] border border-[#d2e3fc]">
          <div className="flex items-center gap-2 mb-1.5">
            <Sparkles size={16} className="text-[#1a73e8]" />
            <h3 className="text-xs font-semibold text-[#1a73e8]">Pro Hygiene Tip</h3>
          </div>
          <p className="text-xs text-[#3c4043] leading-relaxed">
            Running a scan indexes Drive metadata to identify large duplicate video files and
            forgotten public external shares without downloading any confidential contents.
          </p>
        </div>
      </div>
    </div>
  )
}
