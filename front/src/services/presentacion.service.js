import http from "../http-common";

// Obtener una presentacion por su id
const obtenerPresentacion = (idPresentacion) => http.get(`/presentaciones/${idPresentacion}`);

// Crear una presentacion en un festival
const crearPresentacion = (presentacionData) => http.post("/presentaciones", presentacionData);

// Editar escenario, artista u horario de una presentacion
const editarPresentacion = (idPresentacion, presentacionData) => http.put(`/presentaciones/${idPresentacion}`, presentacionData);

// Eliminar una presentacion
const eliminarPresentacion = (idPresentacion) => http.delete(`/presentaciones/${idPresentacion}`);

// Cancelar una presentacion
const cancelarPresentacion = (idPresentacion) => http.post(`/presentaciones/${idPresentacion}/cancelar`);

export default { obtenerPresentacion, crearPresentacion, editarPresentacion, eliminarPresentacion, cancelarPresentacion };