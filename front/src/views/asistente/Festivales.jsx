import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import festivalService from '../../services/festival.service.js';
import { mensajeError } from '../../http-common.js';

// "2026-11-14" -> "14-11-2026" (sin pasar por Date para no correr el día por la zona horaria)
const formatearFecha = (fecha) => fecha.split('-').reverse().join('-');

function Festivales() {
  const [festivales, setFestivales] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [errorMensaje, setErrorMensaje] = useState('');

  useEffect(() => {
    festivalService.listar()
      .then(response => setFestivales(response.data))
      .catch(error => setErrorMensaje(mensajeError(error, "No se pudieron cargar los festivales.")))
      .finally(() => setCargando(false));
  }, []);

  if (cargando) return <p>Cargando festivales...</p>;
  if (errorMensaje) return <div className="alert alert-danger">{errorMensaje}</div>;

  return (
    <>
      <h2 className="mb-4">Festivales</h2>

      {festivales.length === 0 ? (
        <p className="text-muted">No hay festivales publicados por ahora.</p>
      ) : (
        <table className="table table-hover align-middle">
          <thead>
            <tr>
              <th>Nombre</th>
              <th>Recinto</th>
              <th>Inicio</th>
              <th>Término</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {festivales.map(festival => (
              <tr key={festival.id_festival}>
                <td>{festival.nombre}</td>
                <td>{festival.nombre_recinto}</td>
                <td>{formatearFecha(festival.fecha_inicio)}</td>
                <td>{formatearFecha(festival.fecha_fin)}</td>
                <td className="text-end">
                  <Link to={`/festivales/${festival.id_festival}`} className="btn btn-primary btn-sm">
                    Ver
                  </Link>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </>
  );
}

export default Festivales;
