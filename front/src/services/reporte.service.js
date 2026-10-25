import http from "../http-common";

// Recibe el reporte completo de ventas
const obtenerReporte = () => http.get("/reportes/ventas");

// Actualiza el reporte y recibe el nuevo reporte completo de ventas
const actualizarReporte = () => http.post("/reportes/ventas");

export default { obtenerReporte, actualizarReporte };

