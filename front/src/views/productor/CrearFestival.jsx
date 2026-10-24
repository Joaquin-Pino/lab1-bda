import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import authService from '../../services/auth.service.js';
import { mensajeError } from '../../http-common.js';
import festivalService from '../../services/festival.service.js';
import recintoService from '../../services/recinto.service.js';

function CrearFestival() {
  const navigate = useNavigate(); // hook de React Router para redirigir a otra ruta

  // Declaración de estados para los campos del formulario
  const [nombre, setNombre] = useState('');
  const [recinto, setRecinto] = useState('');
  const [fechaInicio, setFechaInicio] = useState('');
  const [fechaFin, setFechaFin] = useState('');
  const [estado, setEstado] = useState('PUBLICADO'); // Estado inicial del festival
  
  // Estado para manejar errores y recintos
  const [errorMensaje, setErrorMensaje] = useState('Sucedió un error al crear el festival.');
  const [recintos, setRecintos] = useState([]);

  // Cargar recintos al montar el componente
  useEffect(() => {
    // Cargar recintos al montar el componente
    recintoService.listarRecintos()
      .then(response => {
        setRecintos(response.data);
      })
      .catch(error => {
        setErrorMensaje(mensajeError(error, "No se pudieron cargar los recintos."));
      });
  }, []);

  const handleCrearFestival = (e) => {
    e.preventDefault();
    setErrorMensaje(''); // Limpiamos errores previos

    // Datos del festival a crear
    const festivalData = {
      nombre: nombre,
      id_recinto: recinto, 
      fecha_inicio: fechaInicio,
      fecha_fin: fechaFin,
      estado: estado
    };

    // Llamada al servicio para crear el festival
    festivalService.crearFestival(festivalData)
      .then(response => {
        alert("Festival creado exitosamente.");
        navigate('/festivales'); // Redirigimos al usuario a la lista de festivales
      })
      .catch(error => {
        setErrorMensaje(mensajeError(error, "Error al crear el festival."));
      });
  };

  return (
    <div className="container d-flex justify-content-center align-items-center vh-100">
      <div className="card shadow p-4" style={{ maxWidth: '400px', width: '100%' }}>
        <h2 className="text-center mb-4">Crear Festival</h2>
        
        {/* Mostrar alerta si hay error */}
        {errorMensaje && <div className="alert alert-danger">{errorMensaje}</div>}

        {/* crear formulario para crear festival */}
        <form onSubmit={handleCrearFestival}>
          {/* nombre del festival */}
          <div className="mb-3">
            <label className="form-label">Nombre</label>
            <input 
              type="text" 
              className="form-control" 
              value={nombre}
              onChange={(e) => setNombre(e.target.value)}
              required
            />
          </div>
          {/* recinto del festival */}
          <div className="mb-3">
            <label className="form-label">Recinto</label>
            <select 
              className="form-control" 
              value={recinto}
              onChange={(e) => setRecinto(e.target.value)}
              required
            >
              <option value="">Seleccionar recinto</option>
              {recintos.map((recinto) => (
                <option key={recinto.id} value={recinto.id}>
                  {recinto.nombre}
                </option>
              ))}
            </select>
          </div>
          {/* fecha de inicio del festival */}
          <div className="mb-3">
            <label className="form-label">Fecha de Inicio</label>
            <input 
              type="date" 
              className="form-control" 
              value={fechaInicio}
              onChange={(e) => setFechaInicio(e.target.value)}
              required
            />
          </div>
          {/* fecha de fin del festival */}
          <div className="mb-3">
            <label className="form-label">Fecha de Fin</label>
            <input 
              type="date" 
              className="form-control" 
              value={fechaFin}
              onChange={(e) => setFechaFin(e.target.value)}
              required
            />
          </div>
          {/* estado del festival */}
          <div className="mb-3">
            <label className="form-label">Estado</label>
            <select 
              className="form-control" 
              value={estado}
              onChange={(e) => setEstado(e.target.value)}
              required
            >
              <option value="PUBLICADO">Publico</option>
              <option value="BORRADOR">borrador</option>
            </select>
          </div>
          <button type="submit" className="btn btn-primary w-100 mb-3">Crear Festival</button>
        </form>
      </div>
    </div>
  );
}

export default CrearFestival;