import { useEffect, useState } from 'react';
import notificacionService, { avisarCambio } from '../../services/notificacion.service.js';
import { mensajeError } from '../../http-common.js';
import { formatearFechaHora } from '../../utils/formato.js';

function MisNotificaciones() {
  const [notificaciones, setNotificaciones] = useState([]);
  const [soloNoLeidas, setSoloNoLeidas] = useState(false);
  const [cargando, setCargando] = useState(true);
  const [errorMensaje, setErrorMensaje] = useState('');

  const cargar = () =>
    notificacionService.listar(soloNoLeidas)
      .then(response => setNotificaciones(response.data))
      .catch(error => setErrorMensaje(mensajeError(error, "No se pudieron cargar tus notificaciones.")))
      .finally(() => setCargando(false));

  useEffect(() => {
    cargar();
  }, [soloNoLeidas]);

  // Ejecuta una acción, recarga la lista y avisa al navbar
  const ejecutar = (accion, mensajeFallo) => {
    setErrorMensaje('');
    accion()
      .then(() => cargar())
      .then(() => avisarCambio())
      .catch(error => setErrorMensaje(mensajeError(error, mensajeFallo)));
  };

  const hayNoLeidas = notificaciones.some(n => !n.leida);

  return (
    <>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2 className="mb-0">Mis notificaciones</h2>
        <div className="d-flex align-items-center gap-3">
          <div className="form-check mb-0">
            <input
              id="solo-no-leidas"
              type="checkbox"
              className="form-check-input"
              checked={soloNoLeidas}
              onChange={e => setSoloNoLeidas(e.target.checked)}
            />
            <label htmlFor="solo-no-leidas" className="form-check-label">Solo no leídas</label>
          </div>
          <button
            className="btn btn-outline-primary btn-sm"
            disabled={!hayNoLeidas}
            onClick={() => ejecutar(notificacionService.marcarTodasLeidas, "No se pudieron marcar como leídas.")}
          >
            Marcar todas como leídas
          </button>
        </div>
      </div>

      {errorMensaje && <div className="alert alert-danger">{errorMensaje}</div>}

      {cargando ? (
        <p>Cargando notificaciones...</p>
      ) : notificaciones.length === 0 ? (
        <p className="text-muted">
          {soloNoLeidas ? 'No tienes notificaciones sin leer.' : 'No tienes notificaciones.'}
        </p>
      ) : (
        <ul className="list-group">
          {notificaciones.map(n => (
            <li
              key={n.id}
              className={`list-group-item d-flex justify-content-between align-items-start ${n.leida ? '' : 'list-group-item-warning'}`}
            >
              <div>
                <div className={n.leida ? 'text-muted' : 'fw-bold'}>{n.mensaje}</div>
                <small className="text-muted">{formatearFechaHora(n.fecha_creacion)}</small>
              </div>
              <div className="d-flex gap-2 ms-3">
                {!n.leida && (
                  <button
                    className="btn btn-outline-primary btn-sm"
                    onClick={() => ejecutar(() => notificacionService.marcarLeida(n.id), "No se pudo marcar como leída.")}
                  >
                    Marcar leída
                  </button>
                )}
                <button
                  className="btn btn-outline-danger btn-sm"
                  onClick={() => ejecutar(() => notificacionService.eliminar(n.id), "No se pudo eliminar la notificación.")}
                >
                  Eliminar
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </>
  );
}

export default MisNotificaciones;
