/* Función única de "día" del festival
El día de una presentación es la fecha calendario de Chile (America/Santiago) de su
fecha_hora_inicio, sin importar la zona horaria de la sesión. Una presentación a la
1:00 AM cuenta como del día calendario siguiente.
Es la única definición de "día" para la grilla, el SP 2 (cancelación) y el control de acceso.
*/

CREATE OR REPLACE FUNCTION fn_dia_festival(p_ts TIMESTAMPTZ)
RETURNS DATE
LANGUAGE sql
IMMUTABLE
AS $$
    SELECT (p_ts AT TIME ZONE 'America/Santiago')::date;
$$;
