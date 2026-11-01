import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

// Protege un grupo de rutas. Sin sesión redirige al login; si se indican
// `roles` y el usuario no tiene uno de ellos, lo devuelve a /home.
// Uso: <Route element={<RutaProtegida roles={['ASISTENTE']} />}> ...rutas... </Route>
function RutaProtegida({ roles }) {
  const { autenticado, rol } = useAuth();

  if (!autenticado) return <Navigate to="/" replace />;
  if (roles && !roles.includes(rol)) return <Navigate to="/home" replace />;

  return <Outlet />;
}

export default RutaProtegida;
