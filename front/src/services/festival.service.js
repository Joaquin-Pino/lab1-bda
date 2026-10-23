import http from "../http-common";

// Al asistente el backend solo le devuelve los festivales PUBLICADO
const listar = () => http.get("/festivales");

const obtener = (id) => http.get(`/festivales/${id}`);

export default { listar, obtener };
