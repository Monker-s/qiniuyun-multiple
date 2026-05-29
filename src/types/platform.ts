export interface PlatformInfo {
  code: string
  name: string
  icon: string
  color: string
  enabled: boolean
  builtin: boolean
}

export interface PlatformRules {
  platformCode: string
  platformName: string
  maxTitleLength: number
  maxContentLength: number
  allowedHtmlTags: string[]
  supportVideo: boolean
  supportEmbedCode: boolean
  imageRatio: string
  maxImageSizeMB: number
  maxVideoSizeMB: number
}

export interface VideoLimit {
  supported: boolean
  reason?: string
  maxSizeMB?: number
  maxDurationSec?: number
  formats?: string[]
}

export interface LoginStatus {
  platformCode: string
  loginStatus: 'VALID' | 'EXPIRED' | 'NEVER_LOGIN' | 'LOGGING_IN'
  lastLoginAt?: string
  estimatedExpireAt?: string
}

export interface UserPlatform {
  id: number
  platformCode: string
  platformName: string
  isBuiltin: boolean
  isActive: boolean
  loginStatus: string
}
