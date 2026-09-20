import { AlertCircle } from 'lucide-react'

export default function ErrorMessage({ message, onRetry }) {
  return (
    <div className="flex items-start gap-3 p-4 bg-[#fce8e6] rounded-lg text-[#c5221f] text-sm">
      <AlertCircle size={18} className="mt-0.5 shrink-0" />
      <div className="flex-1">
        <p>{message || 'Something went wrong.'}</p>
        {onRetry && (
          <button
            onClick={onRetry}
            className="mt-2 text-[#1a73e8] hover:underline text-sm font-medium"
          >
            Try again
          </button>
        )}
      </div>
    </div>
  )
}
