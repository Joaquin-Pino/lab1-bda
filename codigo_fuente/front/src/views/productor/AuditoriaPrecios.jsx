import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import tiposEntradaService from '../../services/tiposEntrada.service.js';
import { mensajeError } from '../../http-common.js';
import { formatearFechaHora, formatearPrecio } from '../../utils/formato.js';

function AuditoriaPrecios() {
    const { idFestival, idTipoEntrada } = useParams();

    const [ tipoEntrada, setTipoEntrada ] = useState(null);
    const [ cambios, setCambios ] = useState([]);
    const [ cargando, setCargando ] = useState(true);
    const [ errorMensaje, setErrorMensaje ] = useState('');

    useEffect(() => {
        Promise.all([
            tiposEntradaService.obtenerTipoEntrada(idTipoEntrada),
            tiposEntradaService.listarAuditoriaPrecios(idTipoEntrada),
        ])
        .then(([respTipo, respCambios]) => {
            setTipoEntrada(respTipo.data);
            setCambios(respCambios.data);
        })
        .catch(error => setErrorMensaje(mensajeError(error, "No se pudo cargar el historial de precios.")))
        .finally(() => setCargando(false));
    }, [idTipoEntrada]);


    const volver = (
        <Link to = {`/festivales-produccion/${idFestival}`} className="d-inline-block mb-3">
            &larr; Volver al festival
        </Link>
    );

    if (cargando) return <p>Cargando historial de precios...</p>;
    if (errorMensaje) {
        return (
            <>
                <div className="alert alert-danger">{errorMensaje}</div>
                {volver}
            </>
        );
    }
    
    return ( 
    <>
        {volver}
        <h2>Historial de precios</h2>
        <p className="text-muted">
            {tipoEntrada.nombre_tipo} · precio actual: {formatearPrecio(tipoEntrada.precio_actual)}
        </p>

        {cambios.length === 0 ? (
            <p className="text-muted">Esta entrada nunca ha cambiado de precio.</p>
        ) : (
            <table className="table align-middle">
                <thead>
                    <tr>
                        <th>Fecha</th>
                        <th>Precio anterior</th>
                        <th>Precio nuevo</th>
                        <th>Modificado por</th>
                    </tr>
                </thead>
                <tbody>
                    {cambios.map(cambio => (
                    <tr key={cambio.id_auditoria}>
                        <td>{formatearFechaHora(cambio.fecha_modificacion)}</td>
                        <td>{formatearPrecio(cambio.precio_anterior)}</td>
                        <td>{formatearPrecio(cambio.precio_nuevo)}</td>
                        {/* nombre_usuario es null si el cambio no se hizo desde la API */}
                        <td>{cambio.nombre_usuario ?? <span className="text-muted">Sin registro</span>}</td>
                    </tr>
                    ))}
                </tbody>
            </table>
      )}
    </>
  );
}

export default AuditoriaPrecios;