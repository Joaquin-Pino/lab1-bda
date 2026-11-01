import http from "../http-common";

// Historial de cambios de precio de todos los festivales (solo ADMINISTRADOR).
// idFestival es opcional; este endpoint lo recibe en camelCase.
const listarHistorialPrecios = (idFestival) =>
  http.get("/auditoria-precios", { params: idFestival ? { idFestival } : {} });

export default { listarHistorialPrecios };
