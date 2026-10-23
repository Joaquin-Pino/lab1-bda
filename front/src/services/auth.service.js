import http from "../http-common";

const login = (email, password) => http.post("/auth/login", { email, password });

const register = (nombre_completo, email, password) =>
  http.post("/auth/register", { nombre_completo, email, password });

export default { login, register };
