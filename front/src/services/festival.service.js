import http from "../http-common";

// Al asistente el backend solo le devuelve los festivales PUBLICADO
const listar = () => http.get("/festivales");

const obtener = (id) => http.get(`/festivales/${id}`);

// Ojo: este endpoint recibe el parámetro en camelCase (idFestival)
const listarTiposEntrada = (idFestival) => http.get("/tipos-entrada", { params: { idFestival } });

// fecha en formato "YYYY-MM-DD"; debe estar dentro de las fechas del festival
const obtenerGrilla = (idFestival, fecha) => http.get(`/festivales/${idFestival}/grilla`, { params: { fecha } });

// crear un festival (solo para PRODUCTOR y ADMIN)
const crearFestival = (festivalData) => http.post("/festivales", festivalData);

export default { listar, obtener, listarTiposEntrada, obtenerGrilla, crearFestival };
