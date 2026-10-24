import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import festivalService from '../../services/festival.service.js';
import compraService from '../../services/compra.service.js';
import { mensajeError } from '../../http-common.js';
import { formatearFecha, formatearPrecio } from '../../utils/formato.js';
import LineupDiaProductor from './LineupDiaProductor.jsx';

function FestivalDetalle() {
  const { id } = useParams(); // id del festival que viene en la URL

  const [festival, setFestival] = useState(null);
  const [tiposEntrada, setTiposEntrada] = useState([]);
  const [saldo, setSaldo] = useState(0);
  const [cargando, setCargando] = useState(true);
  const [errorMensaje, setErrorMensaje] = useState('');

  useEffect(() => {
    // Se piden el festival, sus tipos de entrada y el saldo al mismo tiempo
    Promise.all([
      festivalService.obtener(id),
      festivalService.listarTiposEntrada(id)
    ])
      .then(([respFestival, respTipos]) => {
        setFestival(respFestival.data);
        setTiposEntrada(respTipos.data);
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

  // Si el festival no se pudo cargar, se muestra un mensaje de error y un link para volver a la lista de festivales
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
            <Link to={`/festivales-produccion/${festival.id_festival}/crear-entrada`} className="btn btn-primary btn-sm">
                    Crear un Tipo de Entrada
                  </Link>
          </div>

          <table className="table align-middle">
            <thead>
              <tr>
                <th>Entrada</th>
                <th>Válida para</th>
                <th>Precio</th>
                <th>Disponibles</th>
                <th>Cantidad</th>
              </tr>
            </thead>
            <tbody>
              {tiposEntrada.map(tipo => {
                const agotado = tipo.aforo_disponible <= 0;
                return (
                  <tr key={tipo.id_tipo_entrada}>
                    <td>{tipo.nombre_tipo}</td>
                    {/* fecha null = pase válido para todo el festival */}
                    <td>{tipo.fecha ? formatearFecha(tipo.fecha) : 'Todo el festival'}</td>
                    <td>{formatearPrecio(tipo.precio_actual)}</td>
                    {/* Si el tipo de entrada está agotado, se muestra un badge "Agotado" en vez del número de entradas disponibles */}
                    <td>
                      {agotado ? <span className="badge bg-secondary">Agotado</span> : tipo.aforo_disponible}
                    </td>
                    {/* Boton para editar este tipo de entrada*/}
                    <td>
                      <Link to={`/festivales-produccion/${festival.id_festival}/editar-entrada/${tipo.id_tipo_entrada}`} className="btn btn-outline-primary btn-sm">
                        Editar
                      </Link>
                    </td>
                    {/* Boton para eliminar*/}
                    <td>
                      <Link to={`/festivales-produccion/${festival.id_festival}/eliminar-entrada/${tipo.id_tipo_entrada}`} className="btn btn-outline-danger btn-sm">
                        Eliminar
                      </Link>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </>
      )}

      <LineupDiaProductor festival={festival} />
    </>
  );
}

export default FestivalDetalle;
