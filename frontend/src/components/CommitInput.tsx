import { useState } from 'react'

interface Props {
  onGenerate: (rawCommits: string, version: string) => Promise<void>
  loading: boolean
}

export default function CommitInput({ onGenerate, loading }: Props) {
  const [rawCommits, setRawCommits] = useState('')
  const [version, setVersion] = useState('')

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!rawCommits.trim()) return
    onGenerate(rawCommits, version)
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label htmlFor="commits" className="block text-sm font-medium mb-1">
          Commits (pegá el output de git log)
        </label>
        <textarea
          id="commits"
          rows={10}
          className="w-full border rounded-lg p-3 font-mono text-sm resize-y"
          placeholder="feat: agregar autenticación&#10;fix: corregir error en login&#10;docs: actualizar README"
          value={rawCommits}
          onChange={(e) => setRawCommits(e.target.value)}
        />
      </div>

      <div>
        <label htmlFor="version" className="block text-sm font-medium mb-1">
          Versión (opcional)
        </label>
        <input
          id="version"
          type="text"
          className="w-full border rounded-lg p-2 text-sm"
          placeholder="1.0.0"
          value={version}
          onChange={(e) => setVersion(e.target.value)}
        />
      </div>

      <button
        type="submit"
        disabled={loading || !rawCommits.trim()}
        className="bg-blue-600 text-white px-6 py-2 rounded-lg font-medium hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
      >
        {loading ? 'Generando...' : 'Generar Release Notes'}
      </button>
    </form>
  )
}
