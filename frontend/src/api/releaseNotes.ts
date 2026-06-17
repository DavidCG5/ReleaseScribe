import type { GenerateRequest, GenerateResponse, ReleaseNoteResponse } from '../types'

const BASE = '/api/release-notes'

export async function generate(req: GenerateRequest): Promise<GenerateResponse> {
  const res = await fetch(`${BASE}/generate`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(req),
  })
  if (!res.ok) {
    const text = await res.text().catch(() => 'Error desconocido')
    throw new Error(text || `Error ${res.status}`)
  }
  return res.json()
}

export async function listAll(): Promise<ReleaseNoteResponse[]> {
  const res = await fetch(BASE)
  if (!res.ok) throw new Error(`Error ${res.status}`)
  return res.json()
}

export async function getById(id: string): Promise<ReleaseNoteResponse> {
  const res = await fetch(`${BASE}/${id}`)
  if (!res.ok) throw new Error(`Error ${res.status}`)
  return res.json()
}
