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
    // Obtenemos el usuario logueado desde el localStorage
    const usuarioLogueado = JSON.parse(localStorage.getItem('usuario'));
    // Obtenemos el ID del productor actual (usuario logeado)
    const idProductorActual = usuarioLogueado ? usuarioLogueado.id_usuario : null;

    festivalService.listar()
      .then(response => {
        // Comprobar que el rol del usuario logeado es PRODUCTOR antes de filtrar
        if (!usuarioLogueado || usuarioLogueado.rol !== 'PRODUCTOR') {
          setErrorMensaje("No tienes permisos para ver estos festivales.");
          setFestivales([]); // Limpiar la lista de festivales
          return;
        }

        // Aplicar el filtro comparando la llave de tu DTO con el ID local
        const festivalesFiltrados = response.data.filter(
          festival => festival.id_usuario_productor === idProductorActual
        );
        
        // Guardar solo los filtrados en el estado
        setFestivales(festivalesFiltrados);
      })
      .catch(error => setErrorMensaje(mensajeError(error, "No se pudieron cargar los festivales.")))
      .finally(() => setCargando(false));
  }, []);

  if (cargando) return <p>Cargando festivales...</p>;
  if (errorMensaje) return <div className="alert alert-danger">{errorMensaje}</div>;

  return (
    <>
      <h2 className="mb-4">Festivales</h2>
      
      <td className="text-end">
        <Link to="/festivales-produccion/nuevo" className="btn btn-primary btn-sm">
           Crear nuevo festival
        </Link>
      </td>
      
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
              <th>Estado</th>
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
                <td>{festival.estado}</td>
                <td className="text-end">
                  <Link to={`/festivales-produccion/${festival.id_festival}`} className="btn btn-primary btn-sm">
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

export default FestivalesProduccion;
