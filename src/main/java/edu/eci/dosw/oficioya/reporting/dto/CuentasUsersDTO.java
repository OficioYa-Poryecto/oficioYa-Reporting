package edu.eci.dosw.oficioya.reporting.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Conteo de cuentas tal como lo entrega Users en {@code GET /api/v1/users/stats/accounts?from=&to=} (RF-12).
 * Se convierte al formato de Reporting con {@link #aEstadisticas()}.
 *
 * @param registered  cuentas creadas hasta {@code to}, estén activas o no
 * @param deactivated cuentas desactivadas entre {@code from} y {@code to}
 */
public record CuentasUsersDTO(Conteo registered, Conteo deactivated) {

    /**
     * Fecha de inicio con la que se piden todas las desactivaciones hasta {@code to}: así "desactivadas" es el
     * estado a esa fecha y no solo las del periodo.
     */
    public static final LocalDate DESDE_EL_INICIO = LocalDate.of(2000, 1, 1);

    /**
     * Cantidad de cuentas en total y por rol. Una cuenta con los dos roles cuenta en ambos.
     */
    public record Conteo(long total, long trabajador, long contratante) {
    }

    /**
     * Activas = registradas − desactivadas; inactivas = desactivadas. Users no entrega los usuarios activos por día
     * (inicios de sesión), así que esa lista queda vacía.
     */
    public EstadisticasUsuariosDTO aEstadisticas() {
        long registradas = registered == null ? 0 : registered.total();
        long desactivadas = deactivated == null ? 0 : deactivated.total();
        return new EstadisticasUsuariosDTO(Math.max(registradas - desactivadas, 0), desactivadas, List.of());
    }
}
