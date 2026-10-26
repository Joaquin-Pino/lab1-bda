import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Login from './views/auth/Login';
import Register from './views/auth/Register';
import Home from './views/Home';
import RutaProtegida from './components/RutaProtegida';
import LayoutAsistente from './components/LayoutAsistente';
import Festivales from './views/asistente/Festivales';
import FestivalDetalle from './views/asistente/FestivalDetalle';
import MisTickets from './views/asistente/MisTickets';
import MisOrdenes from './views/asistente/MisOrdenes';
import MiPerfil from './views/asistente/MiPerfil';
import MisNotificaciones from './views/asistente/MisNotificaciones';
import LayoutProductor from './components/LayoutProductor';
import FestivalesProduccion from './views/productor/FestivalesProduccion';
import MisFestivales from './views/productor/MisFestivales';
import CrearFestival from './views/productor/CrearFestival';
import FestivalDetalleProduccion from './views/productor/FestivalDetalleProduccion';
import CrearEntrada from './views/productor/CrearEntrada';
import EditarEntrada from './views/productor/EditarEntrada';
import LayoutAdministrador from './components/LayoutAdministrador';
import DashboardAdministrador from './views/administrador/DashboardAdministrador';
import CrearPresentacion from './views/productor/CrearPresentacion';
import EditarPresentacion from './views/productor/EditarPresentacion';
import AuditoriaPrecios from './views/productor/AuditoriaPrecios';

function App() {
  return (
    <Router>
      {/* Todo lo que esté dentro de <Routes> será reemplazado según la URL */}
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* Rutas que requieren sesión iniciada */}
        <Route element={<RutaProtegida />}>
          <Route path="/home" element={<Home />} />
        </Route>

        {/* Vista del asistente: todas comparten la barra de navegación */}
        <Route element={<RutaProtegida roles={['ASISTENTE']} />}>
          <Route element={<LayoutAsistente />}>
            <Route path="/festivales" element={<Festivales />} />
            <Route path="/festivales/:id" element={<FestivalDetalle />} />
            <Route path="/mis-tickets" element={<MisTickets />} />
            <Route path="/mis-ordenes" element={<MisOrdenes />} />
            <Route path="/mis-notificaciones" element={<MisNotificaciones />} />
            <Route path="/mi-perfil" element={<MiPerfil />} />
          </Route>
        </Route>

        {/* Vista del productor: todas comparten la barra de navegación */}
        <Route element={<RutaProtegida roles={['PRODUCTOR']} />}>
          <Route element={<LayoutProductor />}>
            <Route path="/festivales-produccion" element={<FestivalesProduccion />} />
            <Route path="/mis-festivales" element={<MisFestivales />} />
            <Route path="/festivales-produccion/nuevo" element={<CrearFestival />} />
            <Route path="/festivales-produccion/:idFestival" element={<FestivalDetalleProduccion />} />
            <Route path="/festivales-produccion/:idFestival/crear-entrada" element={<CrearEntrada />} />
            <Route path="/festivales-produccion/:idFestival/editar-entrada/:idTipoEntrada" element={<EditarEntrada />} />
            <Route path="/festivales-produccion/:idFestival/crear-presentacion" element={<CrearPresentacion />} />
            <Route path="/festivales-produccion/:idFestival/editar-presentacion/:idPresentacion" element={<EditarPresentacion />} />
            <Route path="/festivales-produccion/:idFestival/auditoria-precios/:idTipoEntrada" element={<AuditoriaPrecios />} />
          </Route>
        </Route>
        
        {/* Vista del administrador: todas comparten la barra de navegación */}
        <Route element={<RutaProtegida roles={['ADMINISTRADOR']} />}>
          <Route element={<LayoutAdministrador />}>
            <Route path="/dashboard" element={<DashboardAdministrador />} />
          </Route>
        </Route>

      </Routes>
    </Router>
  );
}

export default App;