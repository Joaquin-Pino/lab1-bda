import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { mensajeError } from '../../http-common.js';
import tiposEntradaService from '../../services/tiposEntrada.service.js';
import festivalService from '../../services/festival.service.js';
import formato from '../../utils/formato.js';

function EditarEntrada() {
  const { idFestival, idTipoEntrada } = useParams();
  const navigate = useNavigate(); // hook de React Router para redirigir a otra ruta

  // Declaración de estados para los campos del formulario
  const [nombre, setNombre] = useState('');
  const [precioActual, setPrecioActual] = useState('');
  const [aforoTotal, setAforoTotal] = useState('');
  const [fecha, setFecha] = useState('');
  
  // Estado para manejar errores y fechas disponibles del festival
  const [errorMensaje, setErrorMensaje] = useState('');
  const [fechasFestival, setFechasFestival] = useState([]);

  const handleEditarEntrada = (e) => {
    e.preventDefault();
    setErrorMensaje(''); // Limpiamos errores previos

    // Datos del festival a crear
    const entradaData = {
      nombre_tipo: nombre,
      precio_actual: precioActual,
      aforo_total: aforoTotal,
      fecha: fecha === 'Todo el festival' ? null : fecha
    };

    // Llamada al servicio para editar el tipo de entrada
    tiposEntradaService.editarTipoEntrada(idTipoEntrada, entradaData)
      .then(response => {
        alert("Entrada editada exitosamente.");
        navigate(`/festivales-produccion/${idFestival}`); // Redirigimos al usuario a la lista de festivales
      })
      .catch(error => {
        setErrorMensaje(mensajeError(error, "Error al editar la entrada."));
      });
  };

  // Cargar los datos actuales de la Entrada y las fechas que permite el festival, mostrar un error si la fecha no es válida
  useEffect(() => {
    tiposEntradaService.obtenerTipoEntrada(idTipoEntrada)
      .then(response => {
        const tipoEntrada = response.data;
        setNombre(tipoEntrada.nombre_tipo);
        setPrecioActual(tipoEntrada.precio_actual);
        setAforoTotal(tipoEntrada.aforo_total);
        setFecha(tipoEntrada.fecha === null ? 'Todo el festival' : tipoEntrada.fecha);
      })
      .catch(error => {
        setErrorMensaje(mensajeError(error, "Error al cargar los datos de la entrada."));
      });

    festivalService.obtener(idFestival)
      .then(response => {
        const festival = response.data;
        // Guardar cada dia entre la fecha de inicio y la de fin del festival en fechasFestival
        const fechas = [];
        for (let day = new Date(festival.fecha_inicio); day <= new Date(festival.fecha_fin); day.setDate(day.getDate() + 1)) {
          // Guardar las fechas crudas para enviarlas al backend
          fechas.push(day.toISOString().slice(0, 10));
        }
        setFechasFestival(fechas);
      })
      .catch(error => {
        setErrorMensaje(mensajeError(error, "No se pudo cargar las fechas del festival."));
      });
  }, [idFestival]);

  return (
    <div className="container d-flex justify-content-center align-items-center vh-100">
      <div className="card shadow p-4" style={{ maxWidth: '400px', width: '100%' }}>
        <h2 className="text-center mb-4">Editar Entrada</h2>
        
        {/* Mostrar alerta si hay error */}
        {errorMensaje && <div className="alert alert-danger">{errorMensaje}</div>}

        {/* crear formulario para crear festival */}
        <form onSubmit={handleEditarEntrada}>
          {/* nombre del festival */}
          <div className="mb-3">
            <label className="form-label">Nombre de la Entrada</label>
            <input 
              type="text" 
              className="form-control" 
              value={nombre}
              onChange={(e) => setNombre(e.target.value)}
              required
            />
          </div>
          {/* precio de la entrada */}
          <div className="mb-3">
            <label className="form-label">Precio de la Entrada</label>
            <input 
              type="number" 
              min="1" 
              step="1" 
              className="form-control" 
              value={precioActual}
              onChange={(e) => setPrecioActual(e.target.value)}
              required
            />
          </div>
          {/* aforo total de la entrada, no puede ser negativo */}
          <div className="mb-3">
            <label className="form-label">Aforo Total</label>
            <input 
              type="number" 
              min="1" 
              step="1" 
              className="form-control" 
              value={aforoTotal}
              onChange={(e) => setAforoTotal(e.target.value)}
              required
            />
          </div>
          {/* fecha de la entrada */}
          <div className="mb-3">
            <label className="form-label">Fecha</label>
            <select 
              className="form-control" 
              value={fecha}
              onChange={(e) => setFecha(e.target.value)}
              required
            >
              <option value="">Seleccionar fecha</option>
              <option value="Todo el festival">Todo el festival</option>
              
              {fechasFestival.map((fechaRaw, index) => (
                <option key={index} value={fechaRaw}>
                  {formato.formatearFecha(fechaRaw)}
                </option>
              ))}
            </select>
          </div>
          <button type="submit" className="btn btn-primary w-100 mb-3">Editar Entrada</button>
        </form>
      </div>
    </div>
  );
}

export default EditarEntrada;