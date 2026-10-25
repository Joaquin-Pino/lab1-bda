package cl.grupo3.lab1.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class ReporteRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Lee los datos de la vista materializada.
    // Retorna una lista de mapas, diccionario donde 'nombre del festival' -> datos del festival
    public List<Map<String, Object>> obtenerVentasOcupacion() {
        String sql = "SELECT * FROM mv_ventas_ocupacion"; // Toma toda la tabla
        return jdbcTemplate.queryForList(sql); // queryForList transforma el resultado de la consulta SQL en una lista de mapas (diccionarios)
    }

    // Llama a la función SQL que refresca la vista
    public void refrescarVistaMaterializada() {
        String sql = "SELECT fn_refrescar_reporte_ventas()"; // llama la función SQL que refresca la vista materializada
        jdbcTemplate.execute(sql); // execute ejecuta la consulta SQL sin esperar un resultado
    }
}