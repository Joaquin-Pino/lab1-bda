import { useEffect, useState } from 'react';
import { 
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer,
  PieChart, Pie, Cell
} from 'recharts';
// Asegúrate de que esta ruta coincida con la ubicación real de tu servicio
import reporteService from '../../services/reporte.service.js';
import { useAuth } from '../../context/AuthContext.jsx';
import { Navigate } from 'react-router-dom';

function DashboardAdmin() {
  const [datos, setDatos] = useState([]);
  const [cargando, setCargando] = useState(true);

  // Confirmar que el administrador esta en la vista
  const { rol } = useAuth();
  if (rol === 'ASISTENTE') return <Navigate to="/festivales" replace />;
  if (rol === 'PRODUCTOR') return <Navigate to="/festivales-produccion" replace />;

  // Cargar los datos del reporte al montar el componente
  useEffect(() => {
    reporteService.obtenerReporte()
      .then(response => {
        setDatos(response.data);
      })
      .catch(error => console.error("Error cargando reporte:", error))
      .finally(() => setCargando(false));
  }, []);
 
  // Función para refrescar los datos del reporte
  const manejarRefresco = () => {
    setCargando(true);
    reporteService.actualizarReporte()
      .then(response => {
        // Gracias al cambio que hicimos en el backend, la petición POST 
        // ya nos devuelve la tabla fresca inmediatamente
        setDatos(response.data); 
      })
      .catch(error => console.error("Error refrescando reporte:", error))
      .finally(() => setCargando(false));
  };

  // Mostrar un spinner mientras se cargan los datos
  if (cargando) return <div className="text-center mt-5 spinner-border text-primary" role="status"></div>;

  // Preparar datos para el gráfico de barras (Concatenamos Festival + Entrada para los *tooltips*)
  const datosBarras = datos.map(d => ({
    nombreCompleto: `${d.festival_nombre} - ${d.tipo_entrada}`,
    recaudacion: d.recaudacion_total
  }));

  // Preparar datos para el gráfico de anillo (Ocupación Global calculada en el momento)
  const totalAforo = datos.reduce((sum, d) => sum + d.aforo_total, 0);
  const totalVendidos = datos.reduce((sum, d) => sum + d.tickets_vendidos, 0);
  const totalDisponibles = totalAforo - totalVendidos;
  
  const datosAnillo = [
    { name: 'Tickets Vendidos', value: totalVendidos },
    { name: 'Disponibles', value: totalDisponibles }
  ];
  const coloresAnillo = ['#198754', '#dee2e6']; // Verde 'success' y gris claro de Bootstrap

  // Renderizamos la vista del Dashboard
  return (
    <div className="container mt-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>Dashboard de Reportes</h2>
        {/* Botón para actualizar los datos del reporte */}
        <button className="btn btn-dark" onClick={manejarRefresco} disabled={cargando}>
          Actualizar Datos
        </button>
      </div>

      <div className="row mb-4">
        {/* Gráfico de Barras - Recaudación (Ocupa 8 columnas de 12) */}
        <div className="col-lg-8 mb-3">
          <div className="card shadow h-100 p-3">
            <h5 className="mb-3">Recaudación por Tipo de Entrada</h5>
            <div style={{ width: '100%', height: 300 }}>
              <ResponsiveContainer>
                <BarChart data={datosBarras} margin={{ top: 10, right: 10, left: 10, bottom: 5 }}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="nombreCompleto" hide /> {/* Ocultamos texto del eje X para que no se sobreponga */}
                  <YAxis />
                  <Tooltip formatter={(value) => `$${value.toLocaleString()}`} />
                  <Legend />
                  <Bar dataKey="recaudacion" fill="#0d6efd" name="Recaudación ($)" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>

        {/* Gráfico de Anillo - Ocupación Global (Ocupa 4 columnas de 12) */}
        <div className="col-lg-4 mb-3">
          <div className="card shadow h-100 p-3">
            <h5 className="mb-3">Ocupación Global</h5>
            <div style={{ width: '100%', height: 300 }}>
              <ResponsiveContainer>
                <PieChart>
                  <Pie
                    data={datosAnillo}
                    innerRadius={60} /* Esto convierte el Pie en un anillo (Donut) */
                    outerRadius={100}
                    paddingAngle={5}
                    dataKey="value"
                  >
                    {datosAnillo.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={coloresAnillo[index]} />
                    ))}
                  </Pie>
                  <Tooltip />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>
      </div>

      {/* Tabla Maestra */}
      <div className="card shadow p-3 mb-5">
        <h5 className="mb-3">Detalle por Festival</h5>
        <div className="table-responsive">
          <table className="table table-hover align-middle">
            <thead className="table-dark">
              <tr>
                <th>Festival</th>
                <th>Tipo Entrada</th>
                <th>Aforo Total</th>
                <th>Vendidos</th>
                <th>Recaudación</th>
                <th style={{ width: '25%' }}>Ocupación</th>
              </tr>
            </thead>
            <tbody>
              {datos.map((fila, idx) => {
                // Lógica de colores para la barra de progreso de Bootstrap
                let colorBarra = "bg-primary";
                if (fila.porcentaje_ocupacion >= 90) colorBarra = "bg-danger"; // Rojo: Casi sold out
                else if (fila.porcentaje_ocupacion >= 60) colorBarra = "bg-success"; // Verde: Buenas ventas
                else if (fila.porcentaje_ocupacion < 30) colorBarra = "bg-warning"; // Amarillo: Ventas lentas

                return (
                  <tr key={idx}>
                    <td>{fila.festival_nombre}</td>
                    <td>{fila.tipo_entrada}</td>
                    <td>{fila.aforo_total.toLocaleString()}</td>
                    <td>{fila.tickets_vendidos.toLocaleString()}</td>
                    <td>${fila.recaudacion_total.toLocaleString()}</td>
                    <td>
                      <div className="d-flex align-items-center">
                        <span className="me-2" style={{ width: '50px' }}>{fila.porcentaje_ocupacion}%</span>
                        <div className="progress flex-grow-1" style={{ height: '8px' }}>
                          <div 
                            className={`progress-bar ${colorBarra}`} 
                            style={{ width: `${fila.porcentaje_ocupacion}%` }}
                          ></div>
                        </div>
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

export default DashboardAdmin;