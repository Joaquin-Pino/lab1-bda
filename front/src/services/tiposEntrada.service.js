import http from "../http-common";

const crearTipoEntrada = (tipoEntradaData) => http.post("/tipos-entrada", tipoEntradaData);

export default { crearTipoEntrada };