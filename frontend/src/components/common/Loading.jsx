export default function Loading({ message = 'Loading…' }) {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-[#5f6368]">
      <div className="w-8 h-8 border-2 border-[#e0e0e0] border-t-[#1a73e8] rounded-full animate-spin mb-3" />
      <span className="text-sm">{message}</span>
    </div>
  )
}
