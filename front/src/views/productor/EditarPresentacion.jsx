import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { mensajeError } from '../../http-common.js';
import festivalService from '../../services/festival.service.js';
import escenarioService from '../../services/escenario.service.js';
import artistaService from '../../services/artista.service.js';
import presentacionService from '../../services/presentacion.service.js';
import { formatearFecha } from '../../utils/formato.js';
import { diasEntre, armarHorario } from '../../utils/horario.js';

// Formulario para cambiar escenario, artista u horario de una presentación (no cambia festival ni estado)
function EditarPresentacion() {
  const { idFestival, idPresentacion } = useParams();
  const navigate = useNavigate();

  // Datos para llenar las listas del formulario
  const [dias, setDias] = useState([]);
  const [escenarios, setEscenarios] = useState([]);
  const [artistas, setArtistas] = useState([]);

  // Campos del formulario
  const [dia, setDia] = useState('');
  const [idEscenario, setIdEscenario] = useState('');
  const [idArtista, setIdArtista] = useState('');
  const [horaInicio, setHoraInicio] = useState('');
  const [horaFin, setHoraFin] = useState('');

  const [errorMensaje, setErrorMensaje] = useState('');

  // Al abrir la vista: cargar el festival (sus días y su recinto), sus escenarios y los artistas
  useEffect(() => {
    festivalService.obtener(idFestival)
      .then(response => {
        const festival = response.data;
        setDias(diasEntre(festival.fecha_inicio, festival.fecha_fin));
        // Solo los escenarios del recinto del festival (el trigger rechaza los de otro recinto)
        return escenarioService.listarPorRecinto(festival.id_recinto);
      })
      .then(response => setEscenarios(response.data))
      .catch(error => setErrorMensaje(mensajeError(error, "No se pudo cargar el festival.")));

    artistaService.listarArtistas()
      .then(response => setArtistas(response.data))
      .catch(error => setErrorMensaje(mensajeError(error, "No se pudieron cargar los artistas.")));

 
    presentacionService.obtenerPresentacion(idPresentacion)
      .then(response => {
        const p = response.data;
        setDia(p.dia);
        setIdEscenario(String(p.id_escenario));
        setIdArtista(String(p.id_artista));
        setHoraInicio(p.fecha_hora_inicio.slice(11, 16));
        setHoraFin(p.fecha_hora_fin.slice(11, 16));
      })
      .catch(error => setErrorMensaje(mensajeError(error, "No se pudo cargar la presentación.")));
  }, [idFestival, idPresentacion]);

  const handleEditarPresentacion = (e) => {
    e.preventDefault();
    setErrorMensaje('');

    // Al editar no se envía id_festival: una presentación no puede cambiarse de festival
    const presentacionData = {
      id_escenario: Number(idEscenario),
      id_artista: Number(idArtista),
      ...armarHorario(dia, horaInicio, horaFin)
    };

    presentacionService.editarPresentacion(idPresentacion, presentacionData)
      .then(() => {
        alert("Presentación actualizada exitosamente.");
        navigate(`/festivales-produccion/${idFestival}`);
      })
      .catch(error => {
        // Si choca con otra presentación, aquí llega el mensaje del trigger.
        setErrorMensaje(mensajeError(error, "Error al editar la presentación."));
      });
  };

  return (
    <div className="container d-flex justify-content-center align-items-center vh-100">
      <div className="card shadow p-4" style={{ maxWidth: '400px', width: '100%' }}>
        <h2 className="text-center mb-4">Editar Presentación</h2>

        {/* Mostrar alerta si hay error */}
        {errorMensaje && <div className="alert alert-danger">{errorMensaje}</div>}

        <form onSubmit={handleEditarPresentacion}>
          {/* día del festival */}
          <div className="mb-3">
            <label className="form-label">Día</label>
            <select className="form-select" value={dia} onChange={(e) => setDia(e.target.value)} required>
              <option value="">Seleccionar día</option>
              {dias.map((d, i) => (
                <option key={d} value={d}>Día {i + 1} ({formatearFecha(d)})</option>
              ))}
            </select>
          </div>

          {/* escenario */}
          <div className="mb-3">
            <label className="form-label">Escenario</label>
            <select className="form-select" value={idEscenario} onChange={(e) => setIdEscenario(e.target.value)} required>
              <option value="">Seleccionar escenario</option>
              {escenarios.map(escenario => (
                <option key={escenario.id_escenario} value={escenario.id_escenario}>{escenario.nombre}</option>
              ))}
            </select>
          </div>

          {/* artista */}
          <div className="mb-3">
            <label className="form-label">Artista</label>
            <select className="form-select" value={idArtista} onChange={(e) => setIdArtista(e.target.value)} required>
              <option value="">Seleccionar artista</option>
              {artistas.map(artista => (
                <option key={artista.id_artista} value={artista.id_artista}>
                  {artista.nombre} ({artista.genero_musical})
                </option>
              ))}
            </select>
          </div>

          {/* horario */}
          <div className="row mb-3">
            <div className="col">
              <label className="form-label">Hora de inicio</label>
              <input type="time" className="form-control" value={horaInicio}
                     onChange={(e) => setHoraInicio(e.target.value)} required />
            </div>
            <div className="col">
              <label className="form-label">Hora de término</label>
              <input type="time" className="form-control" value={horaFin}
                     onChange={(e) => setHoraFin(e.target.value)} required />
            </div>
          </div>

          <button type="submit" className="btn btn-primary w-100 mb-3">Guardar Cambios</button>
          <Link to={`/festivales-produccion/${idFestival}`} className="btn btn-outline-secondary w-100">Volver</Link>
        </form>
      </div>
    </div>
  );
}

export default EditarPresentacion;