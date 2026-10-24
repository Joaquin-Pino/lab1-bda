import { useEffect, useState } from 'react';
import compraService from '../../services/compra.service.js';
import { mensajeError } from '../../http-common.js';
import { formatearFecha, formatearFechaHora, formatearPrecio } from '../../utils/formato.js';

// Color del badge según el estado del ticket
const COLOR_ESTADO = { VIGENTE: 'bg-success', UTILIZADO: 'bg-secondary', CANCELADO: 'bg-danger' };

function MisTickets() {
  const [tickets, setTickets] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [errorMensaje, setErrorMensaje] = useState('');

  useEffect(() => {
    compraService.misTickets()
      .then(response => setTickets(response.data))
      .catch(error => setErrorMensaje(mensajeError(error, "No se pudieron cargar tus tickets.")))
      .finally(() => setCargando(false));
  }, []);

  if (cargando) return <p>Cargando tickets...</p>;
  if (errorMensaje) return <div className="alert alert-danger">{errorMensaje}</div>;

  return (
    <>
      <h2 className="mb-4">Mis tickets</h2>

      {tickets.length === 0 ? (
        <p className="text-muted">Todavía no tienes tickets.</p>
      ) : (
        <table className="table align-middle">
          <thead>
            <tr>
              <th>Código</th>
              <th>Festival</th>
              <th>Entrada</th>
              <th>Válido para</th>
              <th>Precio</th>
              <th>Estado</th>
            </tr>
          </thead>
          <tbody>
            {tickets.map(ticket => (
              <tr key={ticket.id_ticket}>
                <td><code>{ticket.codigo_unico}</code></td>
                <td>{ticket.nombre_festival}</td>
                <td>{ticket.nombre_tipo}</td>
                {/* fecha_entrada null = pase válido para todo el festival */}
                <td>{ticket.fecha_entrada ? formatearFecha(ticket.fecha_entrada) : 'Todo el festival'}</td>
                <td>{formatearPrecio(ticket.precio)}</td>
                <td>
                  <span className={`badge ${COLOR_ESTADO[ticket.estado] ?? 'bg-secondary'}`}>{ticket.estado}</span>
                  {ticket.fecha_uso && (
                    <div className="text-muted small">Usado el {formatearFechaHora(ticket.fecha_uso)}</div>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </>
  );
}

export default MisTickets;
