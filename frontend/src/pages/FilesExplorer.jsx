import { useState, useEffect, useMemo, useRef } from 'react'
import { useAuth } from '../context/AuthContext'
import {
  Search,
  ExternalLink,
  ArrowUpDown,
  RefreshCw,
  Sparkles,
  ChevronRight,
  X,
  Trash2,
  Upload,
  AlertTriangle,
  CheckCircle2,
  CloudUpload,
  FolderPlus,
  HardDrive,
  Folder,
} from 'lucide-react'
import Loading from '../components/common/Loading'
import ErrorMessage from '../components/common/ErrorMessage'
import EmptyState from '../components/common/EmptyState'
import FileIcon from '../components/files/FileIcon'
import {
  getStoredFiles,
  getFindings,
  deleteFile,
  uploadFile,
  createFolder,
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
  const [sortBy, setSortBy] = useState('size_desc')
  const [selectedFile, setSelectedFile] = useState(null)

  // Folder Navigation State
  // folderPath: array of { googleFileId: string | null, name: string }
  const [folderPath, setFolderPath] = useState([
    { googleFileId: null, name: 'My Drive' },
  ])

  // Current folder is the last element in folderPath
  const currentFolder = useMemo(() => {
    return folderPath[folderPath.length - 1]
  }, [folderPath])

  // Delete state
  const [deletingFileId, setDeletingFileId] = useState(null)
  const [deleteConfirm, setDeleteConfirm] = useState(null) // file object
  const [deleteError, setDeleteError] = useState(null)
  const [deleteSuccess, setDeleteSuccess] = useState(null)

  // Upload state
  const [uploading, setUploading] = useState(false)
  const [uploadError, setUploadError] = useState(null)
  const [uploadSuccess, setUploadSuccess] = useState(null)
  const [showUploadModal, setShowUploadModal] = useState(false)
  const [uploadDragOver, setUploadDragOver] = useState(false)
  const fileInputRef = useRef(null)

  // New Folder state
  const [showNewFolderModal, setShowNewFolderModal] = useState(false)
  const [newFolderName, setNewFolderName] = useState('')
  const [creatingFolder, setCreatingFolder] = useState(false)
  const [createFolderError, setCreateFolderError] = useState(null)

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
        const refreshed = (storedFiles || []).find((f) => f.id === selectedFile.id)
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

  // Set of all known folder IDs in the dataset
  const allFolderIds = useMemo(() => {
    const ids = new Set()
    files.forEach((f) => {
      if (f.mimeType === 'application/vnd.google-apps.folder' && f.googleFileId) {
        ids.add(f.googleFileId)
      }
    })
    return ids
  }, [files])

  // Hierarchical Scope Filtering & Sorting
  const filteredFiles = useMemo(() => {
    const isSearchActive = searchQuery.trim() !== ''

    return files
      .filter((file) => {
        // Search Filter
        if (isSearchActive) {
          const q = searchQuery.toLowerCase()
          const matchName = file.name?.toLowerCase().includes(q)
          const matchMime = file.mimeType?.toLowerCase().includes(q)
          if (!matchName && !matchMime) return false
        } else {
          // Hierarchy Filter (when not searching)
          if (currentFolder.googleFileId === null) {
            // At ROOT: show items whose parentId is null, empty, 'root', or not matching any folder in our DB
            const isTopLevel =
              !file.parentId ||
              file.parentId === 'root' ||
              !allFolderIds.has(file.parentId)
            if (!isTopLevel) return false
          } else {
            // Inside Folder: show items whose parentId matches currentFolder.googleFileId
            if (file.parentId !== currentFolder.googleFileId) return false
          }
        }

        // Category Filter
        if (selectedCategory !== 'ALL') {
          const mime = (file.mimeType || '').toLowerCase()
          // Folders are not hidden if we are displaying all, but for specific file types:
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
        const isAFolder = a.mimeType === 'application/vnd.google-apps.folder'
        const isBFolder = b.mimeType === 'application/vnd.google-apps.folder'

        // Requirement 1: Show folders first, then files
        if (isAFolder && !isBFolder) return -1
        if (!isAFolder && isBFolder) return 1

        // Sort among folders or among files according to selected criterion
        if (sortBy === 'size_desc') return (b.sizeBytes || 0) - (a.sizeBytes || 0)
        if (sortBy === 'size_asc') return (a.sizeBytes || 0) - (b.sizeBytes || 0)
        if (sortBy === 'date_desc')
          return new Date(b.modifiedTime || 0) - new Date(a.modifiedTime || 0)
        if (sortBy === 'name_asc') return (a.name || '').localeCompare(b.name || '')
        return 0
      })
  }, [files, currentFolder, allFolderIds, searchQuery, selectedCategory, sortBy])

  const getFileFindings = (fileId) => {
    return findings.filter((f) => f.driveFileId === fileId)
  }

  // ── Navigation handlers ──────────────────────────────────────────────────────
  const handleOpenFolder = (folderFile) => {
    setFolderPath((prev) => [
      ...prev,
      { googleFileId: folderFile.googleFileId, name: folderFile.name },
    ])
    setSelectedFile(null)
  }

  const handleBreadcrumbClick = (index) => {
    setFolderPath((prev) => prev.slice(0, index + 1))
    setSelectedFile(null)
  }

  // ── Delete handlers ──────────────────────────────────────────────────────────
  const handleDeleteClick = (e, file) => {
    e.stopPropagation()
    setDeleteError(null)
    setDeleteSuccess(null)
    setDeleteConfirm(file)
  }

  const handleConfirmDelete = async () => {
    if (!deleteConfirm) return
    setDeletingFileId(deleteConfirm.googleFileId)
    setDeleteError(null)
    try {
      await deleteFile(deleteConfirm.googleFileId, activeAccountId)
      setDeleteSuccess(`"${deleteConfirm.name}" deleted from Google Drive.`)
      if (selectedFile?.googleFileId === deleteConfirm.googleFileId) setSelectedFile(null)
      setFiles((prev) => prev.filter((f) => f.googleFileId !== deleteConfirm.googleFileId))
      setDeleteConfirm(null)
      setTimeout(() => setDeleteSuccess(null), 4000)
    } catch (err) {
      const serverMsg = err.response?.data?.message
      setDeleteError(serverMsg || err.message || 'Failed to delete item from Google Drive.')
    } finally {
      setDeletingFileId(null)
    }
  }

  // ── Upload handlers ──────────────────────────────────────────────────────────
  const handleFileDrop = async (e) => {
    e.preventDefault()
    setUploadDragOver(false)
    const droppedFile = e.dataTransfer.files[0]
    if (droppedFile) await doUpload(droppedFile)
  }

  const handleFileInputChange = async (e) => {
    const picked = e.target.files[0]
    if (picked) await doUpload(picked)
    e.target.value = ''
  }

  const doUpload = async (file) => {
    setUploading(true)
    setUploadError(null)
    setUploadSuccess(null)
    try {
      // Upload into currentFolder
      const newFile = await uploadFile(file, activeAccountId, currentFolder.googleFileId)
      setFiles((prev) => [newFile, ...prev])
      setUploadSuccess(`"${file.name}" uploaded into ${currentFolder.name} successfully!`)
      setShowUploadModal(false)
      setTimeout(() => setUploadSuccess(null), 4000)
    } catch (err) {
      setUploadError(err.message || 'Upload failed.')
    } finally {
      setUploading(false)
    }
  }

  // ── Create Folder handler ────────────────────────────────────────────────────
  const handleCreateFolderSubmit = async (e) => {
    e.preventDefault()
    if (!newFolderName.trim()) return
    setCreatingFolder(true)
    setCreateFolderError(null)
    try {
      const newFolderObj = await createFolder(
        newFolderName.trim(),
        activeAccountId,
        currentFolder.googleFileId
      )
      setFiles((prev) => [newFolderObj, ...prev])
      setNewFolderName('')
      setShowNewFolderModal(false)
      setUploadSuccess(`Folder "${newFolderObj.name}" created successfully!`)
      setTimeout(() => setUploadSuccess(null), 4000)
    } catch (err) {
      setCreateFolderError(err.message || 'Failed to create folder.')
    } finally {
      setCreatingFolder(false)
    }
  }

  return (
    <div className="flex h-[calc(100vh-64px)] overflow-hidden bg-slate-50/50">
      {/* Delete Confirm Modal */}
      {deleteConfirm && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-2xl p-6 max-w-md w-full">
            <div className="flex items-center gap-3 mb-4">
              <div className="w-10 h-10 rounded-full bg-red-100 flex items-center justify-center flex-shrink-0">
                <Trash2 size={18} className="text-red-600" />
              </div>
              <div>
                <h3 className="text-base font-bold text-slate-900">
                  Delete {deleteConfirm.mimeType === 'application/vnd.google-apps.folder' ? 'Folder' : 'File'} from Google Drive?
                </h3>
                <p className="text-xs text-slate-500 mt-0.5">This action cannot be undone.</p>
              </div>
            </div>
            <div className="bg-slate-50 rounded-xl px-4 py-3 mb-4 border border-slate-200">
              <p className="text-sm font-medium text-slate-800 truncate">{deleteConfirm.name}</p>
              <p className="text-xs text-slate-500 mt-0.5">
                {deleteConfirm.mimeType === 'application/vnd.google-apps.folder'
                  ? 'Folder'
                  : formatBytes(deleteConfirm.sizeBytes)}
              </p>
            </div>
            <p className="text-xs text-slate-600 mb-5">
              This will permanently delete the item from your Google Drive account and remove it from the local index.
            </p>
            {deleteError && (
              <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-xl text-xs text-red-700">{deleteError}</div>
            )}
            <div className="flex gap-3">
              <button
                onClick={() => setDeleteConfirm(null)}
                disabled={!!deletingFileId}
                className="flex-1 px-4 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-xl text-sm font-medium transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={handleConfirmDelete}
                disabled={!!deletingFileId}
                className="flex-1 px-4 py-2.5 bg-red-600 hover:bg-red-700 text-white rounded-xl text-sm font-semibold transition-colors flex items-center justify-center gap-2"
              >
                {deletingFileId ? (
                  <RefreshCw size={14} className="animate-spin" />
                ) : (
                  <Trash2 size={14} />
                )}
                {deletingFileId ? 'Deleting…' : 'Delete from Drive'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* New Folder Modal */}
      {showNewFolderModal && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-2xl p-6 max-w-md w-full">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-full bg-slate-100 flex items-center justify-center text-slate-700">
                  <FolderPlus size={18} />
                </div>
                <div>
                  <h3 className="text-base font-bold text-slate-900">New Folder</h3>
                  <p className="text-xs text-slate-500">Creating in: {currentFolder.name}</p>
                </div>
              </div>
              <button
                onClick={() => setShowNewFolderModal(false)}
                disabled={creatingFolder}
                className="p-1.5 rounded-lg hover:bg-slate-100 text-slate-400 hover:text-slate-600"
              >
                <X size={16} />
              </button>
            </div>

            {createFolderError && (
              <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-xl text-xs text-red-700">{createFolderError}</div>
            )}

            <form onSubmit={handleCreateFolderSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                  Folder Name
                </label>
                <input
                  type="text"
                  value={newFolderName}
                  onChange={(e) => setNewFolderName(e.target.value)}
                  placeholder="Untitled folder"
                  autoFocus
                  className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:bg-white"
                />
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setShowNewFolderModal(false)}
                  disabled={creatingFolder}
                  className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-xl text-xs font-semibold transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={creatingFolder || !newFolderName.trim()}
                  className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-xl text-xs font-semibold shadow-sm transition-colors flex items-center gap-1.5 disabled:opacity-50"
                >
                  {creatingFolder && <RefreshCw size={13} className="animate-spin" />}
                  <span>{creatingFolder ? 'Creating…' : 'Create Folder'}</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Upload Modal */}
      {showUploadModal && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-2xl p-6 max-w-md w-full">
            <div className="flex items-center justify-between mb-5">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center">
                  <CloudUpload size={18} className="text-blue-600" />
                </div>
                <div>
                  <h3 className="text-base font-bold text-slate-900">Upload to Google Drive</h3>
                  <p className="text-xs text-slate-500">File will be saved inside: <strong className="text-slate-700">{currentFolder.name}</strong></p>
                </div>
              </div>
              <button
                onClick={() => setShowUploadModal(false)}
                disabled={uploading}
                className="p-1.5 rounded-lg hover:bg-slate-100 text-slate-400 hover:text-slate-600"
              >
                <X size={16} />
              </button>
            </div>

            {uploadError && (
              <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-xl text-xs text-red-700">{uploadError}</div>
            )}

            <div
              onDragOver={(e) => { e.preventDefault(); setUploadDragOver(true) }}
              onDragLeave={() => setUploadDragOver(false)}
              onDrop={handleFileDrop}
              onClick={() => !uploading && fileInputRef.current?.click()}
              className={`border-2 border-dashed rounded-2xl p-10 text-center cursor-pointer transition-all ${
                uploadDragOver
                  ? 'border-blue-400 bg-blue-50'
                  : 'border-slate-300 hover:border-blue-300 hover:bg-slate-50'
              } ${uploading ? 'opacity-60 cursor-not-allowed' : ''}`}
            >
              <input
                ref={fileInputRef}
                type="file"
                className="hidden"
                onChange={handleFileInputChange}
                disabled={uploading}
              />
              {uploading ? (
                <div className="flex flex-col items-center gap-3">
                  <RefreshCw size={32} className="animate-spin text-blue-500" />
                  <p className="text-sm font-medium text-slate-600">Uploading to Google Drive…</p>
                </div>
              ) : (
                <div className="flex flex-col items-center gap-3">
                  <Upload size={32} className="text-slate-400" />
                  <div>
                    <p className="text-sm font-semibold text-slate-700">Drop a file here or click to browse</p>
                    <p className="text-xs text-slate-400 mt-1">Target folder: {currentFolder.name}</p>
                  </div>
                </div>
              )}
            </div>

            <p className="text-xs text-slate-500 text-center mt-3">
              Uploading to:{' '}
              <strong className="text-slate-700">{activeAccount?.email || 'Connected Drive'}</strong>
            </p>
          </div>
        </div>
      )}

      {/* Main Files Table Area */}
      <div className="flex-1 flex flex-col min-w-0 overflow-y-auto px-6 py-6 sm:px-8">
        {/* Page Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-4">
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-xl font-bold text-slate-900 tracking-tight">Files Explorer</h1>
              <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-blue-50 text-blue-700 border border-blue-200/60">
                {filteredFiles.length} items
              </span>
            </div>
            <p className="text-xs text-slate-500 mt-1">
              Browse Google Drive hierarchy for{' '}
              <strong className="text-slate-700">{activeAccount?.email || 'Connected Drive'}</strong>
            </p>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={() => { setCreateFolderError(null); setNewFolderName(''); setShowNewFolderModal(true) }}
              className="flex items-center gap-1.5 px-3 py-2 bg-white hover:bg-slate-50 text-slate-700 border border-slate-200 rounded-xl text-xs font-semibold shadow-xs transition-all"
            >
              <FolderPlus size={14} className="text-slate-600" />
              <span>New Folder</span>
            </button>
            <button
              onClick={() => { setUploadError(null); setShowUploadModal(true) }}
              className="flex items-center gap-1.5 px-3 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-xl text-xs font-semibold shadow-xs transition-all"
            >
              <Upload size={13} />
              <span>Upload File</span>
            </button>
            <button
              onClick={loadData}
              disabled={loading}
              className="flex items-center gap-1.5 px-3 py-2 bg-white hover:bg-slate-50 text-slate-700 border border-slate-200 rounded-xl text-xs font-medium shadow-xs transition-all"
            >
              <RefreshCw size={13} className={loading ? 'animate-spin' : ''} />
              <span>Refresh</span>
            </button>
          </div>
        </div>

        {/* Requirement 3: BREADCRUMB NAVIGATION */}
        <div className="bg-white rounded-xl border border-slate-200/90 shadow-xs px-4 py-2.5 mb-4 flex items-center gap-1 overflow-x-auto text-xs font-medium">
          <HardDrive size={15} className="text-blue-600 mr-1 shrink-0" />
          {folderPath.map((item, index) => {
            const isLast = index === folderPath.length - 1
            return (
              <div key={item.googleFileId || 'root'} className="flex items-center gap-1 shrink-0">
                {index > 0 && <ChevronRight size={13} className="text-slate-400 shrink-0" />}
                <button
                  onClick={() => handleBreadcrumbClick(index)}
                  className={`px-2.5 py-1 rounded-lg text-xs font-semibold transition-all ${
                    isLast
                      ? 'bg-blue-50 text-blue-700 font-bold border border-blue-200/60'
                      : 'hover:bg-slate-100 text-slate-600 hover:text-slate-900'
                  }`}
                >
                  {item.name}
                </button>
              </div>
            )
          })}
        </div>

        {/* Success Banners */}
        {deleteSuccess && (
          <div className="mb-4 flex items-center gap-2 p-3 bg-emerald-50 border border-emerald-200 rounded-xl text-xs text-emerald-700 font-medium">
            <CheckCircle2 size={14} />
            {deleteSuccess}
          </div>
        )}
        {uploadSuccess && (
          <div className="mb-4 flex items-center gap-2 p-3 bg-emerald-50 border border-emerald-200 rounded-xl text-xs text-emerald-700 font-medium">
            <CheckCircle2 size={14} />
            {uploadSuccess}
          </div>
        )}

        {/* Search & Filter Bar */}
        <div className="bg-white rounded-2xl border border-slate-200/90 shadow-xs p-3 mb-5 flex flex-wrap items-center gap-3">
          {/* Search Input */}
          <div className="relative flex-1 min-w-[220px]">
            <Search size={15} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search by file name or format…"
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
                    ? 'bg-blue-600 text-white shadow-xs'
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
          <Loading message="Loading Google Drive items and findings…" />
        ) : filteredFiles.length === 0 ? (
          <EmptyState
            title={searchQuery ? 'No items match your search' : 'This folder is empty'}
            description={
              searchQuery
                ? 'Try adjusting your search keywords or filter category.'
                : 'Upload a file or create a folder inside this location.'
            }
          />
        ) : (
          <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden flex-1 flex flex-col">
            <div className="overflow-x-auto flex-1">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="border-b border-slate-200 bg-slate-50/80 text-[11px] font-semibold text-slate-500 uppercase tracking-wider">
                    <th className="py-3 px-4">Name</th>
                    <th className="py-3 px-4">Size</th>
                    <th className="py-3 px-4">Hygiene Status</th>
                    <th className="py-3 px-4">Last Modified</th>
                    <th className="py-3 px-4 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 text-xs text-slate-700">
                  {filteredFiles.map((file) => {
                    const isFolder = file.mimeType === 'application/vnd.google-apps.folder'
                    const fileFindings = getFileFindings(file.id)
                    const isSelected = selectedFile?.id === file.id
                    const isDeleting = deletingFileId === file.googleFileId

                    return (
                      <tr
                        key={file.id}
                        onClick={() => {
                          if (isFolder) {
                            handleOpenFolder(file)
                          } else {
                            setSelectedFile(file)
                          }
                        }}
                        className={`hover:bg-slate-50/90 cursor-pointer transition-colors ${
                          isSelected ? 'bg-blue-50/60' : ''
                        } ${isDeleting ? 'opacity-50' : ''}`}
                      >
                        {/* Name Column */}
                        <td className="py-3 px-4 max-w-[280px]">
                          <div className="flex items-center gap-3">
                            <FileIcon mimeType={file.mimeType} size="sm" />
                            <div className="min-w-0">
                              <p className="font-semibold text-slate-900 truncate flex items-center gap-1.5" title={file.name}>
                                {file.name}
                              </p>
                              <p className="text-[11px] text-slate-400 truncate">
                                {isFolder ? 'Folder' : file.mimeType || 'File'}
                              </p>
                            </div>
                          </div>
                        </td>

                        {/* Size Column */}
                        <td className="py-3 px-4 font-mono font-medium text-slate-600 whitespace-nowrap">
                          {isFolder ? '—' : formatBytes(file.sizeBytes)}
                        </td>

                        {/* Hygiene Status Column */}
                        <td className="py-3 px-4">
                          {isFolder ? (
                            <span className="inline-flex items-center gap-1 text-[11px] text-slate-500 font-medium px-2 py-0.5 rounded-full bg-slate-100 border border-slate-200/60">
                              <Folder size={11} />
                              Folder
                            </span>
                          ) : fileFindings.length === 0 ? (
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

                        {/* Last Modified Column */}
                        <td className="py-3 px-4 text-slate-500 whitespace-nowrap">
                          {formatDate(file.modifiedTime)}
                        </td>

                        {/* Actions Column */}
                        <td
                          className="py-3 px-4 text-right whitespace-nowrap"
                          onClick={(e) => e.stopPropagation()}
                        >
                          <div className="flex items-center justify-end gap-1.5">
                            {file.webViewLink && (
                              <a
                                href={file.webViewLink}
                                target="_blank"
                                rel="noopener noreferrer"
                                title="Open in Google Drive"
                                className="p-1.5 rounded-lg hover:bg-slate-100 text-slate-500 hover:text-blue-600 transition-colors"
                              >
                                <ExternalLink size={13} />
                              </a>
                            )}
                            <button
                              onClick={(e) => handleDeleteClick(e, file)}
                              disabled={isDeleting}
                              title="Delete from Google Drive"
                              className="p-1.5 rounded-lg hover:bg-red-50 text-slate-400 hover:text-red-600 transition-colors"
                            >
                              {isDeleting ? (
                                <RefreshCw size={13} className="animate-spin" />
                              ) : (
                                <Trash2 size={13} />
                              )}
                            </button>
                            {isFolder ? (
                              <button
                                onClick={() => handleOpenFolder(file)}
                                title="Open folder"
                                className="p-1.5 rounded-lg hover:bg-blue-50 text-slate-500 hover:text-blue-700 transition-colors"
                              >
                                <ChevronRight size={14} />
                              </button>
                            ) : (
                              <button
                                onClick={() => setSelectedFile(file)}
                                title="View details"
                                className="p-1.5 rounded-lg hover:bg-slate-100 text-slate-500 hover:text-slate-900 transition-colors"
                              >
                                <ChevronRight size={13} />
                              </button>
                            )}
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
                  {selectedFile.mimeType === 'application/vnd.google-apps.folder'
                    ? 'Folder'
                    : formatBytes(selectedFile.sizeBytes)}
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
          <div className="p-5 space-y-4 flex-1">
            {/* Action buttons */}
            <div className="flex flex-col gap-2">
              {selectedFile.webViewLink && (
                <a
                  href={selectedFile.webViewLink}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="w-full flex items-center justify-center gap-2 px-4 py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-xl text-xs font-semibold shadow-xs transition-colors"
                >
                  <ExternalLink size={14} />
                  <span>Open in Google Drive</span>
                </a>
              )}
              <button
                onClick={(e) => handleDeleteClick(e, selectedFile)}
                className="w-full flex items-center justify-center gap-2 px-4 py-2.5 bg-red-50 hover:bg-red-100 text-red-600 border border-red-200 rounded-xl text-xs font-semibold transition-colors"
              >
                <Trash2 size={14} />
                <span>Delete from Drive</span>
              </button>
            </div>

            {/* Hygiene Findings */}
            <div>
              <p className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-2">
                Hygiene Audit
              </p>
              {getFileFindings(selectedFile.id).length === 0 ? (
                <div className="p-3 bg-emerald-50 rounded-xl border border-emerald-200/60 text-xs text-emerald-800 flex items-center gap-2">
                  <Sparkles size={14} className="text-emerald-600" />
                  <span>No hygiene issues detected for this item.</span>
                </div>
              ) : (
                <div className="space-y-2">
                  {getFileFindings(selectedFile.id).map((f) => (
                    <div key={f.id} className="p-3 rounded-xl border border-slate-200 bg-slate-50 text-xs space-y-1">
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
                Item Details
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
                  <span className="text-slate-500">Parent Folder ID</span>
                  <span className="text-slate-800 font-mono text-[11px] truncate max-w-[180px]">
                    {selectedFile.parentId || 'root'}
                  </span>
                </div>
                <div className="flex justify-between py-1.5 border-b border-slate-100">
                  <span className="text-slate-500">MD5 Checksum</span>
                  <span className="text-slate-800 font-mono text-[11px] truncate max-w-[180px]">
                    {selectedFile.md5Checksum || 'N/A'}
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
                <div className="flex justify-between py-1.5 border-b border-slate-100">
                  <span className="text-slate-500">Owner</span>
                  <span className="text-slate-800 truncate max-w-[160px]">
                    {selectedFile.ownerEmail || 'Unknown'}
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
