package edu.eci.dosw.oficioya.reporting.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.json.JsonMapper;

class ReporteServiceDTOTest {

    @Test
    void traduceLosEstadosYAgrupaLasHorasEnFranjasDeSeisHoras() {
        ReporteServiceDTO reporte = new ReporteServiceDTO(14,
                Map.of("PENDING", 3L, "COMPLETED", 5L, "DELETED", 1L, "CONFLICT", 2L, "OTRO", 3L),
                Map.of("Chapinero", 9L, "Suba", 5L),
                Map.of(2, 1L, 7, 4L, 11, 2L, 12, 3L, 23, 4L, 24, 9L));

        EstadisticasServiciosDTO estadisticas = reporte.aEstadisticas();

        assertThat(estadisticas.solicitudesPorEstado()).containsEntry("PENDIENTE", 3L).containsEntry("CUMPLIDA", 5L)
                .containsEntry("ELIMINADA", 1L).containsEntry("CONFLICTO", 2L).containsEntry("OTRO", 3L);
        assertThat(estadisticas.solicitudesPorZona()).containsExactlyInAnyOrder(new ConteoDTO("Chapinero", 9),
                new ConteoDTO("Suba", 5));
        // La hora 24 no existe y se ignora.
        assertThat(estadisticas.solicitudesPorFranja()).extracting(ConteoDTO::cantidad).containsExactly(1L, 6L, 3L, 4L);
    }

    @Test
    void usaLosServiciosCompletadosPorDiaOrdenadosPorFechaYLosConteosPorOficio() {
        ReporteServiceDTO reporte = new ReporteServiceDTO(7, Map.of("COMPLETED", 7L), Map.of(), Map.of(),
                List.of(new ConteoDTO("2026-10-31", 4), new ConteoDTO("2026-10-30", 3)),
                List.of(new ConteoDTO("Plomería", 5), new ConteoDTO(null, 2)),
                List.of(new ConteoDTO("Plomería", 6), new ConteoDTO(" ", 1), new ConteoDTO("Electricidad", 2)));

        EstadisticasServiciosDTO estadisticas = reporte.aEstadisticas();

        assertThat(estadisticas.cumplidosPorDia()).containsExactly(new ConteoDTO("2026-10-30", 3),
                new ConteoDTO("2026-10-31", 4));
        // Las solicitudes sin oficio (creadas antes de que Service lo guardara) no se cuentan por oficio.
        assertThat(estadisticas.cumplidosPorOficio()).containsExactly(new ConteoDTO("Plomería", 5));
        assertThat(estadisticas.solicitudesPorOficio()).containsExactly(new ConteoDTO("Electricidad", 2),
                new ConteoDTO("Plomería", 6));
        assertThat(estadisticas.tarifaPromedio()).isNull();
    }

    @Test
    void leeLosCamposNuevosDelJsonDeService() throws Exception {
        String json = """
                {"total": 3, "byStatus": {"COMPLETED": 3}, "byNeighborhood": {}, "byHour": {"9": 3},
                 "completedByDay": [{"etiqueta": "2026-10-30", "cantidad": 3}],
                 "completedByTrade": [{"etiqueta": "Plomería", "cantidad": 3}],
                 "requestsByTrade": [{"etiqueta": "Plomería", "cantidad": 4}]}
                """;

        ReporteServiceDTO reporte = JsonMapper.builder().build().readValue(json, ReporteServiceDTO.class);

        assertThat(reporte.aEstadisticas().cumplidosPorDia()).containsExactly(new ConteoDTO("2026-10-30", 3));
        assertThat(reporte.aEstadisticas().cumplidosPorOficio()).containsExactly(new ConteoDTO("Plomería", 3));
        assertThat(reporte.aEstadisticas().solicitudesPorOficio()).containsExactly(new ConteoDTO("Plomería", 4));
    }

    @Test
    void sinDatosDevuelveListasVaciasYFranjasEnCero() {
        EstadisticasServiciosDTO estadisticas = new ReporteServiceDTO(0, null, null, null).aEstadisticas();

        assertThat(estadisticas.cumplidosPorDia()).isEmpty();
        assertThat(estadisticas.solicitudesPorEstado()).isEmpty();
        assertThat(estadisticas.solicitudesPorZona()).isEmpty();
        assertThat(estadisticas.solicitudesPorFranja()).extracting(ConteoDTO::cantidad).containsOnly(0L);
    }
}
