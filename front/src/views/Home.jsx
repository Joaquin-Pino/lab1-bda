import { useNavigate } from 'react-router-dom';

function Home() {
  const navigate = useNavigate();

  const handleLogout = () => {
    // Al cerrar sesión, devolvemos al usuario a la ruta raíz (Login)
    navigate('/');
  };

  return (
    <div className="container mt-5">
      <div className="p-5 text-center bg-light rounded shadow-sm">
        <h1 className="display-4">¡Bienvenido al Inicio!</h1>
        <p className="lead">Has iniciado sesión correctamente. Esta ruta es /inicio.</p>
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