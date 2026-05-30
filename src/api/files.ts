import http from './request'

export const filesApi = {
  upload(file: File, onProgress?: (pct: number) => void) {
    const formData = new FormData()
    formData.append('file', file)
    if (file.type.startsWith('video/')) {
      formData.append('type', 'VIDEO')
    } else {
      formData.append('type', 'IMAGE')
    }
    return http.post('/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: (e) => {
        if (e.total && onProgress) {
          onProgress(Math.round((e.loaded * 100) / e.total))
        }
      }
    })
  },

  uploadChunk(chunk: Blob, uploadId: string, index: number, total: number) {
    const formData = new FormData()
    formData.append('file', chunk)
    formData.append('uploadId', uploadId)
    formData.append('chunkIndex', String(index))
    formData.append('totalChunks', String(total))
    return http.post('/files/upload/chunk', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  mergeChunks(uploadId: string, fileName: string, fileType: string) {
    const formData = new FormData()
    formData.append('uploadId', uploadId)
    formData.append('fileName', fileName)
    formData.append('type', fileType)
    return http.post('/files/upload/merge', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}
