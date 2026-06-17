export interface GenerateRequest {
  rawCommits: string
  version?: string
}

export interface GenerateResponse {
  id: string
  title: string
  markdown: string
  createdAt: string
}

export interface ReleaseNoteResponse {
  id: string
  title: string
  version?: string
  rawCommits?: string
  generatedMarkdown?: string
  createdAt: string
}
