import { useState, useEffect, useMemo } from 'react'
import { useAuth } from '../context/AuthContext'
import {
  Search,
  Filter,
  ExternalLink,
  ArrowUpDown,
  RefreshCw,
  Folder,
  FileText,
  AlertTriangle,
  HardDrive,
  Clock,
  Sparkles,
  ChevronRight,
  X,
} from 'lucide-react'
import Loading from '../components/common/Loading'
import ErrorMessage from '../components/common/ErrorMessage'
import EmptyState from '../components/common/EmptyState'
import FileIcon from '../components/files/FileIcon'
import {
  getStoredFiles,
  getFindings,
  formatBytes,
  formatDate,
  formatDateTime,
  getSeverityBadgeStyle,
} from '../services/driveHealthApi'

const MIME_CATEGORIES = [
  { label: 'All Files', value: 'ALL' },
  { label: 'Documents', value: 'DOCS' },
  { label: 'Spreadsheets', value: 'SHEETS' },
  { label: 'Presentations', value: 'SLIDES' },
  { label: 'PDFs', value: 'PDF' },
  { label: 'Images & Media', value: 'MEDIA' },
  { label: 'Archives', value: 'ARCHIVE' },
]

export default function FilesExplorer() {
  const { activeAccountId, activeAccount } = useAuth()
  const [files, setFiles] = useState([])
  const [findings, setFindings] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [searchQuery, setSearchQuery] = useState('')
  const [selectedCategory, setSelectedCategory] = useState('ALL')
  const [sortBy, setSortBy] = useState('size_desc') // 'size_desc' | 'size_asc' | 'date_desc' | 'name_asc'
  const [selectedFile, setSelectedFile] = useState(null)

  const loadData = async () => {
    setLoading(true)
    setError(null)
    try {
      const [storedFiles, allFindings] = await Promise.all([
        getStoredFiles(activeAccountId).catch(() => []),
        getFindings({ accountId: activeAccountId }).catch(() => []),
      ])
      setFiles(storedFiles || [])
      setFindings(allFindings || [])
      if (selectedFile) {
        const refreshed = storedFiles.find((f) => f.id === selectedFile.id)
        setSelectedFile(refreshed || null)
      }
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [activeAccountId])

  // Filter & sort files
  const filteredFiles = useMemo(() => {
    return files
      .filter((file) => {
        if (searchQuery.trim()) {
          const q = searchQuery.toLowerCase()
          const matchName = file.name?.toLowerCase().includes(q)
          const matchMime = file.mimeType?.toLowerCase().includes(q)
          if (!matchName && !matchMime) return false
        }

        if (selectedCategory !== 'ALL') {
          const mime = (file.mimeType || '').toLowerCase()
          if (selectedCategory === 'DOCS' && !mime.includes('document') && !mime.includes('word')) return false
          if (selectedCategory === 'SHEETS' && !mime.includes('spreadsheet') && !mime.includes('sheet')) return false
          if (selectedCategory === 'SLIDES' && !mime.includes('presentation')) return false
          if (selectedCategory === 'PDF' && !mime.includes('pdf')) return false
          if (
            selectedCategory === 'MEDIA' &&
            !mime.startsWith('image/') &&
            !mime.startsWith('video/') &&
            !mime.startsWith('audio/')
          )
            return false
          if (
            selectedCategory === 'ARCHIVE' &&
            !mime.includes('zip') &&
            !mime.includes('tar') &&
            !mime.includes('rar') &&
            !mime.includes('compressed')
          )
            return false
        }

        return true
      })
      .sort((a, b) => {
        if (sortBy === 'size_desc') return (b.sizeBytes || 0) - (a.sizeBytes || 0)
        if (sortBy === 'size_asc') return (a.sizeBytes || 0) - (b.sizeBytes || 0)
        if (sortBy === 'date_desc')
          return new Date(b.modifiedTime || 0) - new Date(a.modifiedTime || 0)
        if (sortBy === 'name_asc') return (a.name || '').localeCompare(b.name || '')
        return 0
      })
  }, [files, searchQuery, selectedCategory, sortBy])

  // Map file findings
  const getFileFindings = (fileId) => {
    return findings.filter((f) => f.driveFileId === fileId)
  }

  return (
    <div className="flex h-[calc(100vh-64px)] overflow-hidden bg-slate-50/50">
      {/* Main Files Table Area */}
      <div className="flex-1 flex flex-col min-w-0 overflow-y-auto px-6 py-6 sm:px-8">
        {/* Page Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-xl font-bold text-slate-900 tracking-tight">
                Drive Explorer
              </h1>
              <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-blue-50 text-blue-700 border border-blue-200/60">
                {files.length} indexed files
              </span>
            </div>
            <p className="text-xs text-slate-500 mt-1">
              Browse metadata, hygiene status, and ownership for{' '}
              <strong className="text-slate-700">{activeAccount?.email || 'Connected Drive'}</strong>
            </p>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={loadData}
              disabled={loading}
              className="flex items-center gap-1.5 px-3 py-2 bg-white hover:bg-slate-50 text-slate-700 border border-slate-200 rounded-xl text-xs font-medium shadow-sm transition-all"
            >
              <RefreshCw size={13} className={loading ? 'animate-spin' : ''} />
              <span>Refresh</span>
            </button>
          </div>
        </div>

        {/* Search & Filter Bar */}
        <div className="bg-white rounded-2xl border border-slate-200/90 shadow-sm p-3 mb-5 flex flex-wrap items-center gap-3">
          {/* Search Input */}
          <div className="relative flex-1 min-w-[220px]">
            <Search
              size={15}
              className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400"
            />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Filter by file name or extension…"
              className="w-full pl-9 pr-3.5 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-800 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:bg-white transition-all"
            />
          </div>

          {/* Category Tabs */}
          <div className="flex items-center gap-1 overflow-x-auto py-0.5">
            {MIME_CATEGORIES.map((cat) => (
              <button
                key={cat.value}
                onClick={() => setSelectedCategory(cat.value)}
                className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all whitespace-nowrap ${
                  selectedCategory === cat.value
                    ? 'bg-blue-600 text-white shadow-sm'
                    : 'text-slate-600 hover:bg-slate-100'
                }`}
              >
                {cat.label}
              </button>
            ))}
          </div>

          {/* Sort Dropdown */}
          <div className="flex items-center gap-1.5 text-xs text-slate-500 shrink-0">
            <ArrowUpDown size={14} />
            <select
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value)}
              className="bg-slate-50 border border-slate-200 rounded-lg px-2 py-1.5 text-xs text-slate-700 focus:outline-none focus:ring-1 focus:ring-blue-500"
            >
              <option value="size_desc">Largest size</option>
              <option value="size_asc">Smallest size</option>
              <option value="date_desc">Recently modified</option>
              <option value="name_asc">Name (A-Z)</option>
            </select>
          </div>
        </div>

        {error && (
          <div className="mb-4">
            <ErrorMessage message={error} onRetry={loadData} />
          </div>
        )}

        {/* Files Table */}
        {loading ? (
          <Loading message="Loading stored files and findings…" />
        ) : filteredFiles.length === 0 ? (
          <EmptyState
            title={searchQuery ? 'No files match your search' : 'No indexed files found'}
            description={
              searchQuery
                ? 'Try adjusting your search keywords or filter category.'
                : 'Run a Drive scan to index file metadata from your Google account.'
            }
          />
        ) : (
          <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden flex-1 flex flex-col">
            <div className="overflow-x-auto flex-1">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="border-b border-slate-200 bg-slate-50/80 text-[11px] font-semibold text-slate-500 uppercase tracking-wider">
                    <th className="py-3 px-4">File Name</th>
                    <th className="py-3 px-4">Size</th>
                    <th className="py-3 px-4">Hygiene Status</th>
                    <th className="py-3 px-4">Last Modified</th>
                    <th className="py-3 px-4 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 text-xs text-slate-700">
                  {filteredFiles.map((file) => {
                    const fileFindings = getFileFindings(file.id)
                    const isSelected = selectedFile?.id === file.id

                    return (
                      <tr
                        key={file.id}
                        onClick={() => setSelectedFile(file)}
                        className={`hover:bg-slate-50/90 cursor-pointer transition-colors ${
                          isSelected ? 'bg-blue-50/60' : ''
                        }`}
                      >
                        {/* Name */}
                        <td className="py-3 px-4 max-w-[280px]">
                          <div className="flex items-center gap-3">
                            <FileIcon mimeType={file.mimeType} size="sm" />
                            <div className="min-w-0">
                              <p className="font-medium text-slate-900 truncate" title={file.name}>
                                {file.name}
                              </p>
                              <p className="text-[11px] text-slate-400 truncate">
                                {file.mimeType || 'Unknown format'}
                              </p>
                            </div>
                          </div>
                        </td>

                        {/* Size */}
                        <td className="py-3 px-4 font-mono font-medium text-slate-800 whitespace-nowrap">
                          {formatBytes(file.sizeBytes)}
                        </td>

                        {/* Hygiene Status / Badges */}
                        <td className="py-3 px-4">
                          {fileFindings.length === 0 ? (
                            <span className="inline-flex items-center gap-1 text-[11px] text-emerald-700 font-medium px-2 py-0.5 rounded-full bg-emerald-50 border border-emerald-200/60">
                              <Sparkles size={11} />
                              Healthy
                            </span>
                          ) : (
                            <div className="flex flex-wrap gap-1">
                              {fileFindings.map((f) => (
                                <span
                                  key={f.id}
                                  className={`inline-flex items-center gap-1 text-[10px] font-semibold px-2 py-0.5 rounded-full border ${getSeverityBadgeStyle(
                                    f.severity
                                  )}`}
                                >
                                  <AlertTriangle size={10} />
                                  {f.findingType?.replace(/_/g, ' ')}
                                </span>
                              ))}
                            </div>
                          )}
                        </td>

                        {/* Date */}
                        <td className="py-3 px-4 text-slate-500 whitespace-nowrap">
                          {formatDate(file.modifiedTime)}
                        </td>

                        {/* Actions */}
                        <td className="py-3 px-4 text-right whitespace-nowrap" onClick={(e) => e.stopPropagation()}>
                          <div className="flex items-center justify-end gap-2">
                            {file.webViewLink && (
                              <a
                                href={file.webViewLink}
                                target="_blank"
                                rel="noopener noreferrer"
                                title="Open in Google Drive"
                                className="p-1.5 rounded-lg hover:bg-slate-100 text-slate-500 hover:text-blue-600 transition-colors"
                              >
                                <ExternalLink size={14} />
                              </a>
                            )}
                            <button
                              onClick={() => setSelectedFile(file)}
                              className="p-1.5 rounded-lg hover:bg-slate-100 text-slate-500 hover:text-slate-900 transition-colors"
                            >
                              <ChevronRight size={14} />
                            </button>
                          </div>
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>

      {/* Detail Side Panel */}
      {selectedFile && (
        <div className="w-80 sm:w-96 shrink-0 bg-white border-l border-slate-200 shadow-lg flex flex-col h-full overflow-y-auto">
          {/* Header */}
          <div className="p-5 border-b border-slate-100 flex items-start justify-between">
            <div className="flex items-center gap-3 min-w-0">
              <FileIcon mimeType={selectedFile.mimeType} size="md" />
              <div className="min-w-0">
                <h3 className="text-sm font-bold text-slate-900 truncate" title={selectedFile.name}>
                  {selectedFile.name}
                </h3>
                <p className="text-xs text-slate-500 font-mono">
                  {formatBytes(selectedFile.sizeBytes)}
                </p>
              </div>
            </div>
            <button
              onClick={() => setSelectedFile(null)}
              className="p-1 rounded-lg hover:bg-slate-100 text-slate-400 hover:text-slate-600"
            >
              <X size={16} />
            </button>
          </div>

          {/* Body */}
          <div className="p-5 space-y-6 flex-1">
            {/* Action button */}
            {selectedFile.webViewLink && (
              <a
                href={selectedFile.webViewLink}
                target="_blank"
                rel="noopener noreferrer"
                className="w-full flex items-center justify-center gap-2 px-4 py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-xl text-xs font-semibold shadow-sm transition-colors"
              >
                <ExternalLink size={14} />
                <span>Open in Google Drive</span>
              </a>
            )}

            {/* Hygiene Findings */}
            <div>
              <p className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-2">
                Hygiene Audit
              </p>
              {getFileFindings(selectedFile.id).length === 0 ? (
                <div className="p-3 bg-emerald-50 rounded-xl border border-emerald-200/60 text-xs text-emerald-800 flex items-center gap-2">
                  <Sparkles size={14} className="text-emerald-600" />
                  <span>No hygiene issues detected for this file.</span>
                </div>
              ) : (
                <div className="space-y-2">
                  {getFileFindings(selectedFile.id).map((f) => (
                    <div
                      key={f.id}
                      className="p-3 rounded-xl border border-slate-200 bg-slate-50 text-xs space-y-1"
                    >
                      <div className="flex items-center justify-between">
                        <span className="font-semibold text-slate-900">
                          {f.findingType?.replace(/_/g, ' ')}
                        </span>
                        <span
                          className={`px-2 py-0.5 rounded-full text-[10px] font-bold border ${getSeverityBadgeStyle(
                            f.severity
                          )}`}
                        >
                          {f.severity}
                        </span>
                      </div>
                      <p className="text-slate-600 text-[11px]">{f.description || f.reason}</p>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* File Metadata */}
            <div>
              <p className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-3">
                File Details
              </p>
              <div className="space-y-2.5 text-xs">
                <div className="flex justify-between py-1.5 border-b border-slate-100">
                  <span className="text-slate-500">MIME Type</span>
                  <span className="text-slate-800 font-mono text-[11px] truncate max-w-[180px]">
                    {selectedFile.mimeType}
                  </span>
                </div>
                <div className="flex justify-between py-1.5 border-b border-slate-100">
                  <span className="text-slate-500">Google File ID</span>
                  <span className="text-slate-800 font-mono text-[11px] truncate max-w-[180px]">
                    {selectedFile.googleFileId}
                  </span>
                </div>
                <div className="flex justify-between py-1.5 border-b border-slate-100">
                  <span className="text-slate-500">MD5 Checksum</span>
                  <span className="text-slate-800 font-mono text-[11px] truncate max-w-[180px]">
                    {selectedFile.md5Checksum || 'N/A (Google Doc)'}
                  </span>
                </div>
                <div className="flex justify-between py-1.5 border-b border-slate-100">
                  <span className="text-slate-500">Shared Externally</span>
                  <span className="text-slate-800 font-medium">
                    {selectedFile.shared ? 'Yes' : 'No'}
                  </span>
                </div>
                <div className="flex justify-between py-1.5 border-b border-slate-100">
                  <span className="text-slate-500">In Trash</span>
                  <span className="text-slate-800 font-medium">
                    {selectedFile.trashed ? 'Yes' : 'No'}
                  </span>
                </div>
                <div className="flex justify-between py-1.5 border-b border-slate-100">
                  <span className="text-slate-500">Modified Date</span>
                  <span className="text-slate-800">
                    {formatDateTime(selectedFile.modifiedTime)}
                  </span>
                </div>
                <div className="flex justify-between py-1.5 border-b border-slate-100">
                  <span className="text-slate-500">Created Date</span>
                  <span className="text-slate-800">
                    {formatDateTime(selectedFile.createdTime)}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
