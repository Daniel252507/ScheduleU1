import { useEffect, useRef, useState } from 'react'
import { asistenteService } from '../config/container.js'

const SUGERENCIAS = [
  '¿Qué clases tengo el lunes?',
  '¿Cuál es mi primera clase de la semana y a qué hora?',
  '¿Tengo algún hueco libre entre clases?',
  'Resúmeme mi horario confirmado en pocas palabras.',
]

export default function Asistente() {
  const [mensajes, setMensajes] = useState([
    {
      rol: 'asistente',
      texto: '¡Hola! Soy tu asistente académico de ScheduleU. Pregúntame lo que quieras sobre tu horario confirmado.',
    },
  ])
  const [pregunta, setPregunta] = useState('')
  const [enviando, setEnviando] = useState(false)
  const [error, setError] = useState('')
  const finRef = useRef(null)

  useEffect(() => {
    finRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [mensajes, enviando])

  async function enviar(texto) {
    const consulta = (texto ?? pregunta).trim()
    if (!consulta || enviando) return

    setMensajes(prev => [...prev, { rol: 'usuario', texto: consulta }])
    setPregunta('')
    setError('')
    setEnviando(true)
    try {
      const r = await asistenteService.consultar(consulta)
      setMensajes(prev => [...prev, { rol: 'asistente', texto: r?.respuesta || 'No obtuve respuesta.' }])
    } catch (err) {
      setError(err.message || 'El asistente no pudo responder.')
    } finally {
      setEnviando(false)
    }
  }

  function onKeyDown(e) {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      enviar()
    }
  }

  return (
    <div className="page">
      <header className="page-header">
        <h1>🤖 Asistente Académico</h1>
        <p>
          Pregunta en lenguaje natural sobre tu horario confirmado: en qué salón
          o a qué hora tienes cada clase, huecos libres y más.
        </p>
      </header>

      {error && <div className="error-msg big">{error}</div>}

      <section className="card" style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        <div
          style={{
            display: 'flex',
            flexDirection: 'column',
            gap: '12px',
            maxHeight: '55vh',
            overflowY: 'auto',
            padding: '4px',
          }}
        >
          {mensajes.map((m, i) => (
            <div
              key={i}
              style={{
                alignSelf: m.rol === 'usuario' ? 'flex-end' : 'flex-start',
                maxWidth: '80%',
                padding: '10px 14px',
                borderRadius: '14px',
                lineHeight: 1.45,
                whiteSpace: 'pre-wrap',
                background: m.rol === 'usuario' ? 'var(--primary, #2563eb)' : '#f1f5f9',
                color: m.rol === 'usuario' ? '#fff' : 'var(--text, #1e293b)',
                borderBottomRightRadius: m.rol === 'usuario' ? '4px' : '14px',
                borderBottomLeftRadius: m.rol === 'asistente' ? '4px' : '14px',
              }}
            >
              {m.texto}
            </div>
          ))}
          {enviando && (
            <div style={{ alignSelf: 'flex-start', color: 'var(--text-light, #64748b)', fontStyle: 'italic' }}>
              El asistente está pensando...
            </div>
          )}
          <div ref={finRef} />
        </div>

        <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
          {SUGERENCIAS.map(s => (
            <button
              key={s}
              className="badge badge-day"
              type="button"
              style={{ cursor: 'pointer', border: 'none' }}
              onClick={() => enviar(s)}
              disabled={enviando}
            >
              {s}
            </button>
          ))}
        </div>

        <div style={{ display: 'flex', gap: '8px', alignItems: 'flex-end' }}>
          <textarea
            rows={2}
            value={pregunta}
            onChange={e => setPregunta(e.target.value)}
            onKeyDown={onKeyDown}
            placeholder="Escribe tu pregunta y pulsa Enter..."
            disabled={enviando}
            style={{
              flex: 1,
              resize: 'vertical',
              padding: '10px 12px',
              borderRadius: '10px',
              border: '1px solid var(--border, #cbd5e1)',
              fontFamily: 'inherit',
              fontSize: '14px',
            }}
          />
          <button
            className="btn-primary big"
            onClick={() => enviar()}
            disabled={enviando || !pregunta.trim()}
          >
            {enviando ? '...' : 'Enviar'}
          </button>
        </div>
      </section>
    </div>
  )
}
