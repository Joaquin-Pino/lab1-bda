import http from "../http-common";

// El comprador sale del token; el cuerpo solo lleva qué y cuánto
const comprar = (id_tipo_entrada, cantidad, usar_credito) =>
  http.post("/compras", { id_tipo_entrada, cantidad, usar_credito });

// Perfil del usuario logueado (incluye creditos_compensacion)
const perfil = () => http.get("/auth/me");

// Solo devuelven lo del asistente logueado (el backend filtra por el token)
const misOrdenes = () => http.get("/ordenes/mias");

const misTickets = () => http.get("/tickets/mios");

export default { comprar, perfil, misOrdenes, misTickets };
