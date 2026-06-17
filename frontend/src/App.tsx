import { useState } from 'react'
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

  const handleGenerate = async (rawCommits: string, version: string) => {
    setLoading(true)
    setError('')
    setResult(null)
    try {
      const res = await generate({ rawCommits, version: version || undefined })
      setResult(res)
    } catch (e: any) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen max-w-3xl mx-auto p-6 space-y-6">
      <header className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">ReleaseScribe</h1>
        <button
          onClick={() => setView(view === 'history' ? 'home' : 'history')}
          className="text-sm text-blue-600 hover:underline"
        >
          {view === 'history' ? 'Nueva nota' : 'Historial'}
        </button>
      </header>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 rounded-lg p-3 text-sm">
          {error}
        </div>
      )}

      {view === 'history' ? (
        <HistoryList onBack={() => setView('home')} />
      ) : result ? (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold">{result.title}</h2>
            <button
              onClick={() => setResult(null)}
              className="text-sm text-blue-600 hover:underline"
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
  )
}
