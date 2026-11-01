import http from "../http-common";

// Valida un ticket y, si es válido, lo marca como UTILIZADO (solo PRODUCTOR dueño o ADMIN).
// Un ticket inválido no es error HTTP: responde 200 con { resultado, valido, mensaje, ... }
const validar = (codigoUnico, idFestival) =>
  http.post("/acceso/validar", { codigo_unico: codigoUnico, id_festival: idFestival });

export default { validar };
