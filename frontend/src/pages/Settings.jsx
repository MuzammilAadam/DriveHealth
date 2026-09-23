import { useState, useEffect } from 'react'
import {
  Trash2,
  Plus,
  Check,
  AlertCircle,
  HardDrive,
  RefreshCw,
  Sliders,
  Shield,
  Clock,
  Layers,
  CheckCircle2,
} from 'lucide-react'
import Loading from '../components/common/Loading'
import ErrorMessage from '../components/common/ErrorMessage'
import {
  getRules,
  createRule,
  deleteRule,
  syncStorageQuota,
  scanDrive,
  formatBytes,
} from '../services/driveHealthApi'
import { useAuth } from '../context/AuthContext'

function Section({ title, description, children }) {
  return (
    <div className="mb-8 bg-white border border-[#e0e0e0] rounded-2xl p-6 shadow-xs">
      <div className="mb-4 pb-3 border-b border-[#f1f3f4]">
        <h2 className="text-base font-semibold text-[#202124]">{title}</h2>
        {description && <p className="text-xs text-[#5f6368] mt-0.5">{description}</p>}
      </div>
      {children}
    </div>
  )
}

function SettingRow({ label, description, children }) {
  return (
    <div className="flex flex-col sm:flex-row sm:items-center justify-between py-3.5 border-b border-[#f1f3f4] last:border-0 gap-3">
      <div className="flex-1 mr-4">
        <p className="text-sm font-medium text-[#202124]">{label}</p>
        {description && <p className="text-xs text-[#5f6368] mt-0.5">{description}</p>}
      </div>
      <div className="shrink-0">{children}</div>
    </div>
  )
}

