import { useState, useEffect, useMemo } from 'react'
import { useAuth } from '../context/AuthContext'
import {
  AlertCircle,
  CheckCircle2,
  EyeOff,
  RotateCcw,
  ExternalLink,
  Filter,
  RefreshCw,
  Search,
  SlidersHorizontal,
  Sparkles,
} from 'lucide-react'
import Loading from '../components/common/Loading'
import ErrorMessage from '../components/common/ErrorMessage'
import EmptyState from '../components/common/EmptyState'
import {
  getFindings,
  updateFindingStatus,
  formatBytes,
  formatDate,
  getSeverityBadgeStyle,
  getStatusBadgeStyle,
} from '../services/driveHealthApi'

const STATUS_FILTERS = [
  { label: 'All Statuses', value: 'ALL' },
  { label: 'Open Issues', value: 'OPEN' },
  { label: 'Resolved', value: 'RESOLVED' },
  { label: 'Ignored', value: 'IGNORED' },
]

const SEVERITY_FILTERS = [
  { label: 'All Severities', value: 'ALL' },
  { label: 'Critical', value: 'CRITICAL' },
  { label: 'High', value: 'HIGH' },
  { label: 'Medium', value: 'MEDIUM' },
  { label: 'Low', value: 'LOW' },
  { label: 'Info', value: 'INFO' },
]

const TYPE_FILTERS = [
  { label: 'All Categories', value: 'ALL' },
  { label: 'Duplicates', value: 'DUPLICATE' },
  { label: 'Old Files', value: 'OLD_FILE' },
  { label: 'Large Files', value: 'LARGE_FILE' },
  { label: 'External Shares', value: 'EXTERNAL_SHARE' },
  { label: 'Public Files', value: 'PUBLIC_FILE' },
]

