import { useState, useEffect } from 'react'
import { Files, Copy, Clock, HardDrive, Share2, ScanLine } from 'lucide-react'
import FileTable from '../components/files/FileTable'
import FileDetails from '../components/files/FileDetails'
import SummaryCard from '../components/dashboard/SummaryCard'
import Loading from '../components/common/Loading'
import ErrorMessage from '../components/common/ErrorMessage'
import {
  getDashboardSummary,
  getStoredFiles,
  getFindings,
  getScans,
  scanDrive,
  formatDateTime,
} from '../services/driveHealthApi'

export default function ScanResults() {
  const [summary, setSummary] = useState(null)
  const [files, setFiles] = useState([])
  const [issues, setIssues] = useState([])
  const [lastScan, setLastScan] = useState(null)
  const [selectedFile, setSelectedFile] = useState(null)
  const [loading, setLoading] = useState(true)
  const [scanning, setScanning] = useState(false)
  const [error, setError] = useState(null)

  const fetchData = async () => {
    setLoading(true)
    setError(null)
    try {
      const [sum, storedFiles, findings, scans] = await Promise.all([
        getDashboardSummary().catch(() => null),
        getStoredFiles(),
        getFindings().catch(() => []),
        getScans().catch(() => []),
      ])
      setSummary(sum)
      setFiles(storedFiles || [])
      setIssues(findings || [])
      if (scans && scans.length > 0) setLastScan(scans[0])
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
    } catch (e) {
      setError(e.message)
    } finally {
      setScanning(false)
    }
  }

  const summaryCards = summary
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
      {/* Main */}
      <div className="flex-1 overflow-y-auto px-8 py-6">
        {/* Header */}
        <div className="flex items-start justify-between mb-1">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-full bg-[#e8f0fe] flex items-center justify-center">
              <ScanLine size={18} className="text-[#1a73e8]" />
            </div>
            <h1 className="text-xl font-medium text-[#202124]">Scan Results</h1>
          </div>
          <button
            onClick={handleScan}
            disabled={scanning}
            className="flex items-center gap-2 px-4 py-2 bg-[#1a73e8] hover:bg-[#1557b0] text-white text-sm font-medium rounded-full transition-colors disabled:opacity-60"
          >
            {scanning ? (
              <>
                <div className="w-3.5 h-3.5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                Scanning…
              </>
            ) : (
              <>
                <ScanLine size={15} />
                Scan now
              </>
            )}
          </button>
        </div>

        {lastScan && (
          <p className="text-sm text-[#5f6368] mb-5 ml-11">
            Last scan completed on{' '}
            <span className="text-[#202124]">
              {formatDateTime(lastScan.completedAt || lastScan.startedAt)}
            </span>
          </p>
        )}

        {error && (
          <div className="mb-5">
            <ErrorMessage message={error} onRetry={fetchData} />
          </div>
        )}

        {loading ? (
          <Loading message="Loading scan results…" />
        ) : (
          <>
            {/* Summary cards */}
            {summaryCards.length > 0 && (
              <div className="grid grid-cols-5 gap-3 mb-8">
                {summaryCards.map((card) => (
                  <SummaryCard
                    key={card.label}
                    icon={card.icon}
                    label={card.label}
                    value={card.value}
                    color={card.color}
                  />
                ))}
              </div>
            )}

            {/* Files table */}
            <FileTable
              files={files}
              issues={issues}
              onFileClick={setSelectedFile}
            />
          </>
        )}
      </div>

      {/* File details panel */}
      {selectedFile && (
        <FileDetails
          file={selectedFile}
          issues={issues}
          onClose={() => setSelectedFile(null)}
        />
      )}
    </div>
  )
}
