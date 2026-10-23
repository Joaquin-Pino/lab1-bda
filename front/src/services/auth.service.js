import http from "../http-common";

// Función para hacer login
const login = (email, password) => {
  // Hace un POST a /api/auth/login con el formato de tu LoginRequest
  return http.post("/login", { email, password });
};

// Función para hacer registro
const register = (nombre_completo, email, password) => {
  // Hace un POST a /api/auth/register con el formato de tu RegisterRequest
  return http.post("/register", { nombre_completo: nombre_completo, email, password });
};

// Exportamos las funciones para que puedan ser usadas en otros archivos
export default { login, register };