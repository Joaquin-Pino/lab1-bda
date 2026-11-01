package cl.grupo3.lab1.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneId;


final class Fechas {

    static final ZoneId ZONA_CHILE = ZoneId.of("America/Santiago");

    private Fechas() {
    }

    static OffsetDateTime leer(ResultSet rs, String columna) throws SQLException {
        OffsetDateTime valor = rs.getObject(columna, OffsetDateTime.class);
        return valor == null ? null : valor.atZoneSameInstant(ZONA_CHILE).toOffsetDateTime();
    }
}
