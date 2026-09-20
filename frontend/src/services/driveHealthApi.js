import api from './api'

// ─── Auth ───────────────────────────────────────────────────────────────────

export const getAuthUrl = () =>
  api.get('/auth/google/url').then((r) => r.data)

export const getMe = () =>
  api.get('/auth/me').then((r) => r.data)

// ─── Dashboard ──────────────────────────────────────────────────────────────

export const getDashboardSummary = (accountId) =>
  api
    .get('/dashboard/summary', { params: accountId ? { accountId } : {} })
    .then((r) => r.data)

// ─── Drive / Scan ────────────────────────────────────────────────────────────

export const scanDrive = (incremental = false, accountId) =>
  api
    .post('/drive/scan', null, {
      params: { incremental, ...(accountId ? { accountId } : {}) },
    })
    .then((r) => r.data)

export const getStoredFiles = (accountId) =>
  api
    .get('/drive/stored-files', { params: accountId ? { accountId } : {} })
    .then((r) => r.data)

// ─── Scan History ───────────────────────────────────────────────────────────

export const getScans = (accountId) =>
  api
    .get('/scans', { params: accountId ? { accountId } : {} })
    .then((r) => r.data)

// ─── Analysis Findings ───────────────────────────────────────────────────────

export const getFindings = ({ accountId, type, severity, status } = {}) =>
  api
    .get('/analysis/findings', {
      params: {
        ...(accountId ? { accountId } : {}),
        ...(type ? { type } : {}),
        ...(severity ? { severity } : {}),
        ...(status ? { status } : {}),
      },
    })
    .then((r) => r.data)

export const updateFindingStatus = (id, status) =>
  api.patch(`/findings/${id}`, { status }).then((r) => r.data)

// ─── Duplicates ─────────────────────────────────────────────────────────────

export const getDuplicates = (accountId, refresh = false) =>
  api
    .get('/analysis/duplicates', {
      params: { refresh, ...(accountId ? { accountId } : {}) },
    })
    .then((r) => r.data)

// ─── Old Files ───────────────────────────────────────────────────────────────

export const getOldFiles = (accountId, years) =>
  api
    .get('/analysis/old-files', {
      params: {
        ...(accountId ? { accountId } : {}),
        ...(years ? { years } : {}),
      },
    })
    .then((r) => r.data)

// ─── Large Files ─────────────────────────────────────────────────────────────

export const getLargeFiles = (accountId, thresholdBytes) =>
  api
    .get('/analysis/large-files', {
      params: {
        ...(accountId ? { accountId } : {}),
        ...(thresholdBytes ? { thresholdBytes } : {}),
      },
    })
    .then((r) => r.data)

// ─── External Shares ─────────────────────────────────────────────────────────

export const getExternalShares = (accountId) =>
  api
    .get('/analysis/external-shares', {
      params: accountId ? { accountId } : {},
    })
    .then((r) => r.data)

// ─── Rules ───────────────────────────────────────────────────────────────────

export const getRules = (accountId) =>
  api
    .get('/rules', { params: accountId ? { accountId } : {} })
    .then((r) => r.data)

export const createRule = (data, accountId) =>
  api
    .post('/rules', data, { params: accountId ? { accountId } : {} })
    .then((r) => r.data)

export const deleteRule = (id) =>
  api.delete(`/rules/${id}`).then((r) => r.data)

// ─── Helpers ─────────────────────────────────────────────────────────────────

export const formatBytes = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(1))} ${sizes[i]}`
}

export const formatDate = (dateString) => {
  if (!dateString) return '—'
  const date = new Date(dateString)
  return date.toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  })
}

export const formatDateTime = (dateString) => {
  if (!dateString) return '—'
  const date = new Date(dateString)
  return date.toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  })
}
