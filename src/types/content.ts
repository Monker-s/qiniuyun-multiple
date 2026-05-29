export interface Content {
  id?: number
  title: string
  tiptapJson: string
  coverImage?: string
  createdAt?: string
  updatedAt?: string
}

export interface ContentQuery {
  page?: number
  size?: number
  keyword?: string
}

export interface PlatformVersion {
  platformCode: string
  adaptedHtml: string
  wordCount: number
  isEdited: boolean
  appliedTemplateId?: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}
