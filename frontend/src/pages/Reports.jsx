import { useState, useEffect } from 'react'
import { Copy, Clock, HardDrive, Share2, ExternalLink, ChevronDown, ChevronUp } from 'lucide-react'
import Loading from '../components/common/Loading'
import ErrorMessage from '../components/common/ErrorMessage'
import EmptyState from '../components/common/EmptyState'
import FileIcon from '../components/files/FileIcon'
import IssueBadge from '../components/files/IssueBadge'
import {
  getDuplicates,
  getOldFiles,
  getLargeFiles,
  getExternalShares,
  formatBytes,
  formatDate,
} from '../services/driveHealthApi'

function ReportSection({ icon, title, color, count, description, children, defaultOpen = false }) {
  const [open, setOpen] = useState(defaultOpen)

  return (
    <div className="border border-[#e0e0e0] rounded-lg overflow-hidden mb-4">
      <button
        onClick={() => setOpen((o) => !o)}
        className="flex items-center justify-between w-full px-5 py-4 bg-white hover:bg-[#f8f9fa] transition-colors"
      >
        <div className="flex items-center gap-3">
          <div
            className="w-8 h-8 rounded-lg flex items-center justify-center"
            style={{ backgroundColor: `${color}18`, color }}
          >
            {icon}
          </div>
          <span className="text-sm font-medium text-[#202124]">{title}</span>
          <span
            className="px-2 py-0.5 rounded-full text-xs font-medium"
            style={{ backgroundColor: `${color}18`, color }}
          >
            {count}
          </span>
        </div>
        {open ? (
          <ChevronUp size={18} className="text-[#5f6368]" />
        ) : (
          <ChevronDown size={18} className="text-[#5f6368]" />
        )}
      </button>

      {open && (
        <div className="border-t border-[#e0e0e0]">
          {description && (
            <p className="px-5 py-3 text-xs text-[#5f6368] bg-[#f8f9fa]">{description}</p>
          )}
          {children}
        </div>
      )}
    </div>
  )
}

function FileListItem({ name, mimeType, detail, webUrl }) {
  return (
    <div className="flex items-center gap-3 px-5 py-2.5 border-b border-[#e0e0e0] last:border-0 hover:bg-[#f8f9fa] group">
      <FileIcon mimeType={mimeType} size="sm" />
      <div className="flex-1 min-w-0">
        <p className="text-sm text-[#202124] truncate">{name}</p>
        {detail && <p className="text-xs text-[#5f6368]">{detail}</p>}
      </div>
      {webUrl && (
        <a
          href={webUrl}
          target="_blank"
          rel="noopener noreferrer"
          className="opacity-0 group-hover:opacity-100 flex items-center gap-1 text-xs text-[#1a73e8] hover:underline shrink-0 transition-opacity"
        >
          <ExternalLink size={13} />
          Open in Drive
        </a>
      )}
    </div>
  )
}