export default function Settings() {
  const { user: me, activeAccount, activeAccountId, refreshUser } = useAuth()
  const [rules, setRules] = useState([])
  const [loadingRules, setLoadingRules] = useState(true)
  const [error, setError] = useState(null)
  const [newRule, setNewRule] = useState({
    ruleType: 'IGNORE_FOLDER',
    ruleValue: '',
    description: '',
  })
  const [saving, setSaving] = useState(false)
  const [saveSuccess, setSaveSuccess] = useState(false)
  const [syncingQuota, setSyncingQuota] = useState(false)
  const [scanning, setScanning] = useState(false)
  const [scanMessage, setScanMessage] = useState(null)

  useEffect(() => {
    getRules(activeAccountId)
      .then(setRules)
      .catch(() => setRules([]))
      .finally(() => setLoadingRules(false))
  }, [activeAccountId])

  const handleAddRule = async (e) => {
    e.preventDefault()
    if (!newRule.ruleValue.trim()) return
    setSaving(true)
    setError(null)
    try {
      const created = await createRule(
        {
          ruleType: newRule.ruleType,
          ruleValue: newRule.ruleValue.trim(),
          description: newRule.description.trim() || undefined,
        },
        activeAccountId
      )
      setRules((prev) => [...prev, created])
      setNewRule({ ruleType: 'IGNORE_FOLDER', ruleValue: '', description: '' })
      setSaveSuccess(true)
      setTimeout(() => setSaveSuccess(false), 2500)
    } catch (e) {
      setError(e.message)
    } finally {
      setSaving(false)
    }
  }

  const handleDeleteRule = async (id) => {
    try {
      await deleteRule(id)
      setRules((prev) => prev.filter((r) => r.id !== id))
    } catch (e) {
      setError(e.message)
    }
  }

  const handleSyncQuota = async () => {
    setSyncingQuota(true)
    setError(null)
    try {
      await syncStorageQuota(activeAccountId)
      await refreshUser()
      setScanMessage('Storage quota successfully updated from Google Drive!')
      setTimeout(() => setScanMessage(null), 3000)
    } catch (err) {
      setError(err.message)
    } finally {
      setSyncingQuota(false)
    }
  }

  const handleTriggerScan = async (incremental = false) => {
    setScanning(true)
    setError(null)
    setScanMessage(null)
    try {
      const res = await scanDrive(incremental, activeAccountId)
      setScanMessage(
        `Scan completed: ${res.filesScanned} items processed, ${res.newFiles} new files indexed!`
      )
      setTimeout(() => setScanMessage(null), 4000)
    } catch (err) {
      setError(err.message)
    } finally {
      setScanning(false)
    }
  }

  const connectedAccounts = me?.googleAccounts || []
  const usageBytes = activeAccount?.storageQuotaUsage ?? 0
  const limitBytes = activeAccount?.storageQuotaLimit ?? 16106127360

  return (
    <div className="px-8 py-8 max-w-4xl mx-auto">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-[#202124]">Settings</h1>
        <p className="text-sm text-[#5f6368] mt-1">
          Configure Google Drive sync rules, scan thresholds, and automated scheduling preferences.
        </p>
      </div>

      {error && (
        <div className="mb-6">
          <ErrorMessage message={error} onRetry={() => setError(null)} />
        </div>
      )}

      {scanMessage && (
        <div className="mb-6 p-4 rounded-xl bg-[#e6f4ea] border border-[#ceead6] text-[#137333] text-sm flex items-center gap-2">
          <CheckCircle2 size={18} />
          <span>{scanMessage}</span>
        </div>
      )}

      {/* Google Account Section */}
      <Section
        title="Connected Google Account & Quota"
        description="Active Google Drive connection and storage status"
      >
        {connectedAccounts.length === 0 ? (
          <div className="flex items-center gap-3 py-4 text-sm text-[#5f6368]">
            <AlertCircle size={18} className="text-[#F4B400]" />
            No Google account connected. Please sign in with Google to proceed.
          </div>
        ) : (
          <div>
            <SettingRow
              label={activeAccount?.email || 'Google Account'}
              description={`Account ID: ${activeAccount?.googleUserId || 'N/A'} — Access Token automatically refreshed`}
            >
              <div className="flex items-center gap-2">
                <span className="text-xs font-semibold text-[#0F9D58] bg-[#e6f4ea] px-3 py-1 rounded-full flex items-center gap-1.5">
                  <Check size={14} />
                  Connected
                </span>
              </div>
            </SettingRow>

            <SettingRow
              label="Live Storage Quota"
              description={`${formatBytes(usageBytes)} of ${
                limitBytes > 0 ? formatBytes(limitBytes) : 'Unlimited'
              } used`}
            >
              <button
                onClick={handleSyncQuota}
                disabled={syncingQuota}
                className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full border border-[#dadce0] text-[#1a73e8] hover:bg-[#f8fafd] text-xs font-medium transition-colors"
              >
                <RefreshCw size={13} className={syncingQuota ? 'animate-spin' : ''} />
                <span>{syncingQuota ? 'Refreshing…' : 'Sync Quota'}</span>
              </button>
            </SettingRow>

            <SettingRow
              label="On-Demand Hygiene Scan"
              description="Manually initiate full or incremental scan across your Drive metadata"
            >
              <div className="flex items-center gap-2">
                <button
                  onClick={() => handleTriggerScan(true)}
                  disabled={scanning}
                  className="px-3 py-1.5 rounded-full border border-[#dadce0] text-[#3c4043] hover:bg-[#f1f3f4] text-xs font-medium transition-colors disabled:opacity-50"
                >
                  Incremental Sync
                </button>
                <button
                  onClick={() => handleTriggerScan(false)}
                  disabled={scanning}
                  className="px-3.5 py-1.5 rounded-full bg-[#1a73e8] hover:bg-[#1557b0] text-white text-xs font-medium transition-colors disabled:opacity-50"
                >
                  {scanning ? 'Scanning…' : 'Full Scan'}
                </button>
              </div>
            </SettingRow>
          </div>
        )}
      </Section>

      {/* Scan Rules Section */}
      <Section
        title="Custom Hygiene & Ignore Rules"
        description="Filter out specific folders, ignore file formats, or customize analysis thresholds"
      >
        {/* Existing rules */}
        {loadingRules ? (
          <Loading message="Loading configured rules…" />
        ) : rules.length === 0 ? (
          <p className="text-xs text-[#5f6368] py-3 italic">
            No custom rules configured yet. Rules you add below will customize how files are evaluated.
          </p>
        ) : (
          <div className="border border-[#e0e0e0] rounded-xl overflow-hidden mb-5 divide-y divide-[#f1f3f4]">
            {rules.map((rule) => (
              <div
                key={rule.id}
                className="flex items-center justify-between px-4 py-3 hover:bg-[#fafafa] transition-colors"
              >
                <div className="flex-1 min-w-0 mr-4">
                  <div className="flex items-center gap-2">
                    <span className="text-[11px] font-semibold px-2 py-0.5 bg-[#e8f0fe] text-[#1a73e8] rounded-full">
                      {rule.ruleType.replace(/_/g, ' ')}
                    </span>
                    <span className="text-sm font-medium text-[#202124] truncate">
                      {rule.ruleValue}
                    </span>
                  </div>
                  {rule.description && (
                    <p className="text-xs text-[#5f6368] mt-0.5">{rule.description}</p>
                  )}
                </div>
                <button
                  onClick={() => handleDeleteRule(rule.id)}
                  title="Delete rule"
                  className="p-1.5 rounded-full hover:bg-[#fce8e6] text-[#5f6368] hover:text-[#c5221f] transition-colors"
                >
                  <Trash2 size={16} />
                </button>
              </div>
            ))}
          </div>
        )}

        {/* Add new rule form */}
        <form
          onSubmit={handleAddRule}
          className="border border-[#e0e0e0] rounded-xl p-4 bg-[#f8fafd]"
        >
          <p className="text-xs font-semibold text-[#1a73e8] uppercase tracking-wide mb-3 flex items-center gap-1.5">
            <Sliders size={14} />
            <span>Add New Rule</span>
          </p>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 mb-3">
            <div>
              <label className="block text-xs font-medium text-[#5f6368] mb-1">Rule Type</label>
              <select
                value={newRule.ruleType}
                onChange={(e) => setNewRule((r) => ({ ...r, ruleType: e.target.value }))}
                className="w-full px-3 py-2 text-sm bg-white border border-[#dadce0] rounded-lg text-[#202124] focus:outline-none focus:border-[#1a73e8]"
              >
                <option value="IGNORE_FOLDER">Ignore Folder (by Google Folder ID)</option>
                <option value="IGNORE_MIME_TYPE">Ignore MIME Type</option>
                <option value="LARGE_FILE_THRESHOLD">Large File Cutoff (Bytes)</option>
                <option value="OLD_FILE_YEARS">Old File Threshold (Years)</option>
              </select>
            </div>
            <div>
              <label className="block text-xs font-medium text-[#5f6368] mb-1">Value</label>
              <input
                type="text"
                value={newRule.ruleValue}
                onChange={(e) => setNewRule((r) => ({ ...r, ruleValue: e.target.value }))}
                placeholder={
                  newRule.ruleType === 'IGNORE_FOLDER'
                    ? 'e.g. 1VbzIKApMwSyHVmq...'
                    : newRule.ruleType === 'IGNORE_MIME_TYPE'
                    ? 'e.g. video/mp4'
                    : newRule.ruleType === 'LARGE_FILE_THRESHOLD'
                    ? 'e.g. 1073741824 (1 GB)'
                    : 'e.g. 2 (years)'
                }
                className="w-full px-3 py-2 text-sm bg-white border border-[#dadce0] rounded-lg text-[#202124] placeholder-[#9aa0a6] focus:outline-none focus:border-[#1a73e8]"
              />
            </div>
          </div>

          <div className="mb-4">
            <label className="block text-xs font-medium text-[#5f6368] mb-1">
              Description (optional)
            </label>
            <input
              type="text"
              value={newRule.description}
              onChange={(e) => setNewRule((r) => ({ ...r, description: e.target.value }))}
              placeholder="e.g. Skip raw archival video dumps"
              className="w-full px-3 py-2 text-sm bg-white border border-[#dadce0] rounded-lg text-[#202124] placeholder-[#9aa0a6] focus:outline-none focus:border-[#1a73e8]"
            />
          </div>

          <div className="flex items-center gap-3">
            <button
              type="submit"
              disabled={saving || !newRule.ruleValue.trim()}
              className="flex items-center gap-2 px-4 py-2 bg-[#1a73e8] hover:bg-[#1557b0] text-white text-xs font-medium rounded-full transition-colors disabled:opacity-50"
            >
              {saving ? (
                <div className="w-3.5 h-3.5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
              ) : (
                <Plus size={15} />
              )}
              <span>Save Rule</span>
            </button>
            {saveSuccess && (
              <span className="flex items-center gap-1 text-xs text-[#0F9D58] font-medium">
                <Check size={14} />
                Rule active!
              </span>
            )}
          </div>
        </form>
      </Section>

      {/* Scheduled Scans Section */}
      <Section
        title="Background Automation & Scheduling"
        description="Automated cron scans keeping your Drive metadata perpetually in sync"
      >
        <SettingRow
          label="Automated Daily Scan"
          description="Runs every morning at 2:00 AM server time (scheduler.scan.cron=0 0 2 * * ?)"
        >
          <span className="text-xs font-semibold text-[#0F9D58] bg-[#e6f4ea] px-3 py-1 rounded-full flex items-center gap-1.5">
            <Check size={13} />
            Active
          </span>
        </SettingRow>

        <SettingRow
          label="Incremental Changes Sync"
          description="Uses Google Drive Changes API tokens to track additions, renames, and deletions with minimal API quota"
        >
          <span className="text-xs font-semibold text-[#1a73e8] bg-[#e8f0fe] px-3 py-1 rounded-full flex items-center gap-1.5">
            <Clock size={13} />
            Supported
          </span>
        </SettingRow>
      </Section>
    </div>
  )
}
