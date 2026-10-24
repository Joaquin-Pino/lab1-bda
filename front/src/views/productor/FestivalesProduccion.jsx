import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import festivalService from '../../services/festival.service.js';
import { mensajeError } from '../../http-common.js';
import { formatearFecha } from '../../utils/formato.js';

function FestivalesProduccion() {
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
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </>
  );
}

export default FestivalesProduccion;
