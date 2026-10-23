// "2026-11-14" -> "14-11-2026" (sin pasar por Date para no correr el día por la zona horaria)
export const formatearFecha = (fecha) => fecha.split('-').reverse().join('-');

// 35000 -> "$35.000"
export const formatearPrecio = (monto) =>
  Number(monto).toLocaleString('es-CL', { style: 'currency', currency: 'CLP' });
