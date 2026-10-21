\set ON_ERROR_STOP on
\i /db/createDb.sql
\i /db/fn_dia_festival.sql
\i /db/trg_validar_presentacion.sql
\i /db/sp_comprar_entradas.sql
\i /db/seed_dev.sql
-- Las demás personas agregan aquí sus \i (nuevos SP, triggers, vista, índices) ANTES de seed_dev.sql
