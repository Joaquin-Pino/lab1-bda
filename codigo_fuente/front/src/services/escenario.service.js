import http from "../http-common";

// Escenarios de un recinto
const listarPorRecinto = (idRecinto) => http.get("/escenarios", { params: { id_recinto: idRecinto } });

export default { listarPorRecinto };