// Issue badge colors that match Google's subtle palette (no neons)
const BADGE_CONFIG = {
  DUPLICATE: { label: 'Duplicate', bg: '#fce8e6', color: '#c5221f', border: '#f5c6c3' },
  DUPLICATE_FILE: { label: 'Duplicate', bg: '#fce8e6', color: '#c5221f', border: '#f5c6c3' },
  OLD_FILE: { label: 'Old file', bg: '#fef9e7', color: '#b06000', border: '#fde68a' },
  LARGE_FILE: { label: 'Large file', bg: '#e8f0fe', color: '#1557b0', border: '#c5d8fd' },
  EXTERNAL_SHARE: { label: 'Shared externally', bg: '#e6f4ea', color: '#1e7e34', border: '#a8d5b5' },
  PUBLIC_FILE: { label: 'Public', bg: '#f3e5f5', color: '#6a1b9a', border: '#ce93d8' },
}

export default function IssueBadge({ type }) {
  const config = BADGE_CONFIG[type] || {
    label: type,
    bg: '#f1f3f4',
    color: '#5f6368',
    border: '#e0e0e0',
  }

  return (
    <span
      className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border"
      style={{ backgroundColor: config.bg, color: config.color, borderColor: config.border }}
    >
      {config.label}
    </span>
  )
}
