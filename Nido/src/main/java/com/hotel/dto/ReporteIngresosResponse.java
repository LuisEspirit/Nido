package com.hotel.dto;

import java.time.LocalDate;
import java.util.List;

/** Ingresos y gastos del periodo (US14). Solo cuentan los pagos PAGADO; los PENDIENTE se muestran aparte. */
public record ReporteIngresosResponse(
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String moneda,
        double totalIngresos,
        double totalGastos,
        double neto,
        double pendienteDeCobro,
        List<PorAlojamiento> porAlojamiento) {

    public record PorAlojamiento(Integer idAlojamiento, String alojamiento, double ingresos, double gastos,
                                 double neto) {
    }
}
