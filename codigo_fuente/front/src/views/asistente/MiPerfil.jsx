import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import compraService from '../../services/compra.service.js';
import { mensajeError } from '../../http-common.js';
import { formatearPrecio } from '../../utils/formato.js';

function MiPerfil() {
  const [perfil, setPerfil] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [errorMensaje, setErrorMensaje] = useState('');

  useEffect(() => {
    compraService.perfil()
      .then(response => setPerfil(response.data))
      .catch(error => setErrorMensaje(mensajeError(error, "No se pudo cargar tu perfil.")))
      .finally(() => setCargando(false));
  }, []);

  if (cargando) return <p>Cargando perfil...</p>;
  if (errorMensaje) return <div className="alert alert-danger">{errorMensaje}</div>;

  const saldo = Number(perfil.creditos_compensacion);

  return (
    <>
      <h2 className="mb-4">Mi perfil</h2>

      <div className="row g-4">
        <div className="col-md-6">
          <div className="card h-100">
            <div className="card-body">
              <h5 className="card-title">Mis datos</h5>
              <dl className="row mb-0">
                <dt className="col-sm-4">Nombre</dt>
                <dd className="col-sm-8">{perfil.nombre_completo}</dd>
                <dt className="col-sm-4">Email</dt>
                <dd className="col-sm-8">{perfil.email}</dd>
              </dl>
            </div>
          </div>
        </div>

        <div className="col-md-6">
          <div className="card h-100">
            <div className="card-body">
              <h5 className="card-title">Crédito de compensación</h5>
              <p className="display-6 mb-2">{formatearPrecio(saldo)}</p>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}

export default MiPerfil;