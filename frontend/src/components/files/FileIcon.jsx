// Maps MIME types to Google Drive-style colored file icons
const ICON_MAP = {
  'application/pdf': { color: '#EA4335', label: 'PDF', bg: '#fce8e6' },
  'application/vnd.google-apps.document': { color: '#4285F4', label: 'Doc', bg: '#e8f0fe' },
  'application/vnd.google-apps.spreadsheet': { color: '#0F9D58', label: 'Sheet', bg: '#e6f4ea' },
  'application/vnd.google-apps.presentation': { color: '#F4B400', label: 'Slides', bg: '#fef9e7' },
  'application/vnd.google-apps.form': { color: '#7986CB', label: 'Form', bg: '#ede7f6' },
  'application/vnd.google-apps.folder': { color: '#5F6368', label: 'Folder', bg: '#f1f3f4' },
  'image/jpeg': { color: '#EA4335', label: 'JPG', bg: '#fce8e6' },
  'image/png': { color: '#EA4335', label: 'PNG', bg: '#fce8e6' },
  'image/gif': { color: '#EA4335', label: 'GIF', bg: '#fce8e6' },
  'image/webp': { color: '#EA4335', label: 'IMG', bg: '#fce8e6' },
  'video/mp4': { color: '#9C27B0', label: 'MP4', bg: '#f3e5f5' },
  'video/quicktime': { color: '#9C27B0', label: 'MOV', bg: '#f3e5f5' },
  'audio/mpeg': { color: '#E91E63', label: 'MP3', bg: '#fce4ec' },
  'application/zip': { color: '#FF9800', label: 'ZIP', bg: '#fff3e0' },
  'application/x-zip-compressed': { color: '#FF9800', label: 'ZIP', bg: '#fff3e0' },
  'text/plain': { color: '#5F6368', label: 'TXT', bg: '#f1f3f4' },
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document': {
    color: '#4285F4', label: 'DOC', bg: '#e8f0fe',
  },
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': {
    color: '#0F9D58', label: 'XLS', bg: '#e6f4ea',
  },
  'application/vnd.openxmlformats-officedocument.presentationml.presentation': {
    color: '#F4B400', label: 'PPT', bg: '#fef9e7',
  },
}

const DEFAULT = { color: '#5F6368', label: 'File', bg: '#f1f3f4' }

function getIconConfig(mimeType) {
  if (!mimeType) return DEFAULT
  // Exact match
  if (ICON_MAP[mimeType]) return ICON_MAP[mimeType]
  // Partial match for image/*
  if (mimeType.startsWith('image/')) return { color: '#EA4335', label: 'IMG', bg: '#fce8e6' }
  if (mimeType.startsWith('video/')) return { color: '#9C27B0', label: 'VID', bg: '#f3e5f5' }
  if (mimeType.startsWith('audio/')) return { color: '#E91E63', label: 'AUD', bg: '#fce4ec' }
  if (mimeType.startsWith('application/vnd.google-apps.'))
    return { color: '#4285F4', label: 'GDrive', bg: '#e8f0fe' }
  return DEFAULT
}

export function mimeToLabel(mimeType) {
  if (!mimeType) return 'File'
  if (mimeType === 'application/pdf') return 'PDF'
  if (mimeType === 'application/vnd.google-apps.document') return 'Google Docs'
  if (mimeType === 'application/vnd.google-apps.spreadsheet') return 'Google Sheets'
  if (mimeType === 'application/vnd.google-apps.presentation') return 'Google Slides'
  if (mimeType === 'application/vnd.google-apps.form') return 'Google Forms'
  if (mimeType === 'application/vnd.google-apps.folder') return 'Folder'
  if (mimeType.startsWith('image/')) return mimeType.split('/')[1].toUpperCase()
  if (mimeType.startsWith('video/')) return mimeType.split('/')[1].toUpperCase()
  if (mimeType.startsWith('audio/')) return mimeType.split('/')[1].toUpperCase()
  return mimeType.split('/').pop().toUpperCase()
}

export default function FileIcon({ mimeType, size = 'md' }) {
  const { color, label, bg } = getIconConfig(mimeType)
  const dim = size === 'sm' ? 'w-7 h-7 text-[9px]' : 'w-9 h-9 text-[10px]'

  return (
    <div
      className={`${dim} rounded flex items-center justify-center font-bold shrink-0 select-none`}
      style={{ backgroundColor: bg, color }}
    >
      {label}
    </div>
  )
}
