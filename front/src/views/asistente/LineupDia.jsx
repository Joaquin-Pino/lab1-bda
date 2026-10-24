import { useEffect, useState } from 'react';
import festivalService from '../../services/festival.service.js';
import { mensajeError } from '../../http-common.js';
import { formatearFecha, formatearHora } from '../../utils/formato.js';

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
function LineupDia({ festival }) {
  const dias = diasEntre(festival.fecha_inicio, festival.fecha_fin);

  const [dia, setDia] = useState(dias[0]);
  const [grilla, setGrilla] = useState(null);
  const [errorMensaje, setErrorMensaje] = useState('');

  useEffect(() => {
    setErrorMensaje('');
    festivalService.obtenerGrilla(festival.id_festival, dia)
      .then(response => setGrilla(response.data))
      .catch(error => setErrorMensaje(mensajeError(error, "No se pudo cargar la grilla.")));
  }, [festival.id_festival, dia]);

  return (
    <>
      <h4 className="mt-5 mb-3">Lineup</h4>

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
                  {escenario.presentaciones.map(p => (
                    <li key={p.id_presentacion} className="list-group-item">
                      <div className="text-muted small">
                        {formatearHora(p.fecha_hora_inicio)} – {formatearHora(p.fecha_hora_fin)}
                      </div>
                      <div>
                        {p.artista}
                        {p.genero_musical && <span className="text-muted small"> · {p.genero_musical}</span>}
                      </div>
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          ))}
        </div>
      )}
    </>
  );
}

export default LineupDia;
