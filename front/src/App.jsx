import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Login from './views/auth/Login';
import Register from './views/auth/Register';
import Home from './views/Home';
import RutaProtegida from './components/RutaProtegida';

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
      </Routes>
    </Router>
  );
}

export default App;