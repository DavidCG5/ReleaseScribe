import type { GenerateRequest, GenerateResponse, ReleaseNoteResponse } from '../types'

const BASE = '/api/release-notes'

function headers(): Record<string, string> {
  const apiKey = import.meta.env.VITE_API_KEY
  const h: Record<string, string> = { 'Content-Type': 'application/json' }
  if (apiKey) h['X-API-Key'] = apiKey
  return h
}

export async function generate(req: GenerateRequest): Promise<GenerateResponse> {
  const res = await fetch(`${BASE}/generate`, {
    method: 'POST',
    headers: headers(),
    body: JSON.stringify(req),
  })
  if (!res.ok) {
    const text = await res.text().catch(() => 'Error desconocido')
    throw new Error(text || `Error ${res.status}`)
  }
  return res.json()
}

export async function listAll(): Promise<ReleaseNoteResponse[]> {
  const res = await fetch(BASE, { headers: headers() })
  if (!res.ok) throw new Error(`Error ${res.status}`)
  return res.json()
}

export async function getById(id: string): Promise<ReleaseNoteResponse> {
  const res = await fetch(`${BASE}/${id}`, { headers: headers() })
  if (!res.ok) throw new Error(`Error ${res.status}`)
  return res.json()
}
