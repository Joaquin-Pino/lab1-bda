import http from "../http-common";

// Recibe el reporte completo de ventas
const obtenerReporte = () => http.get("/api/reportes/ventas");

// Actualiza el reporte y recibe el nuevo reporte completo de ventas
const actualizarReporte = () => http.post("/api/reportes/ventas/refrescar");

export default { obtenerReporte, actualizarReporte };

