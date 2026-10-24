import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

// Barra de navegación común a todas las pantallas del productor.
// Las pantallas se dibujan debajo, en el <Outlet />.
function LayoutProductor() {
  const navigate = useNavigate();
  const { usuario, cerrarSesion } = useAuth();
  
  // Si el usuario hace click en "Cerrar sesión", se borra la sesión y se redirige al login.
  const handleLogout = () => {
    cerrarSesion();
    navigate('/');
  };

  return (
    <>
      <nav className="navbar navbar-expand navbar-dark bg-dark mb-4">
        <div className="container">
          // Nombre de la aplicación y enlaces a las distintas pantallas del productor. Se usa NavLink para que se aplique la clase "active" al enlace correspondiente a la pantalla actual.
          <span className="navbar-brand">Festivales</span>
          <ul className="navbar-nav me-auto">
            <li className="nav-item">
              <NavLink to="/festivales-produccion" className="nav-link">Festivales</NavLink>
            </li>
            <li className="nav-item">
              <NavLink to="/mis-festivales" className="nav-link">Mis Festivales</NavLink>
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

export default LayoutProductor;