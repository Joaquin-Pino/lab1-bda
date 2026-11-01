import { useEffect, useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import notificacionService, { NOTIFICACIONES_CAMBIARON } from '../services/notificacion.service.js';

// Barra de navegación común a todas las pantallas del asistente.
// Las pantallas se dibujan debajo, en el <Outlet />.
function LayoutAsistente() {
  const navigate = useNavigate();
  const { usuario, cerrarSesion } = useAuth();

  const [noLeidas, setNoLeidas] = useState(0);

  // Contador de la campana: se carga al montar y cada vez que la vista de notificaciones avisa un cambio
  useEffect(() => {
    const actualizar = () =>
      notificacionService.contarNoLeidas()
        .then(response => setNoLeidas(response.data.no_leidas))
        .catch(() => {}); // si falla, la campana simplemente no muestra contador
    actualizar();
    window.addEventListener(NOTIFICACIONES_CAMBIARON, actualizar);
    return () => window.removeEventListener(NOTIFICACIONES_CAMBIARON, actualizar);
  }, []);

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
              <NavLink to="/festivales" className="nav-link">Festivales</NavLink>
            </li>
            <li className="nav-item">
              <NavLink to="/mis-tickets" className="nav-link">Mis tickets</NavLink>
            </li>
            <li className="nav-item">
              <NavLink to="/mis-ordenes" className="nav-link">Mis órdenes</NavLink>
            </li>
            <li className="nav-item">
              <NavLink to="/mi-perfil" className="nav-link">Mi perfil</NavLink>
            </li>
          </ul>
          <NavLink to="/mis-notificaciones" className="nav-link text-light position-relative me-3" title="Notificaciones">
            🔔
            {noLeidas > 0 && (
              <span className="badge rounded-pill bg-danger position-absolute top-0 start-100 translate-middle">
                {noLeidas}
              </span>
            )}
          </NavLink>
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

export default LayoutAsistente;
