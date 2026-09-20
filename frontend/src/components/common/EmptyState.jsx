import { FolderOpen } from 'lucide-react'

export default function EmptyState({ title = 'Nothing here', message, action }) {
  return (
    <div className="flex flex-col items-center justify-center py-20 text-[#5f6368]">
      <FolderOpen size={48} className="mb-4 text-[#dadce0]" />
      <p className="text-base font-medium text-[#202124] mb-1">{title}</p>
      {message && <p className="text-sm text-[#5f6368] mb-4">{message}</p>}
      {action && action}
    </div>
  )
}
