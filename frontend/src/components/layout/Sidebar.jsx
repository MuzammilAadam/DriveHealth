import { NavLink } from 'react-router-dom'
import { Plus, Home, ScanLine, BarChart2, Settings } from 'lucide-react'

const NAV_ITEMS = [
  { to: '/', label: 'Home', icon: Home, end: true },
  { to: '/scan-results', label: 'Scan Results', icon: ScanLine },
  { to: '/reports', label: 'Reports', icon: BarChart2 },
  { to: '/settings', label: 'Settings', icon: Settings },
]

const STORAGE_USED_MB = 204.4
const STORAGE_TOTAL_GB = 15
const STORAGE_PERCENT = (STORAGE_USED_MB / (STORAGE_TOTAL_GB * 1024)) * 100

export default function Sidebar() {
  return (
    <aside className="fixed top-16 left-0 bottom-0 w-56 bg-white flex flex-col overflow-y-auto z-40 pt-3 pb-4">
      {/* New button */}
      <div className="px-3 mb-3">
        <button className="flex items-center gap-3 px-5 py-3 rounded-2xl border border-[#dadce0] text-[#202124] text-sm font-medium hover:bg-[#f6fafe] hover:shadow-sm transition-all w-full">
          <Plus size={18} />
          New
        </button>
      </div>

      {/* Navigation */}
      <nav className="flex-1 px-3">
        {NAV_ITEMS.map(({ to, label, icon: Icon, end }) => (
          <NavLink
            key={to}
            to={to}
            end={end}
            className={({ isActive }) =>
              `flex items-center gap-3 px-4 py-2 rounded-full text-sm mb-0.5 transition-colors ${
                isActive
                  ? 'bg-[#e8f0fe] text-[#1a73e8] font-medium'
                  : 'text-[#202124] hover:bg-[#f1f3f4]'
              }`
            }
          >
            {({ isActive }) => (
              <>
                <Icon size={18} strokeWidth={isActive ? 2.5 : 2} />
                {label}
              </>
            )}
          </NavLink>
        ))}
      </nav>

      {/* Storage section */}
      <div className="px-4 mt-4">
        <div className="w-full h-1 bg-[#e0e0e0] rounded-full mb-2">
          <div
            className="h-full bg-[#1a73e8] rounded-full"
            style={{ width: `${Math.min(STORAGE_PERCENT, 100)}%` }}
          />
        </div>
        <p className="text-xs text-[#5f6368] mb-3">
          {STORAGE_USED_MB} MB of {STORAGE_TOTAL_GB} GB used
        </p>
        <button className="w-full py-2 px-3 rounded-full border border-[#dadce0] text-[#1a73e8] text-xs font-medium hover:bg-[#f6fafe] transition-colors">
          Get more storage
        </button>
      </div>
    </aside>
  )
}
