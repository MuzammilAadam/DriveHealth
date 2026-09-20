import { useState } from 'react'
import { Search, LayoutList, LayoutGrid, ArrowUpDown } from 'lucide-react'
import FileRow from './FileRow'
import EmptyState from '../common/EmptyState'

const COLUMNS = ['Name', 'Type', 'Size', 'Last modified', 'Issues', '']

export default function FileTable({ files = [], issues = [], onFileClick }) {
  const [search, setSearch] = useState('')
  const [viewMode, setViewMode] = useState('list')
  const [selected, setSelected] = useState(new Set())
  const [sortField, setSortField] = useState('name')
  const [sortAsc, setSortAsc] = useState(true)
  const [showCount, setShowCount] = useState(50)

  const handleSelect = (file, checked) => {
    setSelected((prev) => {
      const next = new Set(prev)
      checked ? next.add(file.id) : next.delete(file.id)
      return next
    })
  }

  const handleSelectAll = (checked) => {
    setSelected(checked ? new Set(filtered.map((f) => f.id)) : new Set())
  }

  const toggleSort = (field) => {
    if (sortField === field) setSortAsc((a) => !a)
    else { setSortField(field); setSortAsc(true) }
  }

  const filtered = files
    .filter((f) => !search || f.name?.toLowerCase().includes(search.toLowerCase()))
    .sort((a, b) => {
      let av = a[sortField] ?? ''
      let bv = b[sortField] ?? ''
      if (sortField === 'size') { av = Number(av); bv = Number(bv) }
      if (sortField === 'modifiedTime') { av = new Date(av); bv = new Date(bv) }
      return sortAsc ? (av > bv ? 1 : -1) : (av < bv ? 1 : -1)
    })

  const visible = filtered.slice(0, showCount)

  return (
    <div>
      {/* Toolbar */}
      <div className="flex items-center justify-between mb-3">
        <h2 className="text-base font-medium text-[#202124]">
          Files ({filtered.length.toLocaleString()})
        </h2>
        <div className="flex items-center gap-2">
          {/* Search */}
          <div className="relative flex items-center">
            <Search size={16} className="absolute left-3 text-[#5f6368]" />
            <input
              type="text"
              placeholder="Search files…"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="pl-9 pr-3 py-2 text-sm bg-white border border-[#e0e0e0] rounded-full text-[#202124] placeholder-[#5f6368] focus:outline-none focus:border-[#1a73e8] focus:ring-1 focus:ring-[#1a73e8] w-52 transition-all"
            />
          </div>
          {/* View toggle */}
          <button
            onClick={() => setViewMode('list')}
            className={`p-2 rounded-full transition-colors ${
              viewMode === 'list'
                ? 'bg-[#e8f0fe] text-[#1a73e8]'
                : 'text-[#5f6368] hover:bg-[#f1f3f4]'
            }`}
          >
            <LayoutList size={18} />
          </button>
          <button
            onClick={() => setViewMode('grid')}
            className={`p-2 rounded-full transition-colors ${
              viewMode === 'grid'
                ? 'bg-[#e8f0fe] text-[#1a73e8]'
                : 'text-[#5f6368] hover:bg-[#f1f3f4]'
            }`}
          >
            <LayoutGrid size={18} />
          </button>
        </div>
      </div>

      {filtered.length === 0 ? (
        <EmptyState
          title="No files found"
          message={search ? `No results for "${search}"` : 'Run a scan to index your Drive files.'}
        />
      ) : (
        <>
          <div className="rounded-lg border border-[#e0e0e0] overflow-hidden">
            <table className="w-full">
              <thead>
                <tr className="bg-[#f8f9fa] border-b border-[#e0e0e0]">
                  <th className="w-10 pl-4 py-2.5">
                    <input
                      type="checkbox"
                      className="w-4 h-4"
                      checked={selected.size === filtered.length && filtered.length > 0}
                      onChange={(e) => handleSelectAll(e.target.checked)}
                    />
                  </th>
                  {['name', 'mimeType', 'size', 'modifiedTime'].map((field, i) => (
                    <th
                      key={field}
                      className="text-left py-2.5 pr-6 text-xs font-medium text-[#5f6368] uppercase tracking-wide cursor-pointer select-none hover:text-[#202124]"
                      onClick={() => toggleSort(field)}
                    >
                      <span className="flex items-center gap-1">
                        {COLUMNS[i + 1]}
                        <ArrowUpDown size={12} className="opacity-50" />
                      </span>
                    </th>
                  ))}
                  <th className="text-left py-2.5 pr-4 text-xs font-medium text-[#5f6368] uppercase tracking-wide">
                    Issues
                  </th>
                  <th className="w-10" />
                </tr>
              </thead>
              <tbody>
                {visible.map((file) => (
                  <FileRow
                    key={file.id}
                    file={file}
                    issues={issues}
                    isSelected={selected.has(file.id)}
                    onSelect={handleSelect}
                    onClick={onFileClick}
                  />
                ))}
              </tbody>
            </table>
          </div>

          {showCount < filtered.length && (
            <button
              onClick={() => setShowCount((n) => n + 50)}
              className="mt-4 text-sm text-[#1a73e8] hover:underline font-medium"
            >
              View more ({filtered.length - showCount} remaining)
            </button>
          )}
        </>
      )}
    </div>
  )
}
