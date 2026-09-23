import { useState, useEffect } from 'react'
import {
  User,
  HardDrive,
  ShieldCheck,
  RefreshCw,
  ExternalLink,
  CheckCircle2,
  Calendar,
  Layers,
  ArrowRight,
  Database,
  Mail,
  Trash2,
  Lock,
} from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import {
  getStorageQuota,
  syncStorageQuota,
  getDashboardSummary,
  formatBytes,
  formatDateTime,
  calculateHealthScore,
} from '../services/driveHealthApi'
import Loading from '../components/common/Loading'
import ErrorMessage from '../components/common/ErrorMessage'

export default function Profile() {
  const { user, activeAccount, activeAccountId, switchAccount, loginWithOAuth, logout } = useAuth()
  const [quota, setQuota] = useState(null)
  const [summary, setSummary] = useState(null)
  const [loading, setLoading] = useState(true)
  const [refreshingQuota, setRefreshingQuota] = useState(false)
  const [error, setError] = useState(null)
  const [successMessage, setSuccessMessage] = useState(null)

  const loadProfileData = async () => {
    setLoading(true)
    setError(null)
    try {
      const [quotaData, summaryData] = await Promise.all([
        getStorageQuota(activeAccountId).catch(() => null),
        getDashboardSummary(activeAccountId).catch(() => null),
      ])
      setQuota(quotaData)
      setSummary(summaryData)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadProfileData()
  }, [activeAccountId])

  const handleRefreshQuota = async () => {
    setRefreshingQuota(true)
    setError(null)
    setSuccessMessage(null)
    try {
      const updated = await syncStorageQuota(activeAccountId)
      setQuota(updated)
      setSuccessMessage('Storage quota successfully refreshed from Google Drive!')
      setTimeout(() => setSuccessMessage(null), 3500)
    } catch (err) {
      setError(err.message)
    } finally {
      setRefreshingQuota(false)
    }
  }

  const usageBytes = quota?.usageBytes ?? activeAccount?.storageQuotaUsage ?? 0
  const limitBytes = quota?.limitBytes ?? activeAccount?.storageQuotaLimit ?? 16106127360
  const driveFilesBytes = quota?.usageInDriveBytes ?? activeAccount?.storageQuotaUsageInDrive ?? 0
  const trashBytes = quota?.usageInDriveTrashBytes ?? activeAccount?.storageQuotaUsageInDriveTrash ?? 0
  const otherBytes = Math.max(0, usageBytes - driveFilesBytes - trashBytes)
  const freeBytes = limitBytes > 0 ? Math.max(0, limitBytes - usageBytes) : null
  const percentUsed = limitBytes > 0 ? Math.min(100, (usageBytes / limitBytes) * 100) : 0
  const healthScore = calculateHealthScore(summary)

  const accounts = user?.googleAccounts || []
  const displayName = activeAccount?.name || user?.name || user?.email || 'User'
  const email = activeAccount?.email || user?.email || '-'
  const pictureUrl = activeAccount?.pictureUrl

  if (loading) {
    return <Loading message="Loading profile and Drive storage details…" />
  }

  return (
    <div className="max-w-5xl mx-auto px-6 py-8">
      {/* Top Heading */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
        <div>
          <h1 className="text-2xl font-bold text-[#202124]">Account & Profile</h1>
          <p className="text-sm text-[#5f6368] mt-1">
            Manage your connected Google accounts and review live Drive storage metrics.
          </p>
        </div>

        <button
          onClick={handleRefreshQuota}
          disabled={refreshingQuota}
          className="inline-flex items-center gap-2 px-4 py-2 rounded-full border border-[#dadce0] bg-white hover:bg-[#f8fafd] text-[#1a73e8] text-sm font-medium transition-colors shadow-2xs self-start"
        >
          <RefreshCw size={15} className={refreshingQuota ? 'animate-spin' : ''} />
          <span>{refreshingQuota ? 'Syncing with Google…' : 'Refresh Storage'}</span>
        </button>
      </div>

      {error && (
        <div className="mb-6">
          <ErrorMessage message={error} onRetry={loadProfileData} />
        </div>
      )}

      {successMessage && (
        <div className="mb-6 p-4 rounded-xl bg-[#e6f4ea] border border-[#ceead6] text-[#137333] text-sm flex items-center gap-2">
          <CheckCircle2 size={18} />
          <span>{successMessage}</span>
        </div>
      )}

      {/* Main Profile Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
        {/* User Card */}
        <div className="lg:col-span-1 bg-white border border-[#e0e0e0] rounded-2xl p-6 shadow-xs flex flex-col justify-between">
          <div>
            <div className="flex items-center gap-4 mb-5">
              {pictureUrl ? (
                <img
                  src={pictureUrl}
                  alt={displayName}
                  className="w-16 h-16 rounded-full object-cover ring-2 ring-[#e8f0fe]"
                />
              ) : (
                <div className="w-16 h-16 rounded-full bg-[#1a73e8] text-white flex items-center justify-center text-2xl font-semibold select-none shadow-inner">
                  {displayName.charAt(0).toUpperCase()}
                </div>
              )}
              <div className="min-w-0 flex-1">
                <h2 className="text-lg font-semibold text-[#202124] truncate">{displayName}</h2>
                <p className="text-sm text-[#5f6368] truncate flex items-center gap-1.5 mt-0.5">
                  <Mail size={13} />
                  <span>{email}</span>
                </p>
                <div className="mt-2 inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full bg-[#e6f4ea] text-[#137333] text-xs font-medium">
                  <CheckCircle2 size={12} />
                  Connected Google Drive
                </div>
              </div>
            </div>

            <div className="space-y-3 pt-4 border-t border-[#f1f3f4] text-xs text-[#5f6368]">
              <div className="flex justify-between items-center">
                <span>Google Account ID</span>
                <span className="font-mono text-[#202124] font-medium truncate max-w-44">
                  {activeAccount?.googleUserId || 'N/A'}
                </span>
              </div>
              <div className="flex justify-between items-center">
                <span>Connected Date</span>
                <span className="text-[#202124] font-medium">
                  {formatDateTime(activeAccount?.connectedAt)}
                </span>
              </div>
              <div className="flex justify-between items-center">
                <span>Last Scan / Sync</span>
                <span className="text-[#202124] font-medium">
                  {formatDateTime(activeAccount?.lastSyncedAt)}
                </span>
              </div>
            </div>
          </div>

          <div className="mt-6 pt-4 border-t border-[#f1f3f4] flex items-center justify-between">
            <span className="text-xs text-[#5f6368]">Current Session</span>
            <button
              onClick={logout}
              className="text-xs font-medium text-[#d93025] hover:bg-[#fce8e6] px-3 py-1.5 rounded-full transition-colors"
            >
              Sign out
            </button>
          </div>
        </div>

        {/* Live Storage Quota Breakdown Card */}
        <div className="lg:col-span-2 bg-white border border-[#e0e0e0] rounded-2xl p-6 shadow-xs flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-2.5">
                <div className="w-9 h-9 rounded-xl bg-[#e8f0fe] text-[#1a73e8] flex items-center justify-center">
                  <HardDrive size={20} />
                </div>
                <div>
                  <h2 className="text-base font-semibold text-[#202124]">
                    Google Drive Live Storage
                  </h2>
                  <p className="text-xs text-[#5f6368]">Real-time cloud storage usage breakdown</p>
                </div>
              </div>
              <div className="text-right">
                <span className="text-xl font-bold text-[#202124]">
                  {formatBytes(usageBytes)}
                </span>
                <span className="text-xs text-[#5f6368]">
                  {' '}
                  / {limitBytes > 0 ? formatBytes(limitBytes) : 'Unlimited'}
                </span>
              </div>
            </div>

            {/* Segmented / Progress Bar */}
            <div className="my-5">
              <div className="w-full h-3 bg-[#e8eaed] rounded-full overflow-hidden flex">
                <div
                  title={`Google Drive: ${formatBytes(driveFilesBytes)}`}
                  className="bg-[#1a73e8] h-full transition-all duration-500"
                  style={{ width: `${limitBytes > 0 ? (driveFilesBytes / limitBytes) * 100 : 0}%` }}
                />
                <div
                  title={`Trash: ${formatBytes(trashBytes)}`}
                  className="bg-[#ea4335] h-full transition-all duration-500"
                  style={{ width: `${limitBytes > 0 ? (trashBytes / limitBytes) * 100 : 0}%` }}
                />
                <div
                  title={`Gmail & Google Photos: ${formatBytes(otherBytes)}`}
                  className="bg-[#fbbc04] h-full transition-all duration-500"
                  style={{ width: `${limitBytes > 0 ? (otherBytes / limitBytes) * 100 : 0}%` }}
                />
              </div>
              <div className="flex justify-between items-center text-xs text-[#5f6368] mt-2">
                <span>{percentUsed.toFixed(1)}% total quota utilized</span>
                {freeBytes != null && (
                  <span className="text-[#137333] font-medium">
                    {formatBytes(freeBytes)} free space remaining
                  </span>
                )}
              </div>
            </div>

            {/* Granular Legend Metrics */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 mt-4">
              <div className="p-3 bg-[#f8fafd] rounded-xl border border-[#edf2fa]">
                <div className="flex items-center gap-1.5 text-xs text-[#5f6368] mb-1">
                  <div className="w-2.5 h-2.5 rounded-full bg-[#1a73e8]" />
                  <span>Drive Files</span>
                </div>
                <div className="text-sm font-semibold text-[#202124]">
                  {formatBytes(driveFilesBytes)}
                </div>
              </div>

              <div className="p-3 bg-[#f8fafd] rounded-xl border border-[#edf2fa]">
                <div className="flex items-center gap-1.5 text-xs text-[#5f6368] mb-1">
                  <div className="w-2.5 h-2.5 rounded-full bg-[#ea4335]" />
                  <span>Trash</span>
                </div>
                <div className="text-sm font-semibold text-[#202124]">
                  {formatBytes(trashBytes)}
                </div>
              </div>

              <div className="p-3 bg-[#f8fafd] rounded-xl border border-[#edf2fa]">
                <div className="flex items-center gap-1.5 text-xs text-[#5f6368] mb-1">
                  <div className="w-2.5 h-2.5 rounded-full bg-[#fbbc04]" />
                  <span>Other Google</span>
                </div>
                <div className="text-sm font-semibold text-[#202124]">{formatBytes(otherBytes)}</div>
              </div>

              <div className="p-3 bg-[#f8fafd] rounded-xl border border-[#edf2fa]">
                <div className="flex items-center gap-1.5 text-xs text-[#5f6368] mb-1">
                  <div className="w-2.5 h-2.5 rounded-full bg-[#34a853]" />
                  <span>Free Space</span>
                </div>
                <div className="text-sm font-semibold text-[#202124]">
                  {freeBytes != null ? formatBytes(freeBytes) : '-'}
                </div>
              </div>
            </div>
          </div>

          <div className="mt-6 pt-4 border-t border-[#f1f3f4] flex items-center justify-between">
            <span className="text-xs text-[#5f6368]">
              Storage quota metrics are sourced directly from Google Drive API v3
            </span>
            <a
              href="https://one.google.com/storage"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-1 text-xs font-medium text-[#1a73e8] hover:underline"
            >
              <span>Manage on Google One</span>
              <ExternalLink size={13} />
            </a>
          </div>
        </div>
      </div>

      {/* Connected Accounts & Security Section */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Connected Google Accounts */}
        <div className="bg-white border border-[#e0e0e0] rounded-2xl p-6 shadow-xs">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-2">
              <Layers size={18} className="text-[#1a73e8]" />
              <h2 className="text-base font-semibold text-[#202124]">Connected Google Accounts</h2>
            </div>
            <button
              onClick={loginWithOAuth}
              className="text-xs font-medium text-[#1a73e8] hover:bg-[#e8f0fe] px-3 py-1.5 rounded-full transition-colors flex items-center gap-1"
            >
              <span>Add account</span>
              <ArrowRight size={13} />
            </button>
          </div>

          <div className="space-y-3">
            {accounts.map((acc) => {
              const isSelected = acc.id === activeAccountId
              return (
                <div
                  key={acc.id}
                  className={`p-3.5 rounded-xl border transition-all flex items-center justify-between ${
                    isSelected
                      ? 'border-[#1a73e8] bg-[#f8fafd] ring-1 ring-[#1a73e8]'
                      : 'border-[#e0e0e0] hover:bg-[#f8f9fa]'
                  }`}
                >
                  <div className="flex items-center gap-3 min-w-0">
                    {acc.pictureUrl ? (
                      <img
                        src={acc.pictureUrl}
                        alt={acc.name || acc.email}
                        className="w-9 h-9 rounded-full object-cover"
                      />
                    ) : (
                      <div className="w-9 h-9 rounded-full bg-[#0F9D58] text-white flex items-center justify-center text-sm font-medium">
                        {(acc.name || acc.email).charAt(0).toUpperCase()}
                      </div>
                    )}
                    <div className="min-w-0">
                      <p className="text-sm font-medium text-[#202124] truncate">
                        {acc.name || acc.email}
                      </p>
                      <p className="text-xs text-[#5f6368] truncate">{acc.email}</p>
                    </div>
                  </div>

                  <div className="flex items-center gap-2 shrink-0">
                    {isSelected ? (
                      <span className="text-xs font-medium text-[#1a73e8] bg-[#e8f0fe] px-2.5 py-1 rounded-full">
                        Active Account
                      </span>
                    ) : (
                      <button
                        onClick={() => switchAccount(acc.id)}
                        className="text-xs font-medium text-[#5f6368] hover:text-[#202124] border border-[#dadce0] hover:bg-white px-2.5 py-1 rounded-full transition-colors"
                      >
                        Switch
                      </button>
                    )}
                  </div>
                </div>
              )
            })}
          </div>
        </div>

        {/* Permissions & Security Card */}
        <div className="bg-white border border-[#e0e0e0] rounded-2xl p-6 shadow-xs">
          <div className="flex items-center gap-2 mb-4">
            <ShieldCheck size={18} className="text-[#34a853]" />
            <h2 className="text-base font-semibold text-[#202124]">Security & Granted Scopes</h2>
          </div>

          <p className="text-xs text-[#5f6368] mb-4">
            Drive Health operates with strictly limited read-only metadata scopes. File and document
            contents are NEVER downloaded or stored on our servers.
          </p>

          <div className="space-y-2.5">
            <div className="p-3 bg-[#f8fafd] rounded-xl border border-[#edf2fa] flex items-start gap-3">
              <div className="w-2 h-2 rounded-full bg-[#34a853] mt-1.5 shrink-0" />
              <div>
                <p className="text-xs font-semibold text-[#202124]">
                  drive.metadata.readonly (Read-Only Metadata)
                </p>
                <p className="text-[11px] text-[#5f6368] mt-0.5">
                  Allows scanning file names, sizes, creation dates, MD5 checksums, and sharing
                  permissions.
                </p>
              </div>
            </div>

            <div className="p-3 bg-[#f8fafd] rounded-xl border border-[#edf2fa] flex items-start gap-3">
              <div className="w-2 h-2 rounded-full bg-[#34a853] mt-1.5 shrink-0" />
              <div>
                <p className="text-xs font-semibold text-[#202124]">openid & profile (Identity)</p>
                <p className="text-[11px] text-[#5f6368] mt-0.5">
                  Allows identifying your Google user account and display picture.
                </p>
              </div>
            </div>

            <div className="p-3 bg-[#f8fafd] rounded-xl border border-[#edf2fa] flex items-start gap-3">
              <div className="w-2 h-2 rounded-full bg-[#34a853] mt-1.5 shrink-0" />
              <div>
                <p className="text-xs font-semibold text-[#202124]">
                  Automated Token Refresh Guard
                </p>
                <p className="text-[11px] text-[#5f6368] mt-0.5">
                  Access tokens are securely renewed with Google OAuth servers whenever expired.
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
