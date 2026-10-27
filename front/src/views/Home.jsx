import { Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

function Home() {
  const navigate = useNavigate();
  const { rol, cerrarSesion } = useAuth();

  // El asistente tiene su propia vista
  if (rol === 'ASISTENTE') return <Navigate to="/festivales" replace />;

  // El productor tiene su propia vista
  if (rol === 'PRODUCTOR') return <Navigate to="/festivales-produccion" replace />;

  // El administrador tiene su propia vista
  if (rol === 'ADMINISTRADOR') return <Navigate to="/dashboard" replace />;

  const handleLogout = () => {
    // Limpiamos la sesión y devolvemos al usuario a la ruta raíz (Login)
    cerrarSesion();
    navigate('/');
  };

  return (
    <div className="container mt-5">
      <div className="p-5 text-center bg-light rounded shadow-sm">
        <h1 className="display-4">¡Bienvenido al Inicio!</h1>
        <p className="lead">Has iniciado sesión correctamente. Esta ruta es /home.</p>
        <hr className="my-4" />
        <p>Toda la navegación ocurrió sin recargar la pestaña del navegador.</p>
        <button onClick={handleLogout} className="btn btn-danger btn-lg mt-3">
          Cerrar Sesión
        </button>
      </div>
    </div>
  );
}

export default Home;