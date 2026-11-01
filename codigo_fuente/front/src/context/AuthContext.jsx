import { createContext, useContext, useState } from 'react';
import { TOKEN_KEY, USUARIO_KEY } from '../http-common';

const AuthContext = createContext(null);

// Lee la sesión guardada; si el JSON está corrupto se ignora
const leerUsuario = () => {
  try {
    return JSON.parse(localStorage.getItem(USUARIO_KEY));
  } catch {
    return null;
  }
};

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_KEY));
  const [usuario, setUsuario] = useState(leerUsuario);

  // Recibe el LoginResponse del backend: { token, tipo, expira_en_segundos, usuario }
  const iniciarSesion = (loginResponse) => {
    localStorage.setItem(TOKEN_KEY, loginResponse.token);
    localStorage.setItem(USUARIO_KEY, JSON.stringify(loginResponse.usuario));
    setToken(loginResponse.token);
    setUsuario(loginResponse.usuario);
  };

  const cerrarSesion = () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USUARIO_KEY);
    setToken(null);
    setUsuario(null);
  };

  const value = {
    token,
    usuario,
    rol: usuario?.rol ?? null,
    autenticado: Boolean(token && usuario),
    iniciarSesion,
    cerrarSesion,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth debe usarse dentro de <AuthProvider>');
  return ctx;
}
