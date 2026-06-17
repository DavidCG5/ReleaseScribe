import { useState, useCallback } from 'react'
import Markdown from 'react-markdown'

interface Props {
  title: string
  markdown: string
}

export default function MarkdownPreview({ title, markdown }: Props) {
  const [tab, setTab] = useState<'preview' | 'edit'>('preview')
  const [edited, setEdited] = useState(markdown)
  const [copied, setCopied] = useState(false)

  const content = tab === 'edit' ? edited : markdown

  const handleCopy = useCallback(() => {
    navigator.clipboard.writeText(content)
    setCopied(true)
    setTimeout(() => setCopied(false), 1800)
  }, [content])

  const handleDownload = useCallback(() => {
    const blob = new Blob([content], { type: 'text/markdown' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${title.replace(/\s+/g, '-').toLowerCase()}.md`
    a.click()
    URL.revokeObjectURL(url)
  }, [content, title])

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden animate-fade-in-up">
      <div className="flex items-center justify-between px-4 py-3 border-b border-gray-100 bg-gray-50/50">
        <div className="flex gap-1 bg-gray-200/60 rounded-lg p-0.5">
          <button
            onClick={() => setTab('preview')}
            className={`px-3 py-1 text-sm font-medium rounded-md transition-all ${
              tab === 'preview'
                ? 'bg-white text-gray-900 shadow-sm'
                : 'text-gray-500 hover:text-gray-700'
            }`}
          >
            Vista previa
          </button>
          <button
            onClick={() => setTab('edit')}
            className={`px-3 py-1 text-sm font-medium rounded-md transition-all ${
              tab === 'edit'
                ? 'bg-white text-gray-900 shadow-sm'
                : 'text-gray-500 hover:text-gray-700'
            }`}
          >
            Editar
          </button>
        </div>

        <div className="flex gap-1.5">
          <button
            onClick={handleCopy}
            className="px-3 py-1.5 text-sm text-gray-600 hover:text-gray-900 hover:bg-gray-200/60 rounded-lg transition-colors flex items-center gap-1.5"
          >
            {copied ? (
              <>
                <svg className="size-3.5 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                </svg>
                Copiado
              </>
            ) : (
              <>
                <svg className="size-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
                </svg>
                Copiar
              </>
            )}
          </button>
          <button
            onClick={handleDownload}
            className="px-3 py-1.5 text-sm text-gray-600 hover:text-gray-900 hover:bg-gray-200/60 rounded-lg transition-colors flex items-center gap-1.5"
          >
            <svg className="size-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
            .md
          </button>
        </div>
      </div>

      {tab === 'edit' ? (
        <textarea
          rows={20}
          className="w-full p-4 font-mono text-sm resize-y focus:outline-none"
          value={edited}
          onChange={(e) => setEdited(e.target.value)}
        />
      ) : (
        <div className="p-6 prose max-w-none">
          <Markdown>{markdown}</Markdown>
        </div>
      )}
    </div>
  )
}
