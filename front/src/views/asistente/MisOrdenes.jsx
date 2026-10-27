import { Fragment, useEffect, useState } from 'react';
import compraService from '../../services/compra.service.js';
import { mensajeError } from '../../http-common.js';
import { formatearFechaHora, formatearPrecio } from '../../utils/formato.js';

// Color del badge según el estado de la orden
const COLOR_ESTADO = { PAGADA: 'bg-success', PENDIENTE: 'bg-warning text-dark', CANCELADA: 'bg-danger' };

function MisOrdenes() {
  const [ordenes, setOrdenes] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [errorMensaje, setErrorMensaje] = useState('');
  const [abiertaId, setAbiertaId] = useState(null); // orden con sus tickets desplegados

  useEffect(() => {
    compraService.misOrdenes()
      .then(response => setOrdenes(response.data))
      .catch(error => setErrorMensaje(mensajeError(error, "No se pudieron cargar tus órdenes.")))
      .finally(() => setCargando(false));
  }, []);

  if (cargando) return <p>Cargando órdenes...</p>;
  if (errorMensaje) return <div className="alert alert-danger">{errorMensaje}</div>;

  return (
    <>
      <h2 className="mb-4">Mis órdenes</h2>

      {ordenes.length === 0 ? (
        <p className="text-muted">Todavía no tienes órdenes.</p>
      ) : (
        <table className="table align-middle">
          <thead>
            <tr>
              <th>Orden</th>
              <th>Fecha de compra</th>
              <th>Total</th>
              <th>Crédito aplicado</th>
              <th>Estado</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {ordenes.map(orden => {
              const abierta = abiertaId === orden.id_orden;
              return (
                <Fragment key={orden.id_orden}>
                  <tr>
                    <td>#{orden.id_orden}</td>
                    <td>{formatearFechaHora(orden.fecha_compra)}</td>
                    <td>{formatearPrecio(orden.monto_total)}</td>
                    <td>{formatearPrecio(orden.monto_credito_aplicado)}</td>
                    <td>
                      <span className={`badge ${COLOR_ESTADO[orden.estado] ?? 'bg-secondary'}`}>{orden.estado}</span>
                    </td>
                    <td className="text-end">
                      <button
                        className="btn btn-outline-primary btn-sm"
                        onClick={() => setAbiertaId(abierta ? null : orden.id_orden)}
                      >
                        {abierta ? 'Ocultar tickets' : `Ver tickets (${orden.tickets.length})`}
                      </button>
                    </td>
                  </tr>

                  {/* Fila extra con los tickets de la orden */}
                  {abierta && (
                    <tr>
                      <td colSpan={6} className="bg-light">
                        <ul className="mb-0">
                          {orden.tickets.map(ticket => (
                            <li key={ticket.id_ticket}>
                              <code>{ticket.codigo_unico}</code> · {formatearPrecio(ticket.precio)} · {ticket.estado}
                            </li>
                          ))}
                        </ul>
                      </td>
                    </tr>
                  )}
                </Fragment>
              );
            })}
          </tbody>
        </table>
      )}
    </>
  );
}

export default MisOrdenes;
