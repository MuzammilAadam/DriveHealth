export default function SummaryCard({ icon, label, value, color = '#5f6368', borderColor }) {
  return (
    <div
      className="flex items-center gap-3 px-4 py-3 bg-white rounded-lg border border-[#e0e0e0] hover:shadow-sm transition-shadow"
      style={borderColor ? { borderLeftColor: borderColor, borderLeftWidth: 3 } : {}}
    >
      <div
        className="w-8 h-8 rounded-lg flex items-center justify-center shrink-0"
        style={{ backgroundColor: `${color}18`, color }}
      >
        {icon}
      </div>
      <div className="min-w-0">
        <p className="text-[11px] text-[#5f6368] uppercase tracking-wide font-medium truncate">
          {label}
        </p>
        <p className="text-xl font-medium text-[#202124] leading-tight">
          {value ?? '—'}
        </p>
      </div>
    </div>
  )
}
