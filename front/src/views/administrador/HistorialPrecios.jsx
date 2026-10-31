import { useEffect, useState } from 'react';
import festivalService from '../../services/festival.service.js';
import auditoriaService from '../../services/auditoria.service.js';
import { mensajeError } from '../../http-common.js';
import { formatearFechaHora, formatearPrecio } from '../../utils/formato.js';

// Variación porcentual entre el precio anterior y el nuevo, con color según suba o baje
function Variacion({ anterior, nuevo }) {
  const ant = Number(anterior);
  if (!ant) return <span className="text-muted">-</span>;
  const pct = ((Number(nuevo) - ant) / ant) * 100;
  const clase = pct > 0 ? 'text-danger' : pct < 0 ? 'text-success' : 'text-muted';
  const signo = pct > 0 ? '+' : '';
  return <span className={clase}>{signo}{pct.toFixed(1)}%</span>;
}

function HistorialPrecios() {
  const [festivales, setFestivales] = useState([]);
  const [idFestival, setIdFestival] = useState('');
  const [cambios, setCambios] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    festivalService.listar()
      .then(r => setFestivales(r.data))
      .catch(e => setError(mensajeError(e)));
  }, []);

  // Se vuelve a pedir el historial cada vez que cambia el filtro de festival
  useEffect(() => {
    setCargando(true);
    setError('');
    auditoriaService.listarHistorialPrecios(idFestival)
      .then(r => setCambios(r.data))
      .catch(e => setError(mensajeError(e, 'No se pudo cargar el historial de precios.')))
      .finally(() => setCargando(false));
  }, [idFestival]);

  return (
    <div className="mt-4">
      <h2 className="mb-1">Historial de cambios de precio</h2>
      <p className="text-muted">
        Registro automático (trigger de la base de datos) de cada cambio de precio de una entrada.
      </p>

      <div className="mb-3" style={{ maxWidth: 360 }}>
        <label className="form-label" htmlFor="festival">Festival</label>
        <select id="festival" className="form-select" value={idFestival}
                onChange={e => setIdFestival(e.target.value)}>
          <option value="">Todos los festivales</option>
          {festivales.map(f => (
            <option key={f.id_festival} value={f.id_festival}>{f.nombre}</option>
          ))}
        </select>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}
      {cargando && <p>Cargando historial de precios...</p>}

      {!cargando && !error && (cambios.length === 0 ? (
        <p className="text-muted">No hay cambios de precio registrados.</p>
      ) : (
        <table className="table align-middle">
          <thead>
            <tr>
              <th>Fecha</th>
              <th>Festival</th>
              <th>Entrada</th>
              <th>Precio anterior</th>
              <th>Precio nuevo</th>
              <th>Variación</th>
              <th>Modificado por</th>
            </tr>
          </thead>
          <tbody>
            {cambios.map(c => (
              <tr key={c.id_auditoria}>
                <td>{formatearFechaHora(c.fecha_modificacion)}</td>
                <td>{c.nombre_festival}</td>
                <td>{c.nombre_tipo}</td>
                <td>{formatearPrecio(c.precio_anterior)}</td>
                <td>{formatearPrecio(c.precio_nuevo)}</td>
                <td><Variacion anterior={c.precio_anterior} nuevo={c.precio_nuevo} /></td>
                {/* nombre_usuario es null si el cambio no se hizo desde la API */}
                <td>{c.nombre_usuario ?? <span className="text-muted">Sin registro</span>}</td>
              </tr>
            ))}
          </tbody>
        </table>
      ))}
    </div>
  );
}

export default HistorialPrecios;
