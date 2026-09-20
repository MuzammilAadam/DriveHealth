import { X, ExternalLink } from 'lucide-react'
import FileIcon, { mimeToLabel } from './FileIcon'
import IssueBadge from './IssueBadge'
import { formatBytes, formatDateTime } from '../../services/driveHealthApi'

export default function FileDetails({ file, issues = [], onClose }) {
  if (!file) return null

  const fileIssues = issues.filter(
    (f) => f.fileId === file.id || f.googleFileId === file.googleFileId
  )

  return (
    <aside className="w-72 shrink-0 border-l border-[#e0e0e0] bg-white h-full flex flex-col">
      {/* Header */}
      <div className="flex items-center justify-between px-4 py-3 border-b border-[#e0e0e0]">
        <span className="text-sm font-medium text-[#202124]">Details</span>
        <button
          onClick={onClose}
          className="p-1.5 rounded-full hover:bg-[#f1f3f4] text-[#5f6368]"
        >
          <X size={18} />
        </button>
      </div>

      {/* File icon + name */}
      <div className="flex flex-col items-center py-8 px-4 border-b border-[#e0e0e0]">
        <FileIcon mimeType={file.mimeType} size="lg" />
        <p className="mt-3 text-sm font-medium text-[#202124] text-center break-all">
          {file.name}
        </p>
        <p className="text-xs text-[#5f6368] mt-1">{mimeToLabel(file.mimeType)}</p>
      </div>

      {/* Metadata */}
      <div className="flex-1 overflow-y-auto px-4 py-4 space-y-4">
        <DetailRow label="Size" value={formatBytes(file.size)} />
        <DetailRow label="Created" value={formatDateTime(file.createdTime)} />
        <DetailRow label="Modified" value={formatDateTime(file.modifiedTime)} />
        {file.mimeType && <DetailRow label="Type" value={file.mimeType} small />}
        {file.parentId && <DetailRow label="Parent folder" value={file.parentId} small />}

        {/* Issues */}
        {fileIssues.length > 0 && (
          <div>
            <p className="text-xs font-medium text-[#5f6368] uppercase tracking-wide mb-2">
              Issues detected
            </p>
            <div className="flex flex-wrap gap-1.5">
              {fileIssues.map((issue, i) => (
                <IssueBadge key={i} type={issue.findingType} />
              ))}
            </div>
            {fileIssues.map((issue, i) => (
              <p key={i} className="text-xs text-[#5f6368] mt-2">
                {issue.reason}
              </p>
            ))}
          </div>
        )}

        {/* Open in Drive */}
        {file.webUrl && (
          <a
            href={file.webUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center gap-2 w-full justify-center py-2.5 mt-2 rounded-full border border-[#dadce0] text-sm text-[#1a73e8] font-medium hover:bg-[#f6fafe] transition-colors"
          >
            <ExternalLink size={15} />
            Open in Google Drive
          </a>
        )}
      </div>
    </aside>
  )
}

function DetailRow({ label, value, small }) {
  return (
    <div>
      <p className="text-xs font-medium text-[#5f6368] uppercase tracking-wide mb-0.5">{label}</p>
      <p className={`text-[#202124] break-all ${small ? 'text-xs' : 'text-sm'}`}>{value || '—'}</p>
    </div>
  )
}
