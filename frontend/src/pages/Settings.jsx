import { useState, useEffect } from 'react'
import { Trash2, Plus, Check, AlertCircle, User, Sliders } from 'lucide-react'
import Loading from '../components/common/Loading'
import ErrorMessage from '../components/common/ErrorMessage'
import { getMe, getRules, createRule, deleteRule } from '../services/driveHealthApi'

function Section({ title, children }) {
  return (
    <div className="mb-8">
      <h2 className="text-sm font-medium text-[#202124] mb-4 pb-2 border-b border-[#e0e0e0]">
        {title}
      </h2>
      {children}
    </div>
  )
}

function SettingRow({ label, description, children }) {
  return (
    <div className="flex items-start justify-between py-3 border-b border-[#f1f3f4] last:border-0">
      <div className="flex-1 mr-6">
        <p className="text-sm text-[#202124]">{label}</p>
        {description && <p className="text-xs text-[#5f6368] mt-0.5">{description}</p>}
      </div>
      <div className="shrink-0">{children}</div>
    </div>
  )
}

export default function Settings() {
  const [me, setMe] = useState(null)
  const [rules, setRules] = useState([])
  const [loadingMe, setLoadingMe] = useState(true)
  const [loadingRules, setLoadingRules] = useState(true)
  const [error, setError] = useState(null)
  const [newRule, setNewRule] = useState({ ruleType: 'IGNORE_FOLDER', ruleValue: '', description: '' })
  const [saving, setSaving] = useState(false)
  const [saveSuccess, setSaveSuccess] = useState(false)

  useEffect(() => {
    getMe()
      .then(setMe)
      .catch(() => setMe(null))
      .finally(() => setLoadingMe(false))

    getRules()
      .then(setRules)
      .catch(() => setRules([]))
      .finally(() => setLoadingRules(false))
  }, [])

  const handleAddRule = async (e) => {
    e.preventDefault()
    if (!newRule.ruleValue.trim()) return
    setSaving(true)
    setError(null)
    try {
      const created = await createRule({
        ruleType: newRule.ruleType,
        ruleValue: newRule.ruleValue.trim(),
        description: newRule.description.trim() || undefined,
      })
      setRules((prev) => [...prev, created])
      setNewRule({ ruleType: 'IGNORE_FOLDER', ruleValue: '', description: '' })
      setSaveSuccess(true)
      setTimeout(() => setSaveSuccess(false), 2000)
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

  const connectedAccounts = me?.googleAccounts || []

  return (
    <div className="px-8 py-6 max-w-2xl">
      <h1 className="text-xl font-medium text-[#202124] mb-6">Settings</h1>

      {error && (
        <div className="mb-5">
          <ErrorMessage message={error} onRetry={() => setError(null)} />
        </div>
      )}

      {/* Google Account */}
      <Section title="Google Account">
        {loadingMe ? (
          <Loading message="Loading account…" />
        ) : connectedAccounts.length === 0 ? (
          <div className="flex items-center gap-3 py-4 text-sm text-[#5f6368]">
            <AlertCircle size={18} className="text-[#F4B400]" />
            No Google account connected. Use the OAuth flow to connect.
          </div>
        ) : (
          connectedAccounts.map((account) => (
            <SettingRow
              key={account.id}
              label={account.email}
              description={
                account.connected
                  ? 'Connected — Drive Health has access to this account'
                  : 'Not connected'
              }
            >
              <div className="flex items-center gap-2">
                <span className="text-xs font-medium text-[#0F9D58] flex items-center gap-1">
                  <Check size={13} />
                  Connected
                </span>
              </div>
            </SettingRow>
          ))
        )}
      </Section>

      {/* Scan Rules */}
      <Section title="Scan Rules">
        <p className="text-xs text-[#5f6368] mb-4">
          Define custom rules to skip specific folders, file types, or override size/age thresholds.
        </p>

        {/* Existing rules */}
        {loadingRules ? (
          <Loading message="Loading rules…" />
        ) : rules.length === 0 ? (
          <p className="text-sm text-[#5f6368] py-2">No custom rules configured.</p>
        ) : (
          <div className="border border-[#e0e0e0] rounded-lg overflow-hidden mb-4">
            {rules.map((rule) => (
              <div
                key={rule.id}
                className="flex items-center justify-between px-4 py-3 border-b border-[#e0e0e0] last:border-0"
              >
                <div className="flex-1 min-w-0 mr-4">
                  <div className="flex items-center gap-2">
                    <span className="text-xs font-medium px-2 py-0.5 bg-[#e8f0fe] text-[#1a73e8] rounded-full">
                      {rule.ruleType.replace(/_/g, ' ')}
                    </span>
                    <span className="text-sm text-[#202124] truncate">{rule.ruleValue}</span>
                  </div>
                  {rule.description && (
                    <p className="text-xs text-[#5f6368] mt-0.5">{rule.description}</p>
                  )}
                </div>
                <button
                  onClick={() => handleDeleteRule(rule.id)}
                  className="p-1.5 rounded-full hover:bg-[#fce8e6] text-[#5f6368] hover:text-[#c5221f] transition-colors"
                >
                  <Trash2 size={15} />
                </button>
              </div>
            ))}
          </div>
        )}

        {/* Add new rule form */}
        <form onSubmit={handleAddRule} className="border border-[#e0e0e0] rounded-lg p-4">
          <p className="text-xs font-medium text-[#5f6368] uppercase tracking-wide mb-3">
            Add Rule
          </p>
          <div className="grid grid-cols-2 gap-3 mb-3">
            <div>
              <label className="block text-xs text-[#5f6368] mb-1">Rule Type</label>
              <select
                value={newRule.ruleType}
                onChange={(e) => setNewRule((r) => ({ ...r, ruleType: e.target.value }))}
                className="w-full px-3 py-2 text-sm border border-[#e0e0e0] rounded-lg text-[#202124] focus:outline-none focus:border-[#1a73e8] focus:ring-1 focus:ring-[#1a73e8]"
              >
                <option value="IGNORE_FOLDER">Ignore Folder</option>
                <option value="IGNORE_MIME_TYPE">Ignore MIME Type</option>
                <option value="LARGE_FILE_THRESHOLD">Large File Threshold (bytes)</option>
                <option value="OLD_FILE_YEARS">Old File Threshold (years)</option>
              </select>
            </div>
            <div>
              <label className="block text-xs text-[#5f6368] mb-1">Value</label>
              <input
                type="text"
                value={newRule.ruleValue}
                onChange={(e) => setNewRule((r) => ({ ...r, ruleValue: e.target.value }))}
                placeholder={
                  newRule.ruleType === 'IGNORE_FOLDER'
                    ? 'Folder ID'
                    : newRule.ruleType === 'IGNORE_MIME_TYPE'
                    ? 'e.g. video/mp4'
                    : newRule.ruleType === 'LARGE_FILE_THRESHOLD'
                    ? 'e.g. 1073741824'
                    : 'e.g. 3'
                }
                className="w-full px-3 py-2 text-sm border border-[#e0e0e0] rounded-lg text-[#202124] placeholder-[#9aa0a6] focus:outline-none focus:border-[#1a73e8] focus:ring-1 focus:ring-[#1a73e8]"
              />
            </div>
          </div>
          <div className="mb-3">
            <label className="block text-xs text-[#5f6368] mb-1">Description (optional)</label>
            <input
              type="text"
              value={newRule.description}
              onChange={(e) => setNewRule((r) => ({ ...r, description: e.target.value }))}
              placeholder="e.g. Skip archive folder"
              className="w-full px-3 py-2 text-sm border border-[#e0e0e0] rounded-lg text-[#202124] placeholder-[#9aa0a6] focus:outline-none focus:border-[#1a73e8] focus:ring-1 focus:ring-[#1a73e8]"
            />
          </div>
          <div className="flex items-center gap-3">
            <button
              type="submit"
              disabled={saving || !newRule.ruleValue.trim()}
              className="flex items-center gap-2 px-4 py-2 bg-[#1a73e8] hover:bg-[#1557b0] text-white text-sm font-medium rounded-full transition-colors disabled:opacity-50"
            >
              {saving ? (
                <div className="w-3.5 h-3.5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
              ) : (
                <Plus size={15} />
              )}
              Add rule
            </button>
            {saveSuccess && (
              <span className="flex items-center gap-1 text-sm text-[#0F9D58]">
                <Check size={15} />
                Rule added
              </span>
            )}
          </div>
        </form>
      </Section>

      {/* Scheduled Scans */}
      <Section title="Scheduled Scans">
        <SettingRow
          label="Automatic daily scan"
          description="Drive Health runs a background scan every day at 2:00 AM. Configure via scheduler.scan.cron in application.properties."
        >
          <span className="text-xs font-medium text-[#0F9D58] flex items-center gap-1">
            <Check size={13} />
            Enabled
          </span>
        </SettingRow>
      </Section>
    </div>
  )
}
