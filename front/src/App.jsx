import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Login from './views/auth/Login';
import Register from './views/auth/Register';
import Home from './views/Home';
import RutaProtegida from './components/RutaProtegida';
import LayoutAsistente from './components/LayoutAsistente';
import Festivales from './views/asistente/Festivales';

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
          </Route>
        </Route>
      </Routes>
    </Router>
  );
}

export default App;