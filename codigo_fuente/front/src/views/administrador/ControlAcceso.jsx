import { useEffect, useRef, useState } from 'react';
import festivalService from '../../services/festival.service.js';
import accesoService from '../../services/acceso.service.js';
import { mensajeError } from '../../http-common.js';

// Color de la tarjeta de resultado según el resultado devuelto por el backend
const CLASE_RESULTADO = {
  VALIDO: 'success',
  YA_USADO: 'warning',
  FECHA_INCORRECTA: 'warning',
};

function ControlAcceso() {
  const [festivales, setFestivales] = useState([]);
  const [idFestival, setIdFestival] = useState('');
  const [codigo, setCodigo] = useState('');
  const [resultado, setResultado] = useState(null);
  const [historial, setHistorial] = useState([]);
  const [error, setError] = useState('');
  const [enviando, setEnviando] = useState(false);
  const inputRef = useRef(null);

  useEffect(() => {
    festivalService.listar()
      .then(r => {
        setFestivales(r.data);
        if (r.data.length > 0) setIdFestival(String(r.data[0].id_festival));
      })
      .catch(e => setError(mensajeError(e)));
  }, []);

  const manejarEnvio = (e) => {
    e.preventDefault();
    const codigoLimpio = codigo.trim();
    if (!codigoLimpio || !idFestival) return;
    setEnviando(true);
    setError('');
    accesoService.validar(codigoLimpio, Number(idFestival))
      .then(r => {
        setResultado({ ...r.data, codigo: codigoLimpio });
        setHistorial(h => [{ ...r.data, codigo: codigoLimpio, hora: new Date() }, ...h].slice(0, 10));
        setCodigo('');
      })
      .catch(e => {
        setResultado(null);
        setError(mensajeError(e));
      })
      .finally(() => {
        setEnviando(false);
        inputRef.current?.focus();
      });
  };

  const clase = resultado ? (CLASE_RESULTADO[resultado.resultado] ?? 'danger') : '';

  return (
    <div className="mt-4" style={{ maxWidth: 720 }}>
      <h2 className="mb-4">Control de acceso</h2>

      <form onSubmit={manejarEnvio} className="card shadow-sm p-3 mb-4">
        <div className="mb-3">
          <label className="form-label" htmlFor="festival">Festival donde está el lector</label>
          <select id="festival" className="form-select" value={idFestival}
                  onChange={e => setIdFestival(e.target.value)} required>
            {festivales.map(f => (
              <option key={f.id_festival} value={f.id_festival}>{f.nombre}</option>
            ))}
          </select>
        </div>
        <div className="mb-3">
          <label className="form-label" htmlFor="codigo">Código del ticket</label>
          <input id="codigo" ref={inputRef} className="form-control" value={codigo} maxLength={40}
                 onChange={e => setCodigo(e.target.value)} autoFocus autoComplete="off" required />
        </div>
        <button type="submit" className="btn btn-dark" disabled={enviando || !idFestival}>
          {enviando ? 'Validando...' : 'Validar ticket'}
        </button>
      </form>

      {error && <div className="alert alert-danger">{error}</div>}

      {resultado && (
        <div className={`alert alert-${clase} shadow-sm`}>
          <h4 className="alert-heading">{resultado.mensaje}</h4>
          <div>Código: <code>{resultado.codigo}</code></div>
          {resultado.nombre_tipo && <div>Tipo de entrada: {resultado.nombre_tipo}</div>}
          {resultado.fecha_uso && (
            <div>{resultado.valido ? 'Ingreso' : 'Usado el'}: {new Date(resultado.fecha_uso).toLocaleString()}</div>
          )}
        </div>
      )}

      {historial.length > 0 && (
        <>
          <h5 className="mt-4">Últimas lecturas</h5>
          <table className="table table-sm">
            <thead>
              <tr><th>Hora</th><th>Código</th><th>Resultado</th></tr>
            </thead>
            <tbody>
              {historial.map((h, i) => (
                <tr key={i}>
                  <td>{h.hora.toLocaleTimeString()}</td>
                  <td><code>{h.codigo}</code></td>
                  <td>{h.mensaje}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </>
      )}
    </div>
  );
}

export default ControlAcceso;
