import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import authService from '../../services/auth.service.js';
import { mensajeError } from '../../http-common.js';
import { useAuth } from '../../context/AuthContext.jsx';

function Login() {
  const navigate = useNavigate(); // hook de React Router para redirigir a otra ruta
  const { iniciarSesion } = useAuth();
  const registrado = useLocation().state?.registrado; // viene desde Register

  // Declaración de estados para los campos del formulario
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errorMensaje, setErrorMensaje] = useState('');

  const handleLogin = (e) => {
    e.preventDefault();
    setErrorMensaje(''); // Limpiamos errores previos

    // Llamada a Spring Boot
    authService.login(email, password)
      .then(response => {
        iniciarSesion(response.data); // guarda token y usuario en el AuthContext
        navigate('/home'); // Redirigimos al usuario a la ruta /home
      })
      .catch(error => {
        setErrorMensaje(mensajeError(error, "Credenciales inválidas."));
      });
  };

  return (
    <div className="container d-flex justify-content-center align-items-center vh-100">
      <div className="card shadow p-4" style={{ maxWidth: '400px', width: '100%' }}>
        <h2 className="text-center mb-4">Iniciar Sesión</h2>
        
        {/* Mostrar alerta si hay error */}
        {registrado && !errorMensaje && (
          <div className="alert alert-success">Cuenta creada con éxito. Ya puedes iniciar sesión.</div>
        )}
        {errorMensaje && <div className="alert alert-danger">{errorMensaje}</div>}

        <form onSubmit={handleLogin}>
          <div className="mb-3">
            <label className="form-label">Correo Electrónico</label>
            <input 
              type="email" 
              className="form-control" 
              value={email} // Conectamos el input a la variable
              onChange={(e) => setEmail(e.target.value)} // Actualizamos la variable al tipear
              required  // Hacemos que el campo sea obligatorio
            />
          </div>
          <div className="mb-3">
            <label className="form-label">Contraseña</label>
            <input 
              type="password" 
              className="form-control" 
              value={password} // Conectamos el input a la variable
              onChange={(e) => setPassword(e.target.value)} // Actualizamos la variable al tipear
              required // Hacemos que el campo sea obligatorio
            />
          </div>
          <button type="submit" className="btn btn-primary w-100 mb-3">Ingresar</button>
          <div className="text-center">
            <span>¿No tienes cuenta? </span>
            <Link to="/register" className="text-decoration-none">Regístrate aquí</Link>
          </div>
        </form>
      </div>
    </div>
  );
}

export default Login;