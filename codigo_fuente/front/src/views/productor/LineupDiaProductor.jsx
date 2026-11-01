import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import festivalService from '../../services/festival.service.js';
import presentacionService from '../../services/presentacion.service.js';
import { mensajeError } from '../../http-common.js';
import { formatearFecha, formatearHora, formatearPrecio } from '../../utils/formato.js';

// Días del festival entre dos fechas "YYYY-MM-DD", ambas incluidas.
// Se calcula en UTC para que la zona horaria del navegador no corra el día.
const diasEntre = (inicio, fin) => {
  const dias = [];
  for (let d = new Date(`${inicio}T00:00:00Z`); d <= new Date(`${fin}T00:00:00Z`); d.setUTCDate(d.getUTCDate() + 1)) {
    dias.push(d.toISOString().slice(0, 10));
  }
  return dias;
};

// Lineup de un festival: el usuario elige un día y ve las presentaciones de cada escenario
function LineupDiaProductor({ festival }) {
  const dias = diasEntre(festival.fecha_inicio, festival.fecha_fin);

  const [dia, setDia] = useState(dias[0]);
  const [grilla, setGrilla] = useState(null);
  const [errorMensaje, setErrorMensaje] = useState('');
  // Al sumarle 1 se vuelve a ejecutar el useEffect y se recarga la grilla (después de eliminar o cancelar)
  const [recargar, setRecargar] = useState(0);

  useEffect(() => {
    setErrorMensaje('');
    festivalService.obtenerGrilla(festival.id_festival, dia)
      .then(response => setGrilla(response.data))
      .catch(error => setErrorMensaje(mensajeError(error, "No se pudo cargar la grilla.")));
  }, [festival.id_festival, dia, recargar]);

  // Eliminar borra la presentación sin avisar ni compensar a nadie: sirve para corregir errores del line-up
  const handleEliminar = (p) => {
    if (!window.confirm(`¿Eliminar la presentación de ${p.artista}? Los asistentes NO serán notificados ni compensados. Si el festival ya vendió entradas, usa "Cancelar".`)) return;
    setErrorMensaje('');
    presentacionService.eliminarPresentacion(p.id_presentacion)
      .then(() => setRecargar(r => r + 1))
      .catch(error => setErrorMensaje(mensajeError(error, "No se pudo eliminar la presentación.")));
  };

  // Cancelar llama al procedimiento de la Persona 4: la marca CANCELADA, notifica y da crédito a los asistentes
  const handleCancelar = (p) => {
    if (!window.confirm(`¿Cancelar la presentación de ${p.artista}? Se notificará y compensará con crédito a los asistentes con entradas para ese día.`)) return;
    setErrorMensaje('');
    presentacionService.cancelarPresentacion(p.id_presentacion)
      .then(response => {
        const resultado = response.data;
        alert(`Presentación cancelada. Asistentes notificados: ${resultado.asistentes_notificados}. `
          + `Crédito otorgado: ${formatearPrecio(resultado.credito_total_otorgado)}.`);
        setRecargar(r => r + 1);
      })
      .catch(error => setErrorMensaje(mensajeError(error, "No se pudo cancelar la presentación.")));
  };

  return (
    <>
      <h4 className="mt-5 mb-3">Lineup</h4>

      {/* Un boton para crear una presentacion */}
      <div className="text-muted mb-3">
        <Link to={`/festivales-produccion/${festival.id_festival}/crear-presentacion`} className="btn btn-secondary mb-3">
          Crear Presentación
        </Link>
      </div>
      

      {/* Un botón por día del festival */}
      <div className="btn-group mb-3">
        {dias.map((d, i) => (
          <button
            key={d}
            className={`btn ${d === dia ? 'btn-dark' : 'btn-outline-dark'}`}
            onClick={() => setDia(d)}
          >
            Día {i + 1} ({formatearFecha(d)})
          </button>
        ))}
      </div>

      {errorMensaje && <div className="alert alert-danger">{errorMensaje}</div>}

      {grilla && !errorMensaje && (
        <div className="row">
          {/* Una columna por escenario */}
          {grilla.escenarios.map(escenario => (
            <div key={escenario.id_escenario} className="col-md mb-3">
              <div className="card h-100">
                <div className="card-header fw-bold">{escenario.nombre}</div>
                <ul className="list-group list-group-flush">
                  {escenario.presentaciones.length === 0 && (
                    <li className="list-group-item text-muted">Sin presentaciones este día</li>
                  )}
                  {escenario.presentaciones.map(p => {
                    const cancelada = p.estado === 'CANCELADA';
                    return (
                      <li key={p.id_presentacion} className="list-group-item">
                        {/* Si está cancelada se muestra tachada y en gris */}
                        <div className={cancelada ? 'text-decoration-line-through text-muted' : ''}>
                          <div className="text-muted small">
                            {formatearHora(p.fecha_hora_inicio)} – {formatearHora(p.fecha_hora_fin)}
                          </div>
                          <div>
                            {p.artista}
                            {p.genero_musical && <span className="text-muted small"> · {p.genero_musical}</span>}
                          </div>
                        </div>
                        {cancelada && <span className="badge bg-secondary">Cancelada</span>}

                        {/* Acciones: una presentación cancelada ya no se puede editar ni cancelar otra vez */}
                        <div className="mt-2">
                          {!cancelada && (
                            <>
                              <Link to={`/festivales-produccion/${festival.id_festival}/editar-presentacion/${p.id_presentacion}`}
                                    className="btn btn-outline-primary btn-sm me-2">
                                Editar
                              </Link>
                              <button className="btn btn-outline-warning btn-sm me-2" onClick={() => handleCancelar(p)}>
                                Cancelar
                              </button>
                            </>
                          )}
                          <button className="btn btn-outline-danger btn-sm" onClick={() => handleEliminar(p)}>
                            Eliminar
                          </button>
                        </div>
                      </li>
                    );
                  })}
                </ul>
              </div>
            </div>
          ))}
        </div>
      )}
    </>
  );
}

export default LineupDiaProductor;
