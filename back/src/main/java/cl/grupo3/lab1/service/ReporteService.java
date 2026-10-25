package cl.grupo3.lab1.service;

import cl.grupo3.lab1.repository.ReporteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ReporteService {

    @Autowired
    private ReporteRepository reporteRepository;

    // Obtiene los datos de la vista materializada para el reporte de ventas y ocupación
    public List<Map<String, Object>> obtenerVentasOcupacion() {
        return reporteRepository.obtenerVentasOcupacion();
    }

    // Refresca manualmente la vista materializada de ventas y ocupación
    // y retorna los datos actualizados
    public List<Map<String, Object>> refrescarReporteManual() {
        reporteRepository.refrescarVistaMaterializada();
        return reporteRepository.obtenerVentasOcupacion();
    }

    // Se ejecuta automáticamente todos los días a las 23:59:59 (cierre de ventas)
    // El formato Cron es: Segundo, Minuto, Hora, Día, Mes, Día de la semana
    // La zona horaria es "America/Santiago" para que coincida con la hora local de Chile
    @Scheduled(cron = "59 59 23 * * ?", zone = "America/Santiago")
    public void refrescoAutomaticoDiario() {
        System.out.println("[SISTEMA] Ejecutando refresco diario de la vista materializada de ventas...");
        reporteRepository.refrescarVistaMaterializada();
    }
}