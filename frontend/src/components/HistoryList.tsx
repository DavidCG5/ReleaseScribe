import { useState, useEffect } from 'react'
import type { ReleaseNoteResponse } from '../types'
import { listAll, getById } from '../api/releaseNotes'
import MarkdownPreview from './MarkdownPreview'

export default function HistoryList() {
  const [notes, setNotes] = useState<ReleaseNoteResponse[]>([])
  const [selected, setSelected] = useState<ReleaseNoteResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    listAll()
      .then(setNotes)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [])

  const openNote = async (id: string) => {
    setSelected(null)
    try {
      const note = await getById(id)
      setSelected(note)
    } catch (e: any) {
      setError(e.message)
    }
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center py-20 animate-fade-in">
        <div className="flex flex-col items-center gap-3">
          <span className="size-6 border-2 border-brand-200 border-t-brand-600 rounded-full animate-[spin_0.6s_linear_infinite]" />
          <p className="text-sm text-gray-400">Cargando historial...</p>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 text-red-700 rounded-xl p-4 text-sm animate-fade-in">
        Error: {error}
      </div>
    )
  }

  if (selected) {
    return (
      <div className="space-y-6 animate-fade-in-up">
        <button onClick={() => setSelected(null)} className="inline-flex items-center gap-1 text-sm text-gray-500 hover:text-gray-900 transition-colors">
          <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
          </svg>
          Volver al historial
        </button>
        <div>
          <h2 className="text-2xl font-bold text-gray-900">{selected.title}</h2>
          <p className="text-sm text-gray-500 mt-1">
            {new Date(selected.createdAt).toLocaleString('es-AR', {
              dateStyle: 'long', timeStyle: 'short'
            })}
          </p>
        </div>
        <MarkdownPreview title={selected.title} markdown={selected.generatedMarkdown || ''} />
      </div>
    )
  }

  if (notes.length === 0) {
    return (
      <div className="text-center py-20 animate-fade-in">
        <div className="size-12 mx-auto mb-4 rounded-full bg-gray-100 flex items-center justify-center">
          <svg className="size-6 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z" />
          </svg>
        </div>
        <p className="text-gray-500 font-medium">Todavía no hay release notes</p>
        <p className="text-sm text-gray-400 mt-1">Generá tu primera nota desde la pestaña Generar</p>
      </div>
    )
  }

  return (
    <div className="space-y-4 animate-fade-in-up">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-bold text-gray-900">Historial</h2>
        <span className="text-sm text-gray-400 tabular-nums">
          {notes.length} {notes.length === 1 ? 'nota' : 'notas'}
        </span>
      </div>

      <div className="grid gap-3">
        {notes.map((n, i) => (
          <button
            key={n.id}
            onClick={() => openNote(n.id)}
            className="w-full text-left bg-white rounded-xl border border-gray-200 p-4 hover:border-brand-300 hover:shadow-sm transition-all group animate-fade-in-up"
            style={{ animationDelay: `${i * 40}ms` }}
          >
            <div className="flex items-start justify-between gap-4">
              <div className="min-w-0">
                <h3 className="font-medium text-gray-900 truncate group-hover:text-brand-700 transition-colors">
                  {n.title}
                </h3>
                <p className="text-sm text-gray-500 mt-0.5">
                  {n.version ? `v${n.version}` : 'Sin versión'} &middot;{' '}
                  {new Date(n.createdAt).toLocaleDateString('es-AR', {
                    year: 'numeric', month: 'long', day: 'numeric'
                  })}
                </p>
              </div>
              <svg className="size-5 text-gray-300 group-hover:text-brand-400 shrink-0 mt-0.5 transition-colors" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" />
              </svg>
            </div>
          </button>
        ))}
      </div>
    </div>
  )
}
