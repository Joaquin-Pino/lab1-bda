import http from "../http-common";

// Obtener datos de un tipo de entrada por su id
const obtenerTipoEntrada = (idTipoEntrada) => http.get(`/tipos-entrada/${idTipoEntrada}`);

// Crear un tipo de entrada para un festival
const crearTipoEntrada = (tipoEntradaData) => http.post("/tipos-entrada", tipoEntradaData);

// Actualizar un tipo de entrada existente de un festival
const editarTipoEntrada = (idTipoEntrada, tipoEntradaData) => http.put(`/tipos-entrada/${idTipoEntrada}`, tipoEntradaData);

// Eliminar un tipo de entrada existente de un festival
const eliminarTipoEntrada = (idTipoEntrada) => http.delete(`/tipos-entrada/${idTipoEntrada}`);

// Historial de cambios de precio de un tipo de entrada
const listarAuditoriaPrecios = (idTipoEntrada) => http.get(`/tipos-entrada/${idTipoEntrada}/auditoria-precios`);

export default { obtenerTipoEntrada, crearTipoEntrada, editarTipoEntrada, eliminarTipoEntrada, listarAuditoriaPrecios };