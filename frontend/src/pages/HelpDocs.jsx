import { useState } from 'react'
import {
  ShieldCheck,
  AlertTriangle,
  CheckCircle2,
  UserPlus,
  ExternalLink,
  RefreshCw,
  HelpCircle,
  Lock,
  Mail,
  ChevronRight,
  FolderSync,
  Info
} from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { scanDrive } from '../services/driveHealthApi'

export default function HelpDocs() {
  const { activeAccountId, activeAccount } = useAuth()
  const [syncing, setSyncing] = useState(false)
  const [syncMessage, setSyncMessage] = useState(null)
  const [syncError, setSyncError] = useState(null)

  const handleManualScan = async () => {
    if (!activeAccountId) {
      setSyncError('No active account selected. Please sign in first.')
      return
    }
    try {
      setSyncing(true)
      setSyncMessage(null)
      setSyncError(null)
      const res = await scanDrive(activeAccountId)
      setSyncMessage(`Successfully scanned ${res?.filesScanned || 0} files (${res?.newFiles || 0} new, ${res?.updatedFiles || 0} updated).`)
    } catch (err) {
      console.error('Scan error:', err)
      setSyncError(err?.response?.data?.message || err.message || 'Failed to scan Google Drive. Please verify permissions.')
    } finally {
      setSyncing(false)
    }
  }

  return (
    <div className="p-6 max-w-5xl mx-auto space-y-8">
      {/* Header Banner */}
      <div className="bg-white rounded-xl border border-[#dadce0] p-6 shadow-xs flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div className="flex items-start gap-4">
          <div className="w-12 h-12 rounded-xl bg-[#e8f0fe] text-[#1a73e8] flex items-center justify-center shrink-0">
            <HelpCircle size={26} />
          </div>
          <div>
            <h1 className="text-xl font-semibold text-[#202124]">Google Drive Permission & Setup Guide</h1>
            <p className="text-sm text-[#5f6368] mt-1">
              Having trouble fetching Google Drive data with a new email address? Follow these step-by-step instructions to enable API access and grant permissions.
            </p>
          </div>
        </div>

        <button
          onClick={handleManualScan}
          disabled={syncing}
          className="flex items-center gap-2 px-4 py-2.5 rounded-lg bg-[#1a73e8] hover:bg-[#1557b0] text-white text-sm font-medium transition-colors shrink-0 shadow-xs disabled:opacity-50"
        >
          <RefreshCw size={16} className={syncing ? 'animate-spin' : ''} />
          <span>{syncing ? 'Syncing Drive...' : 'Trigger Drive Sync Now'}</span>
        </button>
      </div>

      {/* Sync Status Alert if user tested manual sync */}
      {syncMessage && (
        <div className="p-4 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-800 text-sm flex items-center gap-3">
          <CheckCircle2 size={20} className="text-emerald-600 shrink-0" />
          <span>{syncMessage}</span>
        </div>
      )}

      {syncError && (
        <div className="p-4 rounded-xl bg-red-50 border border-red-200 text-red-800 text-sm flex items-center gap-3">
          <AlertTriangle size={20} className="text-red-600 shrink-0" />
          <span>{syncError}</span>
        </div>
      )}

      {/* Overview Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="bg-white rounded-xl border border-[#dadce0] p-5">
          <div className="w-10 h-10 rounded-lg bg-amber-50 text-amber-600 flex items-center justify-center mb-3">
            <Lock size={20} />
          </div>
          <h3 className="text-base font-medium text-[#202124] mb-1">1. Scope Consent</h3>
          <p className="text-xs text-[#5f6368] leading-relaxed">
            Ensure all Google Drive permissions are checked on the consent screen during Google OAuth login.
          </p>
        </div>

        <div className="bg-white rounded-xl border border-[#dadce0] p-5">
          <div className="w-10 h-10 rounded-lg bg-blue-50 text-[#1a73e8] flex items-center justify-center mb-3">
            <UserPlus size={20} />
          </div>
          <h3 className="text-base font-medium text-[#202124] mb-1">2. GCP Test User</h3>
          <p className="text-xs text-[#5f6368] leading-relaxed">
            If the Google Cloud project is in testing mode, your email must be listed in Google Cloud Test Users.
          </p>
        </div>

        <div className="bg-white rounded-xl border border-[#dadce0] p-5">
          <div className="w-10 h-10 rounded-lg bg-purple-50 text-purple-600 flex items-center justify-center mb-3">
            <FolderSync size={20} />
          </div>
          <h3 className="text-base font-medium text-[#202124] mb-1">3. Initial Indexing</h3>
          <p className="text-xs text-[#5f6368] leading-relaxed">
            New emails need an initial scan to populate local metadata. Click "Trigger Drive Sync" to start scanning.
          </p>
        </div>
      </div>

      {/* Main Content Sections */}
      <div className="space-y-6">

        {/* SECTION 1: How to grant Google Drive Permissions */}
        <section className="bg-white rounded-xl border border-[#dadce0] overflow-hidden">
          <div className="px-6 py-4 border-b border-[#dadce0] bg-[#f8fafd] flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <ShieldCheck className="text-[#1a73e8]" size={20} />
              <h2 className="text-base font-semibold text-[#202124]">
                Step 1: Granting Google Drive Permissions during OAuth Login
              </h2>
            </div>
            <span className="text-xs font-medium px-2.5 py-1 rounded-full bg-[#e8f0fe] text-[#1a73e8]">
              End User Guide
            </span>
          </div>

          <div className="p-6 space-y-4">
            <p className="text-sm text-[#3c4043]">
              When logging in with a new Google Account, Google presents an OAuth permission prompt. If you do not grant permissions to Google Drive, Drive Health will not be able to scan or show your files.
            </p>

            <div className="bg-[#f8fafd] border border-[#e8eaed] rounded-xl p-4 space-y-3">
              <h4 className="text-xs font-semibold text-[#202124] uppercase tracking-wider flex items-center gap-1.5">
                <Info size={14} className="text-[#1a73e8]" />
                How to ensure permissions are allowed:
              </h4>
              <ol className="list-decimal list-inside text-sm text-[#3c4043] space-y-2 font-normal">
                <li>Click <strong>Sign in with Google</strong> on the login page.</li>
                <li>Select or enter your Google email ID.</li>
                <li>
                  On the permission screen (<em>"Drive Health wants to access your Google Account"</em>), ensure the box for:
                  <span className="block my-1 ml-6 p-2 rounded bg-white border border-[#dadce0] font-mono text-xs text-blue-700">
                    ☑ See, edit, create, and delete all of your Google Drive files
                  </span>
                  is <strong>checked</strong> before clicking <strong>Continue</strong>.
                </li>
              </ol>
            </div>

            {/* Sub-card: Re-granting permissions if previously denied */}
            <div className="border border-amber-200 bg-amber-50/50 rounded-xl p-4">
              <h4 className="text-sm font-semibold text-amber-900 flex items-center gap-2 mb-1">
                <AlertTriangle size={16} className="text-amber-600" />
                Already logged in but didn't grant permission?
              </h4>
              <p className="text-xs text-amber-800 mb-3">
                If you previously denied access, Google won't show the permission box again automatically. You need to revoke app permissions in your Google Security settings and sign in again.
              </p>
              <div className="flex flex-wrap items-center gap-3">
                <a
                  href="https://myaccount.google.com/permissions"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-amber-700 hover:bg-amber-800 text-white text-xs font-medium transition-colors"
                >
                  <span>Open Google Security Permissions</span>
                  <ExternalLink size={13} />
                </a>
                <span className="text-xs text-amber-700">Remove "Drive Health", then sign in again here.</span>
              </div>
            </div>
          </div>
        </section>

        {/* SECTION 2: Adding Test Users in Google Cloud Console */}
        <section className="bg-white rounded-xl border border-[#dadce0] overflow-hidden">
          <div className="px-6 py-4 border-b border-[#dadce0] bg-[#f8fafd] flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <UserPlus className="text-purple-600" size={20} />
              <h2 className="text-base font-semibold text-[#202124]">
                Step 2: Adding New Email IDs in Google Cloud Console (Testing Mode)
              </h2>
            </div>
            <span className="text-xs font-medium px-2.5 py-1 rounded-full bg-purple-100 text-purple-700">
              Admin / Developer Guide
            </span>
          </div>

          <div className="p-6 space-y-4">
            <p className="text-sm text-[#3c4043]">
              If the Drive Health application is running in Google Cloud <strong>Testing</strong> status (OAuth consent screen unverified), Google will block any new email ID with an <code className="bg-red-50 text-red-700 px-1.5 py-0.5 rounded border border-red-100">Access blocked: App not verified</code> or <code className="bg-red-50 text-red-700 px-1.5 py-0.5 rounded border border-red-100">403: access_denied</code> error unless the email is added as a <strong>Test User</strong>.
            </p>

            <div className="space-y-3">
              <div className="flex items-start gap-3 p-3.5 rounded-xl border border-[#dadce0] bg-white">
                <span className="w-6 h-6 rounded-full bg-purple-100 text-purple-700 text-xs font-bold flex items-center justify-center shrink-0">1</span>
                <div className="text-sm">
                  <p className="font-medium text-[#202124]">Open Google Cloud Console</p>
                  <p className="text-xs text-[#5f6368] mt-0.5">
                    Navigate to <a href="https://console.cloud.google.com/" target="_blank" rel="noopener noreferrer" className="text-[#1a73e8] underline">Google Cloud Console</a> and select your Drive Health project.
                  </p>
                </div>
              </div>

              <div className="flex items-start gap-3 p-3.5 rounded-xl border border-[#dadce0] bg-white">
                <span className="w-6 h-6 rounded-full bg-purple-100 text-purple-700 text-xs font-bold flex items-center justify-center shrink-0">2</span>
                <div className="text-sm">
                  <p className="font-medium text-[#202124]">Go to OAuth Consent Screen</p>
                  <p className="text-xs text-[#5f6368] mt-0.5">
                    In the left navigation menu, click <strong>APIs & Services</strong> &rarr; <strong>OAuth consent screen</strong>.
                  </p>
                </div>
              </div>

              <div className="flex items-start gap-3 p-3.5 rounded-xl border border-[#dadce0] bg-white">
                <span className="w-6 h-6 rounded-full bg-purple-100 text-purple-700 text-xs font-bold flex items-center justify-center shrink-0">3</span>
                <div className="text-sm">
                  <p className="font-medium text-[#202124]">Add the New Email under "Test Users"</p>
                  <p className="text-xs text-[#5f6368] mt-0.5">
                    Scroll down to the <strong>Test users</strong> section, click <strong>+ ADD USERS</strong>, type the new email address (e.g. <code className="bg-gray-100 px-1.5 py-0.5 rounded text-gray-800">newuser@gmail.com</code>), and click <strong>SAVE</strong>.
                  </p>
                </div>
              </div>

              <div className="flex items-start gap-3 p-3.5 rounded-xl border border-[#dadce0] bg-white">
                <span className="w-6 h-6 rounded-full bg-purple-100 text-purple-700 text-xs font-bold flex items-center justify-center shrink-0">4</span>
                <div className="text-sm">
                  <p className="font-medium text-[#202124]">Verify Google Drive API Enabled</p>
                  <p className="text-xs text-[#5f6368] mt-0.5">
                    Go to <strong>APIs & Services</strong> &rarr; <strong>Library</strong>, search for <strong>Google Drive API</strong>, and make sure it is <strong>ENABLED</strong>.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* SECTION 3: Troubleshooting matrix */}
        <section className="bg-white rounded-xl border border-[#dadce0] overflow-hidden">
          <div className="px-6 py-4 border-b border-[#dadce0] bg-[#f8fafd] flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <Info className="text-blue-600" size={20} />
              <h2 className="text-base font-semibold text-[#202124]">
                Step 3: Troubleshooting Common Issues
              </h2>
            </div>
          </div>

          <div className="p-6">
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs border-collapse">
                <thead>
                  <tr className="border-b border-[#dadce0] bg-[#f8fafd] text-[#5f6368] font-semibold">
                    <th className="py-2.5 px-3">Symptom</th>
                    <th className="py-2.5 px-3">Possible Cause</th>
                    <th className="py-2.5 px-3">Resolution</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-[#f1f3f4] text-[#3c4043]">
                  <tr>
                    <td className="py-3 px-3 font-medium text-red-700">Files Explorer is empty after login</td>
                    <td className="py-3 px-3 text-[#5f6368]">First sync not triggered or Drive API permission denied</td>
                    <td className="py-3 px-3">Click <strong>Trigger Drive Sync Now</strong> button above or in Files Explorer.</td>
                  </tr>
                  <tr>
                    <td className="py-3 px-3 font-medium text-red-700">Google Login Error: "App Not Verified / 403 Access Denied"</td>
                    <td className="py-3 px-3 text-[#5f6368]">Email not in GCP Test Users list</td>
                    <td className="py-3 px-3">Add email address to GCP OAuth Consent Screen under <strong>Test Users</strong>.</td>
                  </tr>
                  <tr>
                    <td className="py-3 px-3 font-medium text-red-700">Cannot upload or delete files</td>
                    <td className="py-3 px-3 text-[#5f6368]">Missing full Drive scope permissions (<code className="bg-gray-100 px-1 text-gray-800">https://www.googleapis.com/auth/drive</code>)</td>
                    <td className="py-3 px-3">Revoke app access in <a href="https://myaccount.google.com/permissions" target="_blank" rel="noopener noreferrer" className="text-[#1a73e8] underline">Google Permissions</a> and log in again, making sure to grant all requested scopes.</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </section>

      </div>
    </div>
  )
}
