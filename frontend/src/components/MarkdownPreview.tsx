import { useState } from 'react'
import Markdown from 'react-markdown'

interface Props {
  title: string
  markdown: string
}

export default function MarkdownPreview({ title, markdown }: Props) {
  const [editing, setEditing] = useState(false)
  const [edited, setEdited] = useState(markdown)

  const handleCopy = () => {
    navigator.clipboard.writeText(editing ? edited : markdown)
  }

  const handleDownload = () => {
    const blob = new Blob([editing ? edited : markdown], { type: 'text/markdown' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${title.replace(/\s+/g, '-').toLowerCase()}.md`
    a.click()
    URL.revokeObjectURL(url)
  }

  return (
    <div className="space-y-3">
      <div className="flex gap-2 flex-wrap">
        <button onClick={() => setEditing(!editing)} className="text-sm px-3 py-1 rounded border hover:bg-gray-100">
          {editing ? 'Ver vista previa' : 'Editar'}
        </button>
        <button onClick={handleCopy} className="text-sm px-3 py-1 rounded border hover:bg-gray-100">
          Copiar
        </button>
        <button onClick={handleDownload} className="text-sm px-3 py-1 rounded border hover:bg-gray-100">
          Descargar .md
        </button>
      </div>

      {editing ? (
        <textarea
          rows={20}
          className="w-full border rounded-lg p-3 font-mono text-sm resize-y"
          value={edited}
          onChange={(e) => setEdited(e.target.value)}
        />
      ) : (
        <div className="prose prose-sm max-w-none border rounded-lg p-4 bg-white">
          <Markdown>{markdown}</Markdown>
        </div>
      )}
    </div>
  )
}
