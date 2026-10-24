// "2026-11-14" -> "14-11-2026" (sin pasar por Date para no correr el día por la zona horaria)
export const formatearFecha = (fecha) => fecha.split('-').reverse().join('-');

// "2026-12-12T18:30:00-03:00" -> "18:30". El backend ya manda la hora en zona de Chile,
// así que se toma tal cual en vez de convertirla a la zona del navegador.
export const formatearHora = (fechaHora) => fechaHora.slice(11, 16);

// "2026-12-12T18:30:00-03:00" -> "12-12-2026 18:30"
export const formatearFechaHora = (fechaHora) =>
  `${formatearFecha(fechaHora.slice(0, 10))} ${formatearHora(fechaHora)}`;

// 35000 -> "$35.000"
export const formatearPrecio = (monto) =>
  Number(monto).toLocaleString('es-CL', { style: 'currency', currency: 'CLP' });
