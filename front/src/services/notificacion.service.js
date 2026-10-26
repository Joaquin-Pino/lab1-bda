import http from "../http-common";

// Solo devuelven lo del asistente logueado (el backend filtra por el token)
const listar = (soloNoLeidas = false) =>
  http.get("/notificaciones", { params: { solo_no_leidas: soloNoLeidas } });

// Contador de la campana: { no_leidas }
const contarNoLeidas = () => http.get("/notificaciones/no-leidas");

const marcarLeida = (id) => http.put(`/notificaciones/${id}/leida`);

const marcarTodasLeidas = () => http.put("/notificaciones/leidas");

const eliminar = (id) => http.delete(`/notificaciones/${id}`);

// Aviso al navbar para que refresque el contador de la campana
export const NOTIFICACIONES_CAMBIARON = "notificaciones-cambiaron";
export const avisarCambio = () => window.dispatchEvent(new Event(NOTIFICACIONES_CAMBIARON));

export default { listar, contarNoLeidas, marcarLeida, marcarTodasLeidas, eliminar };
