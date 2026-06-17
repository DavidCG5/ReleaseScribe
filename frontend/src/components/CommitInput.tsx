import { useState } from 'react'

interface Props {
  onGenerate: (rawCommits: string, version: string) => Promise<void>
  loading: boolean
}

export default function CommitInput({ onGenerate, loading }: Props) {
  const [rawCommits, setRawCommits] = useState('')
  const [version, setVersion] = useState('')

  const lineCount = rawCommits.trim() ? rawCommits.trim().split('\n').length : 0

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!rawCommits.trim() || loading) return
    onGenerate(rawCommits, version)
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 space-y-5">
        <div>
          <div className="flex items-center justify-between mb-1.5">
            <label htmlFor="commits" className="text-sm font-medium text-gray-700">
              Commits
            </label>
            {lineCount > 0 && (
              <span className="text-xs text-gray-400 tabular-nums">
                {lineCount} {lineCount === 1 ? 'commit' : 'commits'}
              </span>
            )}
          </div>
          <textarea
            id="commits"
            rows={10}
            className="w-full border border-gray-200 rounded-lg p-4 font-mono text-sm resize-y focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-400 transition-shadow"
            placeholder="Pegá acá el output de git log --oneline..."
            value={rawCommits}
            onChange={(e) => setRawCommits(e.target.value)}
          />
        </div>

        <div>
          <label htmlFor="version" className="block text-sm font-medium text-gray-700 mb-1.5">
            Versión <span className="text-gray-400 font-normal">(opcional)</span>
          </label>
          <div className="relative">
            <span className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 font-medium text-sm select-none">v</span>
            <input
              id="version"
              type="text"
              className="w-full border border-gray-200 rounded-lg pl-7 pr-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-400 transition-shadow"
              placeholder="1.0.0"
              value={version}
              onChange={(e) => setVersion(e.target.value)}
            />
          </div>
        </div>
      </div>

      <button
        type="submit"
        disabled={loading || !rawCommits.trim()}
        className="w-full flex items-center justify-center gap-2 bg-gradient-to-r from-brand-600 to-brand-700 text-white px-6 py-3 rounded-xl font-medium hover:from-brand-700 hover:to-brand-800 disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-sm hover:shadow-md active:scale-[0.98]"
      >
        {loading ? (
          <>
            <span className="inline-block size-4 border-2 border-white/30 border-t-white rounded-full animate-[spin_0.6s_linear_infinite]" />
            Generando...
          </>
        ) : (
          <>
            <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M9 12h6m-3-3v6m-7 4h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
            </svg>
            Generar Release Notes
          </>
        )}
      </button>
    </form>
  )
}
