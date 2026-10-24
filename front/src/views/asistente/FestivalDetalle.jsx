import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import festivalService from '../../services/festival.service.js';
import compraService from '../../services/compra.service.js';
import { mensajeError } from '../../http-common.js';
import { formatearFecha, formatearPrecio } from '../../utils/formato.js';
import LineupDia from './LineupDia.jsx';

const MAX_POR_COMPRA = 10; // límite del backend (CompraRequest)

function FestivalDetalle() {
  const { id } = useParams(); // id del festival que viene en la URL

  const [festival, setFestival] = useState(null);
  const [tiposEntrada, setTiposEntrada] = useState([]);
  const [saldo, setSaldo] = useState(0);
  const [cargando, setCargando] = useState(true);
  const [errorMensaje, setErrorMensaje] = useState('');

  // Estado de la compra
  const [cantidades, setCantidades] = useState({}); // { id_tipo_entrada: cantidad }
  const [usarCredito, setUsarCredito] = useState(false);
  const [comprandoId, setComprandoId] = useState(null); // fila con la compra en curso
  const [orden, setOrden] = useState(null); // última orden comprada
  const [errorCompra, setErrorCompra] = useState('');

  useEffect(() => {
    // Se piden el festival, sus tipos de entrada y el saldo al mismo tiempo
    Promise.all([
      festivalService.obtener(id),
      festivalService.listarTiposEntrada(id),
      compraService.perfil(),
    ])
      .then(([respFestival, respTipos, respPerfil]) => {
        setFestival(respFestival.data);
        setTiposEntrada(respTipos.data);
        setSaldo(Number(respPerfil.data.creditos_compensacion));
      })
      .catch(error => setErrorMensaje(mensajeError(error, "No se pudo cargar el festival.")))
      .finally(() => setCargando(false));
  }, [id]);

  const handleComprar = (tipo) => {
    setErrorCompra('');
    setOrden(null);
    setComprandoId(tipo.id_tipo_entrada);

    const cantidad = cantidades[tipo.id_tipo_entrada] ?? 1;
    compraService.comprar(tipo.id_tipo_entrada, cantidad, usarCredito)
      .then(response => {
        setOrden(response.data);
        setUsarCredito(false);
        // Se recargan los cupos y el saldo, que cambiaron con la compra
        return Promise.all([festivalService.listarTiposEntrada(id), compraService.perfil()]);
      })
      .then(([respTipos, respPerfil]) => {
        setTiposEntrada(respTipos.data);
        setSaldo(Number(respPerfil.data.creditos_compensacion));
      })
      .catch(error => setErrorCompra(mensajeError(error, "No se pudo realizar la compra.")))
      .finally(() => setComprandoId(null));
  };

  if (cargando) return <p>Cargando festival...</p>;
  if (errorMensaje) {
    return (
      <>
        <div className="alert alert-danger">{errorMensaje}</div>
        <Link to="/festivales">Volver a festivales</Link>
      </>
    );
  }

  return (
    <>
      <Link to="/festivales" className="d-inline-block mb-3">&larr; Volver a festivales</Link>

      <h2>{festival.nombre}</h2>
      <p className="text-muted">
        {festival.nombre_recinto} · {formatearFecha(festival.fecha_inicio)} al {formatearFecha(festival.fecha_fin)}
      </p>

      <h4 className="mt-4 mb-3">Entradas</h4>

      {orden && (
        <div className="alert alert-success">
          <strong>Compra realizada (orden #{orden.id_orden}).</strong>{' '}
          Total: {formatearPrecio(orden.monto_total)}
          {Number(orden.monto_credito_aplicado) > 0 &&
            ` · Crédito aplicado: ${formatearPrecio(orden.monto_credito_aplicado)}`}
          <div className="mt-2">Códigos de tus tickets:</div>
          <ul className="mb-0">
            {orden.tickets.map(ticket => (
              <li key={ticket.id_ticket}><code>{ticket.codigo_unico}</code></li>
            ))}
          </ul>
        </div>
      )}
      {errorCompra && <div className="alert alert-danger">{errorCompra}</div>}

      {tiposEntrada.length === 0 ? (
        <p className="text-muted">Este festival todavía no tiene entradas a la venta.</p>
      ) : (
        <>
          <div className="form-check mb-3">
            <input
              id="usarCredito"
              type="checkbox"
              className="form-check-input"
              checked={usarCredito}
              onChange={(e) => setUsarCredito(e.target.checked)}
              disabled={saldo <= 0}
            />
            <label htmlFor="usarCredito" className="form-check-label">
              Usar mi crédito de compensación (saldo: {formatearPrecio(saldo)})
            </label>
          </div>

          <table className="table align-middle">
            <thead>
              <tr>
                <th>Entrada</th>
                <th>Válida para</th>
                <th>Precio</th>
                <th>Disponibles</th>
                <th style={{ width: '200px' }}>Cantidad</th>
              </tr>
            </thead>
            <tbody>
              {tiposEntrada.map(tipo => {
                const agotado = tipo.aforo_disponible <= 0;
                const maximo = Math.min(MAX_POR_COMPRA, tipo.aforo_disponible);
                return (
                  <tr key={tipo.id_tipo_entrada}>
                    <td>{tipo.nombre_tipo}</td>
                    {/* fecha null = pase válido para todo el festival */}
                    <td>{tipo.fecha ? formatearFecha(tipo.fecha) : 'Todo el festival'}</td>
                    <td>{formatearPrecio(tipo.precio_actual)}</td>
                    <td>
                      {agotado ? <span className="badge bg-secondary">Agotado</span> : tipo.aforo_disponible}
                    </td>
                    <td>
                      {!agotado && (
                        <div className="input-group input-group-sm">
                          <input
                            type="number"
                            className="form-control"
                            min={1}
                            max={maximo}
                            value={cantidades[tipo.id_tipo_entrada] ?? 1}
                            onChange={(e) => setCantidades({
                              ...cantidades,
                              [tipo.id_tipo_entrada]: Math.min(maximo, Math.max(1, Number(e.target.value))),
                            })}
                          />
                          <button
                            className="btn btn-primary"
                            onClick={() => handleComprar(tipo)}
                            disabled={comprandoId !== null}
                          >
                            {comprandoId === tipo.id_tipo_entrada ? 'Comprando...' : 'Comprar'}
                          </button>
                        </div>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </>
      )}

      <LineupDia festival={festival} />
    </>
  );
}

export default FestivalDetalle;