export default function FindingsHub() {
  const { activeAccountId, activeAccount } = useAuth()
  const [findings, setFindings] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [updatingId, setUpdatingId] = useState(null)

  const [statusFilter, setStatusFilter] = useState('OPEN')
  const [severityFilter, setSeverityFilter] = useState('ALL')
  const [typeFilter, setTypeFilter] = useState('ALL')
  const [searchQuery, setSearchQuery] = useState('')

  const loadFindings = async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await getFindings({ accountId: activeAccountId })
      setFindings(data || [])
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadFindings()
  }, [activeAccountId])

  const handleStatusChange = async (findingId, newStatus) => {
    setUpdatingId(findingId)
    try {
      const updated = await updateFindingStatus(findingId, newStatus)
      setFindings((prev) =>
        prev.map((f) => (f.id === findingId ? { ...f, status: updated.status } : f))
      )
    } catch (err) {
      alert('Failed to update finding status: ' + err.message)
    } finally {
      setUpdatingId(null)
    }
  }

  // Filtered findings
  const filteredFindings = useMemo(() => {
    return findings.filter((item) => {
      if (statusFilter !== 'ALL' && item.status !== statusFilter) return false
      if (severityFilter !== 'ALL' && item.severity !== severityFilter) return false
      if (typeFilter !== 'ALL' && item.findingType !== typeFilter) return false

      if (searchQuery.trim()) {
        const q = searchQuery.toLowerCase()
        const matchFile = item.fileName?.toLowerCase().includes(q)
        const matchDesc = item.description?.toLowerCase().includes(q)
        const matchReason = item.reason?.toLowerCase().includes(q)
        const matchType = item.findingType?.toLowerCase().includes(q)
        if (!matchFile && !matchDesc && !matchReason && !matchType) return false
      }

      return true
    })
  }, [findings, statusFilter, severityFilter, typeFilter, searchQuery])

  // Status counts
  const counts = useMemo(() => {
    return {
      open: findings.filter((f) => f.status === 'OPEN').length,
      resolved: findings.filter((f) => f.status === 'RESOLVED').length,
      ignored: findings.filter((f) => f.status === 'IGNORED').length,
    }
  }, [findings])

  return (
    <div className="h-[calc(100vh-64px)] overflow-y-auto px-6 py-6 sm:px-8 bg-slate-50/50">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-bold text-slate-900 tracking-tight">
              Hygiene Findings Hub
            </h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-50 text-amber-800 border border-amber-200">
              {counts.open} open issues
            </span>
          </div>
          <p className="text-xs text-slate-500 mt-1">
            Universal issue management for{' '}
            <strong className="text-slate-700">{activeAccount?.email || 'Connected Drive'}</strong>. Resolve or ignore items to boost your health score.
          </p>
        </div>

        <button
          onClick={loadFindings}
          disabled={loading}
          className="flex items-center gap-1.5 px-3.5 py-2 bg-white hover:bg-slate-50 text-slate-700 border border-slate-200 rounded-xl text-xs font-medium shadow-sm transition-all self-start sm:self-auto"
        >
          <RefreshCw size={13} className={loading ? 'animate-spin' : ''} />
          <span>Refresh Findings</span>
        </button>
      </div>

      {/* Filter Toolbar */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-4 mb-6 space-y-3">
        <div className="flex flex-wrap items-center justify-between gap-3">
          {/* Status Tabs */}
          <div className="flex bg-slate-100 p-1 rounded-xl text-xs font-medium">
            {STATUS_FILTERS.map((s) => {
              const count =
                s.value === 'OPEN'
                  ? counts.open
                  : s.value === 'RESOLVED'
                  ? counts.resolved
                  : s.value === 'IGNORED'
                  ? counts.ignored
                  : findings.length

              return (
                <button
                  key={s.value}
                  onClick={() => setStatusFilter(s.value)}
                  className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg transition-all ${
                    statusFilter === s.value
                      ? 'bg-white text-blue-600 shadow-sm font-semibold'
                      : 'text-slate-600 hover:text-slate-900'
                  }`}
                >
                  <span>{s.label}</span>
                  <span className="px-1.5 py-0.2 rounded-full text-[10px] bg-slate-200/70 font-semibold text-slate-700">
                    {count}
                  </span>
                </button>
              )
            })}
          </div>

          {/* Search Bar */}
          <div className="relative min-w-[240px] flex-1 max-w-sm">
            <Search
              size={14}
              className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400"
            />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search findings by file or keyword…"
              className="w-full pl-9 pr-3.5 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-800 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:bg-white transition-all"
            />
          </div>
        </div>

        {/* Dropdown Filters */}
        <div className="flex flex-wrap items-center gap-3 pt-2 border-t border-slate-100 text-xs">
          <div className="flex items-center gap-2">
            <span className="text-slate-400 font-medium">Category:</span>
            <select
              value={typeFilter}
              onChange={(e) => setTypeFilter(e.target.value)}
              className="bg-slate-50 border border-slate-200 rounded-lg px-2.5 py-1.5 text-xs text-slate-700 focus:outline-none focus:ring-1 focus:ring-blue-500"
            >
              {TYPE_FILTERS.map((t) => (
                <option key={t.value} value={t.value}>
                  {t.label}
                </option>
              ))}
            </select>
          </div>

          <div className="flex items-center gap-2">
            <span className="text-slate-400 font-medium">Severity:</span>
            <select
              value={severityFilter}
              onChange={(e) => setSeverityFilter(e.target.value)}
              className="bg-slate-50 border border-slate-200 rounded-lg px-2.5 py-1.5 text-xs text-slate-700 focus:outline-none focus:ring-1 focus:ring-blue-500"
            >
              {SEVERITY_FILTERS.map((s) => (
                <option key={s.value} value={s.value}>
                  {s.label}
                </option>
              ))}
            </select>
          </div>

          {(statusFilter !== 'OPEN' || severityFilter !== 'ALL' || typeFilter !== 'ALL' || searchQuery) && (
            <button
              onClick={() => {
                setStatusFilter('OPEN')
                setSeverityFilter('ALL')
                setTypeFilter('ALL')
                setSearchQuery('')
              }}
              className="text-xs text-blue-600 hover:underline font-medium ml-auto"
            >
              Reset filters
            </button>
          )}
        </div>
      </div>

      {error && (
        <div className="mb-5">
          <ErrorMessage message={error} onRetry={loadFindings} />
        </div>
      )}

      {/* Findings List */}
      {loading ? (
        <Loading message="Loading hygiene analysis findings…" />
      ) : filteredFindings.length === 0 ? (
        <EmptyState
          title={
            statusFilter === 'OPEN' && findings.length > 0
              ? 'Zero open issues!'
              : 'No findings match your criteria'
          }
          description={
            statusFilter === 'OPEN' && findings.length > 0
              ? 'Awesome job! You have resolved or reviewed all detected hygiene issues.'
              : 'Try changing your status or category filter above.'
          }
        />
      ) : (
        <div className="space-y-3">
          {filteredFindings.map((finding) => {
            const isUpdating = updatingId === finding.id

            return (
              <div
                key={finding.id}
                className="bg-white rounded-2xl border border-slate-200/90 shadow-sm p-4 sm:p-5 hover:border-slate-300 transition-all flex flex-col sm:flex-row sm:items-center justify-between gap-4"
              >
                {/* Info Left */}
                <div className="space-y-2 min-w-0 flex-1">
                  <div className="flex flex-wrap items-center gap-2">
                    <span
                      className={`px-2.5 py-0.5 rounded-full text-[10px] font-bold border uppercase tracking-wider ${getSeverityBadgeStyle(
                        finding.severity
                      )}`}
                    >
                      {finding.severity}
                    </span>
                    <span className="px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-slate-100 text-slate-700 border border-slate-200">
                      {finding.findingType?.replace(/_/g, ' ')}
                    </span>
                    <span
                      className={`px-2.5 py-0.5 rounded-full text-[10px] font-semibold border ${getStatusBadgeStyle(
                        finding.status
                      )}`}
                    >
                      {finding.status}
                    </span>
                  </div>

                  <div>
                    <h4 className="text-sm font-semibold text-slate-900 flex items-center gap-2 truncate">
                      <span>{finding.fileName || 'Account Rule Finding'}</span>
                      {finding.webUrl && (
                        <a
                          href={finding.webUrl}
                          target="_blank"
                          rel="noopener noreferrer"
                          title="Open file in Google Drive"
                          className="text-blue-600 hover:text-blue-800"
                        >
                          <ExternalLink size={13} />
                        </a>
                      )}
                    </h4>
                    <p className="text-xs text-slate-600 mt-0.5">
                      {finding.description || finding.reason || 'Flagged by automated Drive Health scanner.'}
                    </p>
                  </div>

                  <div className="flex items-center gap-4 text-[11px] text-slate-400">
                    {finding.fileSize && (
                      <span>File size: {formatBytes(finding.fileSize)}</span>
                    )}
                    <span>Detected: {formatDate(finding.detectedAt || finding.createdAt)}</span>
                  </div>
                </div>

                {/* Actions Right */}
                <div className="flex items-center gap-2 shrink-0 self-end sm:self-center">
                  {finding.status === 'OPEN' ? (
                    <>
                      <button
                        onClick={() => handleStatusChange(finding.id, 'RESOLVED')}
                        disabled={isUpdating}
                        className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-emerald-50 hover:bg-emerald-100 text-emerald-700 text-xs font-semibold border border-emerald-200 transition-colors disabled:opacity-50"
                      >
                        <CheckCircle2 size={13} />
                        <span>Mark Resolved</span>
                      </button>

                      <button
                        onClick={() => handleStatusChange(finding.id, 'IGNORED')}
                        disabled={isUpdating}
                        className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-600 text-xs font-semibold transition-colors disabled:opacity-50"
                      >
                        <EyeOff size={13} />
                        <span>Ignore</span>
                      </button>
                    </>
                  ) : (
                    <button
                      onClick={() => handleStatusChange(finding.id, 'OPEN')}
                      disabled={isUpdating}
                      className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-slate-100 hover:bg-blue-50 hover:text-blue-700 text-slate-600 text-xs font-semibold transition-colors disabled:opacity-50"
                    >
                      <RotateCcw size={13} />
                      <span>Reopen Issue</span>
                    </button>
                  )}
                </div>
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
