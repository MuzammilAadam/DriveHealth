import { formatDateTime } from '../../services/driveHealthApi'
import { RefreshCw } from 'lucide-react'

export default function ScanStatus({ lastScan, scanning, onScan }) {
  return (
    <div className="flex items-center gap-3 text-sm text-[#5f6368]">
      {scanning ? (
        <>
          <div className="w-4 h-4 border-2 border-[#e0e0e0] border-t-[#1a73e8] rounded-full animate-spin" />
          <span>Scanning your Drive…</span>
        </>
      ) : lastScan ? (
        <>
          <span>
            Last scan completed on{' '}
            <span className="text-[#202124] font-medium">
              {formatDateTime(lastScan.completedAt || lastScan.startedAt)}
            </span>
          </span>
          <button
            onClick={onScan}
            className="flex items-center gap-1.5 text-[#1a73e8] hover:underline font-medium ml-1"
          >
            <RefreshCw size={14} />
            Scan again
          </button>
        </>
      ) : (
        <span>No scans yet. Run your first scan.</span>
      )}
    </div>
  )
}
