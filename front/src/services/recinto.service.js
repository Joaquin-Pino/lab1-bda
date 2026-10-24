import http from "../http-common";

const listarRecintos = () => http.get("/recintos");

export default { listarRecintos };