export default function Reports() {
  const [duplicates, setDuplicates] = useState(null)
  const [oldFiles, setOldFiles] = useState(null)
  const [largeFiles, setLargeFiles] = useState(null)
  const [externalShares, setExternalShares] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const fetchData = async () => {
    setLoading(true)
    setError(null)
    try {
      const [dups, old, large, shares] = await Promise.all([
        getDuplicates().catch(() => null),
        getOldFiles().catch(() => null),
        getLargeFiles().catch(() => null),
        getExternalShares().catch(() => null),
      ])
      setDuplicates(dups)
      setOldFiles(old)
      setLargeFiles(large)
      setExternalShares(shares)
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchData() }, [])

  if (loading) return <div className="px-8 py-6"><Loading message="Loading reports…" /></div>

  return (
    <div className="px-8 py-6">
      <h1 className="text-xl font-medium text-[#202124] mb-1">Reports</h1>
      <p className="text-sm text-[#5f6368] mb-6">
        Hygiene analysis of your Google Drive files.
      </p>

      {error && (
        <div className="mb-5">
          <ErrorMessage message={error} onRetry={fetchData} />
        </div>
      )}

      {/* Duplicates */}
      <ReportSection
        icon={<Copy size={18} />}
        title="Duplicate Files"
        color="#EA4335"
        count={duplicates?.totalDuplicateFiles ?? 0}
        description="Files with identical content (same MD5 checksum). Review and remove unneeded copies."
        defaultOpen
      >
        {!duplicates?.groups?.length ? (
          <EmptyState title="No duplicates found" />
        ) : (
          duplicates.groups.map((group, gi) => (
            <div key={gi} className="border-b border-[#e0e0e0] last:border-0">
              <div className="px-5 py-2 bg-[#fef9e7]">
                <span className="text-xs text-[#5f6368]">
                  {group.files?.length} copies · {formatBytes(group.totalGroupSizeBytes)} total
                </span>
              </div>
              {group.files?.map((f, fi) => (
                <FileListItem
                  key={fi}
                  name={f.name}
                  mimeType={f.mimeType}
                  detail={`${formatBytes(f.size)} · ${formatDate(f.modifiedTime)}`}
                  webUrl={f.webUrl}
                />
              ))}
            </div>
          ))
        )}
      </ReportSection>

      {/* Old Files */}
      <ReportSection
        icon={<Clock size={18} />}
        title="Old Files"
        color="#F4B400"
        count={oldFiles?.totalOldFiles ?? 0}
        description={`Files not modified for more than ${oldFiles?.yearsThreshold ?? 2} years.`}
      >
        {!oldFiles?.files?.length ? (
          <EmptyState title="No old files found" />
        ) : (
          oldFiles.files.map((f, i) => (
            <FileListItem
              key={i}
              name={f.name}
              mimeType={f.mimeType}
              detail={`${f.daysSinceModified} days since last modification · ${formatBytes(f.size)}`}
              webUrl={f.webUrl}
            />
          ))
        )}
      </ReportSection>

      {/* Large Files */}
      <ReportSection
        icon={<HardDrive size={18} />}
        title="Large Files"
        color="#1A73E8"
        count={largeFiles?.totalLargeFiles ?? 0}
        description={`Files exceeding ${formatBytes(largeFiles?.thresholdBytes ?? 524288000)} storage threshold.`}
      >
        {!largeFiles?.files?.length ? (
          <EmptyState title="No large files found" />
        ) : (
          largeFiles.files.map((f, i) => (
            <FileListItem
              key={i}
              name={f.name}
              mimeType={f.mimeType}
              detail={`${formatBytes(f.size)} · ${formatDate(f.modifiedTime)}`}
              webUrl={f.webUrl}
            />
          ))
        )}
      </ReportSection>

      {/* External Sharing */}
      <ReportSection
        icon={<Share2 size={18} />}
        title="External Sharing"
        color="#0F9D58"
        count={(externalShares?.externalShareCount ?? 0) + (externalShares?.publicFileCount ?? 0)}
        description="Files shared with users outside your domain or accessible to anyone with the link."
      >
        {!externalShares?.externalShares?.length && !externalShares?.publicFiles?.length ? (
          <EmptyState title="No external sharing found" />
        ) : (
          <>
            {externalShares?.publicFiles?.map((f, i) => (
              <div key={`pub-${i}`} className="flex items-center gap-3 px-5 py-2.5 border-b border-[#e0e0e0] last:border-0 hover:bg-[#f8f9fa] group">
                <FileIcon mimeType={f.mimeType} size="sm" />
                <div className="flex-1 min-w-0">
                  <p className="text-sm text-[#202124] truncate">{f.name}</p>
                  <p className="text-xs text-[#5f6368]">Public · anyone with link</p>
                </div>
                <IssueBadge type="PUBLIC_FILE" />
                {f.webUrl && (
                  <a href={f.webUrl} target="_blank" rel="noopener noreferrer"
                    className="opacity-0 group-hover:opacity-100 text-xs text-[#1a73e8] hover:underline transition-opacity flex items-center gap-1">
                    <ExternalLink size={13} /> Open
                  </a>
                )}
              </div>
            ))}
            {externalShares?.externalShares?.map((share, i) => (
              <div key={`ext-${i}`} className="flex items-center gap-3 px-5 py-2.5 border-b border-[#e0e0e0] last:border-0 hover:bg-[#f8f9fa] group">
                <FileIcon mimeType={share.mimeType} size="sm" />
                <div className="flex-1 min-w-0">
                  <p className="text-sm text-[#202124] truncate">{share.fileName}</p>
                  <p className="text-xs text-[#5f6368]">{share.sharedWith?.join(', ')}</p>
                </div>
                <IssueBadge type="EXTERNAL_SHARE" />
                {share.webUrl && (
                  <a href={share.webUrl} target="_blank" rel="noopener noreferrer"
                    className="opacity-0 group-hover:opacity-100 text-xs text-[#1a73e8] hover:underline transition-opacity flex items-center gap-1">
                    <ExternalLink size={13} /> Open
                  </a>
                )}
              </div>
            ))}
          </>
        )}
      </ReportSection>
    </div>
  )
}
