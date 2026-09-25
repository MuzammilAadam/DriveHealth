import api from './api'

const withAccountId = (params = {}, accountId) => {
  const activeAccountId = accountId || localStorage.getItem('drivehealth_active_account_id')
  return activeAccountId ? { ...params, accountId: activeAccountId } : params
}

export const getAuthUrl = () => api.get('/auth/google/url').then((r) => r.data)

export const getMe = (userId) =>
  api.get('/auth/me', { params: userId ? { userId } : {} }).then((r) => r.data)

export const getUserProfile = (userId) =>
  api.get(`/auth/users/${userId}`).then((r) => r.data)

export const getAllUsers = () => api.get('/auth/users').then((r) => r.data)

export const getDashboardSummary = (accountId) =>
  api.get('/dashboard/summary', { params: withAccountId({}, accountId) }).then((r) => r.data)

export const scanDrive = (incremental = false, accountId) =>
  api.post('/drive/scan', null, {
    params: withAccountId({ incremental }, accountId),
  }).then((r) => r.data)

export const getStoredFiles = (accountId) =>
  api.get('/drive/stored-files', { params: withAccountId({}, accountId) }).then((r) => r.data)

export const deleteFile = (googleFileId, accountId) =>
  api.delete(`/drive/files/${googleFileId}`, { params: withAccountId({}, accountId) }).then((r) => r.data)

export const uploadFile = (file, accountId) => {
  const formData = new FormData()
  formData.append('file', file)
  const params = withAccountId({}, accountId)
  const queryString = new URLSearchParams(params).toString()
  return api.post(`/drive/files/upload${queryString ? `?${queryString}` : ''}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 300000, // 5 minute timeout for uploads
  }).then((r) => r.data)
}

export const getStorageQuota = (accountId) =>
  api.get('/drive/storage-quota', { params: withAccountId({}, accountId) }).then((r) => r.data)

export const syncStorageQuota = (accountId) =>
  api.post('/drive/storage-quota/sync', null, { params: withAccountId({}, accountId) }).then((r) => r.data)

export const getDriveFilesPage = (accountId, pageSize = 50, pageToken = null) =>
  api.get('/drive/files', {
    params: withAccountId({
      pageSize,
      ...(pageToken ? { pageToken } : {}),
    }, accountId),
  }).then((r) => r.data)

export const getScans = (accountId) =>
  api.get('/scans', { params: withAccountId({}, accountId) }).then((r) => r.data)

export const getFindings = ({ accountId, type, severity, status } = {}) =>
  api.get('/analysis/findings', {
    params: withAccountId({
      ...(type ? { type } : {}),
      ...(severity ? { severity } : {}),
      ...(status ? { status } : {}),
    }, accountId),
  }).then((r) => r.data)

export const updateFindingStatus = (id, status) =>
  api.patch(`/findings/${id}`, { status }).then((r) => r.data)

export const getDuplicates = (accountId, refresh = false) =>
  api.get('/analysis/duplicates', {
    params: withAccountId({ refresh }, accountId),
  }).then((r) => r.data)

export const runDuplicateDetection = (accountId) =>
  api.post('/analysis/duplicates/run', null, {
    params: withAccountId({}, accountId),
  }).then((r) => r.data)

export const getOldFiles = (accountId, years) =>
  api.get('/analysis/old-files', {
    params: withAccountId({
      ...(years ? { years } : {}),
    }, accountId),
  }).then((r) => r.data)

export const runOldFileAnalysis = (accountId, years) =>
  api.post('/analysis/old-files/run', null, {
    params: withAccountId({
      ...(years ? { years } : {}),
    }, accountId),
  }).then((r) => r.data)

export const getLargeFiles = (accountId, thresholdBytes) =>
  api.get('/analysis/large-files', {
    params: withAccountId({
      ...(thresholdBytes ? { thresholdBytes } : {}),
    }, accountId),
  }).then((r) => r.data)

export const runLargeFileAnalysis = (accountId, thresholdBytes) =>
  api.post('/analysis/large-files/run', null, {
    params: withAccountId({
      ...(thresholdBytes ? { thresholdBytes } : {}),
    }, accountId),
  }).then((r) => r.data)

export const getExternalShares = (accountId) =>
  api.get('/analysis/external-shares', {
    params: withAccountId({}, accountId),
  }).then((r) => r.data)

export const runExternalSharesAnalysis = (accountId) =>
  api.post('/analysis/external-shares/run', null, {
    params: withAccountId({}, accountId),
  }).then((r) => r.data)

export const getRules = (accountId) =>
  api.get('/rules', { params: withAccountId({}, accountId) }).then((r) => r.data)

export const createRule = (data, accountId) =>
  api.post('/rules', data, { params: withAccountId({}, accountId) }).then((r) => r.data)

export const deleteRule = (id) => api.delete(`/rules/${id}`).then((r) => r.data)

export const formatBytes = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(1))} ${sizes[i]}`
}

export const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  })
}

export const formatDateTime = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  })
}

export const getSeverityBadgeStyle = (severity) => {
  switch (severity) {
    case 'CRITICAL':
      return 'bg-red-100 text-red-700 border-red-200'
    case 'HIGH':
      return 'bg-amber-100 text-amber-800 border-amber-200'
    case 'MEDIUM':
      return 'bg-yellow-100 text-yellow-800 border-yellow-200'
    case 'LOW':
      return 'bg-blue-100 text-blue-700 border-blue-200'
    case 'INFO':
    default:
      return 'bg-slate-100 text-slate-700 border-slate-200'
  }
}

export const getStatusBadgeStyle = (status) => {
  switch (status) {
    case 'OPEN':
      return 'bg-amber-50 text-amber-700 border-amber-300'
    case 'RESOLVED':
      return 'bg-emerald-50 text-emerald-700 border-emerald-300'
    case 'IGNORED':
      return 'bg-slate-100 text-slate-600 border-slate-300'
    default:
      return 'bg-slate-50 text-slate-600 border-slate-200'
  }
}

export const calculateHealthScore = (summary) => {
  if (!summary) return 100
  const totalFiles = summary.totalFiles || 0
  if (totalFiles === 0) return 100

  let score = 100
  const openFindings = summary.openFindings || 0
  const duplicateFiles = summary.duplicateFiles || 0
  const externalShares = summary.externalShares || 0
  const oldFiles = summary.oldFiles || 0

  score -= Math.min(openFindings * 3, 30)
  score -= Math.min(duplicateFiles * 2, 25)
  score -= Math.min(externalShares * 4, 25)
  score -= Math.min(oldFiles * 1, 20)

  return Math.max(0, Math.min(100, Math.round(score)))
}
