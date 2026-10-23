import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import authService from '../../services/auth.service.js';
import { mensajeError } from '../../http-common.js';

function Register() {
  const navigate = useNavigate(); // hook de React Router para redireccionar después del registro exitoso
  
  // Variables mapeadas exactamente a tu RegisterRequest en Spring Boot
  const [nombreCompleto, setNombreCompleto] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errorMensaje, setErrorMensaje] = useState('');

  const handleRegister = (e) => {
    e.preventDefault();
    setErrorMensaje('');

    // El backend exige entre 8 y 72 caracteres
    if (password.length < 8 || password.length > 72) {
      setErrorMensaje('La contraseña debe tener entre 8 y 72 caracteres.');
      return;
    }

    authService.register(nombreCompleto, email, password)
      .then(() => {
        // El endpoint devuelve un UsuarioResumen con status 201 CREATED.
        // Volvemos al login, que muestra el aviso de cuenta creada.
        navigate('/', { state: { registrado: true } });
      })
      .catch(error => {
        // Axios guarda la respuesta de error del servidor en error.response.data
        setErrorMensaje(mensajeError(error, "Error al crear la cuenta."));
      });
  };

  return (
    <div className="container d-flex justify-content-center align-items-center vh-100">
      <div className="card shadow p-4" style={{ maxWidth: '400px', width: '100%' }}>
        <h2 className="text-center mb-4">Crear Cuenta</h2>
        
        {errorMensaje && <div className="alert alert-danger">{errorMensaje}</div>}

        <form onSubmit={handleRegister}>
          <div className="mb-3">
            <label className="form-label">Nombre Completo</label>
            <input 
              type="text" 
              className="form-control"
              value={nombreCompleto} // Conectamos el input a la variable
              onChange={(e) => setNombreCompleto(e.target.value)} // Actualizamos la variable al tipear
              required // Hacemos que el campo sea obligatorio
            />
          </div>
          <div className="mb-3">
            <label className="form-label">Correo Electrónico</label>
            <input 
              type="email" 
              className="form-control"
              value={email} // Conectamos el input a la variable
              onChange={(e) => setEmail(e.target.value)} // Actualizamos la variable al tipear
              required // Hacemos que el campo sea obligatorio
            />
          </div>
          <div className="mb-3">
            <label className="form-label">Contraseña</label>
            <input 
              type="password" 
              className="form-control"
              value={password} // Conectamos el input a la variable
              onChange={(e) => setPassword(e.target.value)} // Actualizamos la variable al tipear
              minLength={8}
              maxLength={72}
              required // Hacemos que el campo sea obligatorio
            />
            <div className="form-text">Entre 8 y 72 caracteres.</div>
          </div>
          <button type="submit" className="btn btn-success w-100 mb-3">Registrarse</button>
          <div className="text-center">
            <span>¿Ya tienes cuenta? </span>
            <Link to="/" className="text-decoration-none">Vuelve al Login</Link>
          </div>
        </form>
      </div>
    </div>
  );
}

export default Register;