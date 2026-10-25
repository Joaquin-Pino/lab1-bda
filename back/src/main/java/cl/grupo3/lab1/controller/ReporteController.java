package cl.grupo3.lab1.controller;

import cl.grupo3.lab1.service.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    // Endpoint para que React consuma los datos del gráfico
    @GetMapping("/ventas")
    public ResponseEntity<List<Map<String, Object>>> obtenerReporte() {
        List<Map<String, Object>> reporte = reporteService.obtenerVentasOcupacion();
        return ResponseEntity.ok(reporte);
    }

    // Endpoint para refrescar manualmente la vista materializada
    @PostMapping("/ventas/refrescar")
    public ResponseEntity<List<Map<String, Object>>> refrescarReporteManual() {
        // Ejecuta el refresco y guarda los datos nuevos al mismo tiempo
        List<Map<String, Object>> datosFrescos = reporteService.refrescarReporteManual();
        
        return ResponseEntity.ok(datosFrescos);
    }
}