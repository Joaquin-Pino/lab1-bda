import axios from "axios";

// Claves de localStorage donde se guarda la sesión
export const TOKEN_KEY = "token";
export const USUARIO_KEY = "usuario";

const http = axios.create({
  baseURL: `${import.meta.env.VITE_API_URL ?? "http://localhost:8080"}/api`,
  headers: {
    "Content-type": "application/json"
  }
});

// Agrega el token JWT a cada petición
http.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Si el token expiró o es inválido, se limpia la sesión y se vuelve al login.
// Se excluye /auth/login, donde un 401 solo significa credenciales incorrectas.
http.interceptors.response.use(
  (response) => response,
  (error) => {
    const url = error.config?.url ?? "";
    if (error.response?.status === 401 && !url.includes("/auth/login")) {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USUARIO_KEY);
      window.location.assign("/");
    }
    return Promise.reject(error);
  }
);

// Extrae el mensaje del formato de error del backend: {"error": "..."}
export const mensajeError = (error, porDefecto = "Ocurrió un error inesperado.") => {
  if (!error.response) return "No se pudo conectar con el servidor.";
  return error.response.data?.error ?? porDefecto;
};

export default http;
