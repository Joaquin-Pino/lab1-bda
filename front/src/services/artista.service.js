import http from "../http-common";

const listarArtistas = () => http.get("/artistas");

export default { listarArtistas };