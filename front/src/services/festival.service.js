import http from "../http-common";

// Al asistente el backend solo le devuelve los festivales PUBLICADO
const listar = () => http.get("/festivales");

const obtener = (id) => http.get(`/festivales/${id}`);

// Ojo: este endpoint recibe el parámetro en camelCase (idFestival)
const listarTiposEntrada = (idFestival) => http.get("/tipos-entrada", { params: { idFestival } });

export default { listar, obtener, listarTiposEntrada };
