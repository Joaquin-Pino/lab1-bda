// Funciones de fechas y horas para los formularios de presentaciones.

// Días del festival entre dos fechas "YYYY-MM-DD", ambas incluidas.
export const diasEntre = (inicio, fin) => {
  const dias = [];
  for (let d = new Date(`${inicio}T00:00:00Z`); d <= new Date(`${fin}T00:00:00Z`); d.setUTCDate(d.getUTCDate() + 1)) {
    dias.push(d.toISOString().slice(0, 10));
  }
  return dias;
};

export const diaSiguiente = (dia) => {
  const d = new Date(`${dia}T00:00:00Z`);
  d.setUTCDate(d.getUTCDate() + 1);
  return d.toISOString().slice(0, 10);
};


const zonaChile = (dia) => {
  const partes = new Intl.DateTimeFormat('en-US', { timeZone: 'America/Santiago', timeZoneName: 'longOffset' })
    .formatToParts(new Date(`${dia}T12:00:00Z`));
  return partes.find(parte => parte.type === 'timeZoneName').value.replace('GMT', '');
};

export const armarFechaHora = (dia, hora) => `${dia}T${hora}:00${zonaChile(dia)}`;


export const armarHorario = (dia, horaInicio, horaFin) => ({
  fecha_hora_inicio: armarFechaHora(dia, horaInicio),
  fecha_hora_fin: armarFechaHora(horaFin <= horaInicio ? diaSiguiente(dia) : dia, horaFin)
});