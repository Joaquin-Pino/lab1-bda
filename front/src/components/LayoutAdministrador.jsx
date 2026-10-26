import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

// Barra de navegación común a todas las pantallas del administrador.
// Las pantallas se dibujan debajo, en el <Outlet />.
function LayoutAdministrador() {
  const navigate = useNavigate();
  const { usuario, cerrarSesion } = useAuth();

  const handleLogout = () => {
    cerrarSesion();
    navigate('/');
  };

  return (
    <>
      <nav className="navbar navbar-expand navbar-dark bg-dark mb-4">
        <div className="container">
          <span className="navbar-brand">Festivales</span>
          <ul className="navbar-nav me-auto">
            <li className="nav-item">
              <NavLink to="/dashboard" className="nav-link">Dashboard</NavLink>
            </li>
            <li className="nav-item">
              <NavLink to="/control-acceso" className="nav-link">Control de acceso</NavLink>
            </li>
          </ul>
          <span className="navbar-text text-light me-3">{usuario?.nombre_completo}</span>
          <button onClick={handleLogout} className="btn btn-outline-light btn-sm">
            Cerrar sesión
          </button>
        </div>
      </nav>
      <div className="container">
        <Outlet />
      </div>
    </>
  );
}

export default LayoutAdministrador;
