import { MoreVertical, ExternalLink } from 'lucide-react'
import FileIcon, { mimeToLabel } from './FileIcon'
import IssueBadge from './IssueBadge'
import { formatBytes, formatDate } from '../../services/driveHealthApi'

export default function FileRow({ file, issues = [], isSelected, onSelect, onClick }) {
  const fileIssues = issues.filter((f) => f.fileId === file.id || f.googleFileId === file.googleFileId)

  return (
    <tr
      className={`border-b border-[#e0e0e0] hover:bg-[#f8f9fa] cursor-pointer group transition-colors ${
        isSelected ? 'bg-[#e8f0fe]' : ''
      }`}
      onClick={() => onClick && onClick(file)}
    >
      {/* Checkbox */}
      <td className="w-10 pl-4 py-2.5" onClick={(e) => e.stopPropagation()}>
        <input
          type="checkbox"
          checked={isSelected}
          onChange={(e) => onSelect && onSelect(file, e.target.checked)}
          className="w-4 h-4 rounded border-[#5f6368] text-[#1a73e8] cursor-pointer"
        />
      </td>

      {/* Name */}
      <td className="py-2.5 pr-4">
        <div className="flex items-center gap-3 min-w-0">
          <FileIcon mimeType={file.mimeType} />
          <span className="text-sm text-[#202124] truncate max-w-xs">
            {file.name || '—'}
          </span>
          {file.webUrl && (
            <a
              href={file.webUrl}
              target="_blank"
              rel="noopener noreferrer"
              onClick={(e) => e.stopPropagation()}
              className="opacity-0 group-hover:opacity-100 transition-opacity text-[#5f6368] hover:text-[#1a73e8] shrink-0"
            >
              <ExternalLink size={14} />
            </a>
          )}
        </div>
      </td>

      {/* Type */}
      <td className="py-2.5 pr-6 text-sm text-[#5f6368] whitespace-nowrap">
        {mimeToLabel(file.mimeType)}
      </td>

      {/* Size */}
      <td className="py-2.5 pr-6 text-sm text-[#5f6368] whitespace-nowrap">
        {formatBytes(file.size)}
      </td>

      {/* Last modified */}
      <td className="py-2.5 pr-6 text-sm text-[#5f6368] whitespace-nowrap">
        {formatDate(file.modifiedTime)}
      </td>

      {/* Issues */}
      <td className="py-2.5 pr-4">
        <div className="flex flex-wrap gap-1">
          {fileIssues.length > 0
            ? fileIssues.map((issue, i) => (
                <IssueBadge key={i} type={issue.findingType} />
              ))
            : <span className="text-[#5f6368] text-sm">—</span>
          }
        </div>
      </td>

      {/* More */}
      <td className="py-2.5 pr-3" onClick={(e) => e.stopPropagation()}>
        <button className="opacity-0 group-hover:opacity-100 p-1.5 rounded-full hover:bg-[#e0e0e0] text-[#5f6368] transition-all">
          <MoreVertical size={16} />
        </button>
      </td>
    </tr>
  )
}
