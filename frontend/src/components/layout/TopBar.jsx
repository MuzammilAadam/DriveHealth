import { HelpCircle, Settings, Grid3x3 } from 'lucide-react'

export default function TopBar() {
  return (
    <header className="fixed top-0 left-0 right-0 z-50 h-16 flex items-center px-4 bg-white border-b border-[#e0e0e0]">
      {/* Left: Logo */}
      <div className="flex items-center gap-2 w-56 shrink-0">
        <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
          <path d="M16 4L28 24H4L16 4Z" fill="#0F9D58" opacity="0.85" />
          <path d="M4 24L10 14H28L22 24H4Z" fill="#4285F4" opacity="0.85" />
          <path d="M10 14L16 4L22 14H10Z" fill="#FBBC04" opacity="0.9" />
        </svg>
        <span className="text-lg font-medium text-[#202124] tracking-tight">
          Drive Health
        </span>
      </div>

      {/* Center: Search */}
      <div className="flex-1 max-w-2xl mx-auto">
        <div className="relative flex items-center">
          <svg
            className="absolute left-4 text-[#5f6368]"
            width="20"
            height="20"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
          >
            <circle cx="11" cy="11" r="8" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
          <input
            type="text"
            placeholder="Search in Drive Health"
            className="w-full pl-12 pr-4 py-2.5 bg-[#f1f3f4] rounded-full text-[14px] text-[#202124] placeholder-[#5f6368] border border-transparent focus:outline-none focus:bg-white focus:border-[#e0e0e0] focus:shadow-sm transition-all"
          />
        </div>
      </div>

      {/* Right: Action icons */}
      <div className="flex items-center gap-1 ml-4">
        <button className="p-2.5 rounded-full hover:bg-[#f1f3f4] text-[#5f6368] transition-colors">
          <HelpCircle size={20} />
        </button>
        <button className="p-2.5 rounded-full hover:bg-[#f1f3f4] text-[#5f6368] transition-colors">
          <Settings size={20} />
        </button>
        <button className="p-2.5 rounded-full hover:bg-[#f1f3f4] text-[#5f6368] transition-colors">
          <Grid3x3 size={20} />
        </button>
        {/* User avatar */}
        <div className="ml-1 w-8 h-8 rounded-full bg-[#0F9D58] flex items-center justify-center text-white text-sm font-medium cursor-pointer select-none">
          A
        </div>
      </div>
    </header>
  )
}
