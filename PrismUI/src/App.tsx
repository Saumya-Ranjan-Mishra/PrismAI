import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { ArrowUp, Bot, CircleStop, Sparkles } from 'lucide-react'
import './App.css'

type ChatMessage = {
  id: number
  role: 'user' | 'assistant'
  content: string
  servedFrom?: string
}

function App() {
  const [query, setQuery] = useState('')
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [isStreaming, setIsStreaming] = useState(false)
  const [error, setError] = useState('')
  const streamRef = useRef<EventSource | null>(null)
  const transcriptRef = useRef<HTMLDivElement | null>(null)

  useEffect(() => {
    transcriptRef.current?.scrollTo({ top: transcriptRef.current.scrollHeight, behavior: 'smooth' })
  }, [messages])

  useEffect(() => () => streamRef.current?.close(), [])

  function sendQuery(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const message = query.trim()
    if (!message || isStreaming) return

    setError('')
    setQuery('')
    setIsStreaming(true)

    const userId = Date.now()
    const assistantId = userId + 1
    setMessages((current) => [
      ...current,
      { id: userId, role: 'user', content: message },
      { id: assistantId, role: 'assistant', content: '' },
    ])

    const params = new URLSearchParams({ message })
    const stream = new EventSource(`/chat/initiateChat/stream?${params.toString()}`)
    streamRef.current = stream

    stream.addEventListener('token', (event) => {
      const { content: chunk } = JSON.parse((event as MessageEvent<string>).data) as { content: string }
      setMessages((current) => current.map((item) =>
        item.id === assistantId ? { ...item, content: item.content + chunk } : item,
      ))
    })

    stream.addEventListener('complete', (event) => {
      try {
        const result = JSON.parse((event as MessageEvent<string>).data) as {
          response?: string
          servedFrom?: string
        }
        setMessages((current) => current.map((item) => item.id === assistantId
          ? {
            ...item,
            content: item.content || result.response || '',
            servedFrom: result.servedFrom,
          }
          : item,
        ))
      } catch {
        setError('The server returned an unreadable response.')
      }
      stream.close()
      setIsStreaming(false)
    })

    stream.addEventListener('error', (event) => {
      const data = (event as MessageEvent<string>).data
      setError(data || 'Could not connect to Prism. Check that the backend is running.')
      stream.close()
      setIsStreaming(false)
    })
  }

  function stopStreaming() {
    streamRef.current?.close()
    streamRef.current = null
    setIsStreaming(false)
  }

  return (
    <main className="app-shell">
      <header className="topbar">
        <a className="brand" href="/" aria-label="Prism home">
          <span className="brand-mark"><Sparkles size={17} strokeWidth={2.2} /></span>
          <span>Prism<span className="brand-ai">AI</span></span>
        </a>
        <span className="connection"><span className="connection-dot" /> Local model</span>
      </header>

      <section className="chat-layout" aria-label="Chat">
        <div className="conversation" ref={transcriptRef}>
          {messages.length === 0 ? (
            <div className="welcome">
              <span className="welcome-icon"><Bot size={23} /></span>
              <p className="eyebrow">PRISM / LOCAL ASSISTANT</p>
              <h1>What can I help<br />you figure out?</h1>
              <p className="welcome-copy">Ask a question to start a conversation.</p>
            </div>
          ) : (
            <div className="message-list">
              {messages.map((message) => (
                <article className={`message message-${message.role}`} key={message.id}>
                  {message.role === 'assistant' && <span className="assistant-mark"><Sparkles size={15} /></span>}
                  <p className="message-content">
                    {message.content || (isStreaming && message.role === 'assistant' ? <span className="typing-indicator">Thinking<span>.</span><span>.</span><span>.</span></span> : '')}
                  </p>
                  {message.servedFrom && (
                    <span className="served-from">Served by {message.servedFrom.replace(/Cache$/, ' cache')}</span>
                  )}
                </article>
              ))}
            </div>
          )}
        </div>

        <div className="composer-wrap">
          {error && <p className="error-message" role="alert">{error}</p>}
          <form className="composer" onSubmit={sendQuery}>
            <textarea
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              onKeyDown={(event) => {
                if (event.key === 'Enter' && !event.shiftKey) {
                  event.preventDefault()
                  event.currentTarget.form?.requestSubmit()
                }
              }}
              placeholder="Ask Prism anything..."
              aria-label="Your question"
              rows={1}
              disabled={isStreaming}
            />
            {isStreaming ? (
              <button className="send-button stop-button" type="button" onClick={stopStreaming} aria-label="Stop response" title="Stop response">
                <CircleStop size={19} />
              </button>
            ) : (
              <button className="send-button" type="submit" disabled={!query.trim()} aria-label="Send question" title="Send question">
                <ArrowUp size={19} strokeWidth={2.4} />
              </button>
            )}
          </form>
          <p className="composer-note">Prism can make mistakes. Check important information.</p>
        </div>
      </section>
    </main>
  )
}

export default App
