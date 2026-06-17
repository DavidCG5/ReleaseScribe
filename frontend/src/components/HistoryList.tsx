import { useState, useEffect } from 'react'
import type { ReleaseNoteResponse } from '../types'
import { listAll, getById } from '../api/releaseNotes'
import MarkdownPreview from './MarkdownPreview'

export default function HistoryList({ onBack }: { onBack: () => void }) {
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

  if (loading) return <p className="text-gray-500">Cargando historial...</p>
  if (error) return <p className="text-red-500">Error: {error}</p>

  if (selected) {
    return (
      <div className="space-y-4">
        <button onClick={() => setSelected(null)} className="text-sm text-blue-600 hover:underline">
          &larr; Volver al historial
        </button>
        <h2 className="text-xl font-bold">{selected.title}</h2>
        <p className="text-sm text-gray-500">{new Date(selected.createdAt).toLocaleString()}</p>
        <MarkdownPreview title={selected.title} markdown={selected.generatedMarkdown || ''} />
      </div>
    )
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-bold">Historial</h2>
        <button onClick={onBack} className="text-sm text-blue-600 hover:underline">
          &larr; Volver
        </button>
      </div>

      {notes.length === 0 ? (
        <p className="text-gray-500">Todavía no hay release notes generadas.</p>
      ) : (
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="border-b">
              <th className="pb-2 text-sm font-medium">Título</th>
              <th className="pb-2 text-sm font-medium">Versión</th>
              <th className="pb-2 text-sm font-medium">Creado</th>
              <th className="pb-2 text-sm font-medium"></th>
            </tr>
          </thead>
          <tbody>
            {notes.map((n) => (
              <tr key={n.id} className="border-b hover:bg-gray-50">
                <td className="py-2 text-sm">{n.title}</td>
                <td className="py-2 text-sm text-gray-500">{n.version || '-'}</td>
                <td className="py-2 text-sm text-gray-500">
                  {new Date(n.createdAt).toLocaleDateString()}
                </td>
                <td className="py-2">
                  <button
                    onClick={() => openNote(n.id)}
                    className="text-sm text-blue-600 hover:underline"
                  >
                    Ver
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}
