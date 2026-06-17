import { useState, useCallback } from 'react'
import CommitInput from './components/CommitInput'
import MarkdownPreview from './components/MarkdownPreview'
import HistoryList from './components/HistoryList'
import { generate } from './api/releaseNotes'
import type { GenerateResponse } from './types'

type View = 'home' | 'history'

export default function App() {
  const [view, setView] = useState<View>('home')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [result, setResult] = useState<GenerateResponse | null>(null)

  const handleGenerate = useCallback(async (rawCommits: string, version: string) => {
    setLoading(true)
    setError('')
    setResult(null)
    try {
      const res = await generate({ rawCommits, version: version || undefined })
      setResult(res)
    } catch (e: any) {
      setError(e.message || 'Error al generar release notes')
    } finally {
      setLoading(false)
    }
  }, [])

  const handleNew = useCallback(() => {
    setResult(null)
    setView('home')
  }, [])

  const handleViewChange = useCallback((v: View) => {
    setView(v)
    setError('')
  }, [])

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100">
      <nav className="sticky top-0 z-10 bg-white/80 backdrop-blur-md border-b border-gray-200">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 h-14 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="size-7 rounded-lg bg-gradient-to-br from-brand-500 to-brand-700 flex items-center justify-center text-white text-xs font-bold shadow-sm">
              RS
            </div>
            <span className="font-semibold text-gray-900">ReleaseScribe</span>
          </div>
          <div className="flex gap-1 bg-gray-100 rounded-lg p-0.5">
            <button
              onClick={() => handleViewChange('home')}
              className={`px-4 py-1.5 text-sm font-medium rounded-md transition-all ${
                view === 'home'
                  ? 'bg-white text-brand-700 shadow-sm'
                  : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              Generar
            </button>
            <button
              onClick={() => handleViewChange('history')}
              className={`px-4 py-1.5 text-sm font-medium rounded-md transition-all ${
                view === 'history'
                  ? 'bg-white text-brand-700 shadow-sm'
                  : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              Historial
            </button>
          </div>
        </div>
      </nav>

      <main className="max-w-4xl mx-auto px-4 sm:px-6 py-8">
        {error && (
          <div className="mb-6 animate-fade-in bg-red-50 border border-red-200 text-red-700 rounded-xl p-4 text-sm flex items-start gap-3 shadow-sm">
            <span className="mt-0.5 shrink-0 size-4 rounded-full bg-red-400 text-white text-[10px] font-bold flex items-center justify-center">!</span>
            <span>{error}</span>
            <button onClick={() => setError('')} className="ml-auto shrink-0 text-red-400 hover:text-red-600">&times;</button>
          </div>
        )}

        <div key={view + (result ? '-result' : '')} className="animate-fade-in-up">
          {view === 'history' ? (
            <HistoryList />
          ) : result ? (
            <div className="space-y-6 animate-fade-in-up">
              <div className="flex items-center justify-between">
                <div>
                  <h2 className="text-2xl font-bold text-gray-900">{result.title}</h2>
                  <p className="text-sm text-gray-500 mt-1">
                    {new Date(result.createdAt).toLocaleString('es-AR', {
                      dateStyle: 'long', timeStyle: 'short'
                    })}
                  </p>
                </div>
                <button
                  onClick={handleNew}
                  className="px-4 py-2 text-sm font-medium text-brand-700 bg-brand-50 hover:bg-brand-100 rounded-lg transition-colors"
                >
                  Generar otra
                </button>
              </div>
              <MarkdownPreview title={result.title} markdown={result.markdown} />
            </div>
          ) : (
            <CommitInput onGenerate={handleGenerate} loading={loading} />
          )}
        </div>
      </main>
    </div>
  )
}
