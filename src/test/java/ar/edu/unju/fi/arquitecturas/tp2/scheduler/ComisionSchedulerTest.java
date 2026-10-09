package ar.edu.unju.fi.arquitecturas.tp2.scheduler;

import ar.edu.unju.fi.arquitecturas.tp2.event.ConfiguracionActualizadaEvent;
import ar.edu.unju.fi.arquitecturas.tp2.service.ComisionService;
import ar.edu.unju.fi.arquitecturas.tp2.service.ConfiguracionService;
import ar.edu.unju.fi.arquitecturas.tp2.util.ClavesConfiguracion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;

import java.util.concurrent.ScheduledFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del scheduler dinámico de comisiones.
 */
@ExtendWith(MockitoExtension.class)
class ComisionSchedulerTest {

    @Mock
    private TaskScheduler taskScheduler;

    @Mock
    private ComisionService comisionService;

    @Mock
    private ConfiguracionService configuracionService;

    @Mock
    private ScheduledFuture<?> primeraTarea;

    @Mock
    private ScheduledFuture<?> segundaTarea;

    /**
     * Comprueba la programación inicial utilizando
     * el Cron almacenado en la configuración general.
     */
    @Test
    void deberiaProgramarTareaAlIniciarLaAplicacion() {
        when(configuracionService.obtenerValorTexto(
                ClavesConfiguracion.CRON_LIQUIDACION_COMISIONES
        )).thenReturn("0 0 0 1 * ?");

        doReturn(primeraTarea)
                .when(taskScheduler)
                .schedule(
                        any(Runnable.class),
                        any(Trigger.class)
                );

        ComisionScheduler scheduler = crearScheduler();

        scheduler.iniciarProgramacion(null);

        verify(configuracionService)
                .obtenerValorTexto(
                        ClavesConfiguracion.CRON_LIQUIDACION_COMISIONES
                );

        verify(taskScheduler)
                .schedule(
                        any(Runnable.class),
                        any(Trigger.class)
                );
    }

    /**
     * Comprueba que cambiar el Cron cancele
     * la tarea anterior y genere una nueva programación.
     */
    @Test
    void deberiaReprogramarCuandoCambiaElCron() {
        when(configuracionService.obtenerValorTexto(
                ClavesConfiguracion.CRON_LIQUIDACION_COMISIONES
        )).thenReturn(
                "0 0 0 1 * ?",
                "0 0 3 1 * ?"
        );

        doReturn(
                primeraTarea,
                segundaTarea
        ).when(taskScheduler)
                .schedule(
                        any(Runnable.class),
                        any(Trigger.class)
                );

        ComisionScheduler scheduler = crearScheduler();

        scheduler.iniciarProgramacion(null);

        scheduler.alActualizarConfiguracion(
                new ConfiguracionActualizadaEvent(
                        ClavesConfiguracion.CRON_LIQUIDACION_COMISIONES,
                        "0 0 3 1 * ?"
                )
        );

        verify(primeraTarea).cancel(false);

        verify(taskScheduler, times(2))
                .schedule(
                        any(Runnable.class),
                        any(Trigger.class)
                );
    }

    /**
     * Verifica que un Cron inválido no cancele
     * una programación válida que ya se encontraba activa.
     */
    @Test
    void noDeberiaCancelarTareaActualSiNuevoCronEsInvalido() {
        when(configuracionService.obtenerValorTexto(
                ClavesConfiguracion.CRON_LIQUIDACION_COMISIONES
        )).thenReturn(
                "0 0 0 1 * ?",
                "cron-invalido"
        );

        doReturn(primeraTarea)
                .when(taskScheduler)
                .schedule(
                        any(Runnable.class),
                        any(Trigger.class)
                );

        ComisionScheduler scheduler = crearScheduler();

        scheduler.iniciarProgramacion(null);

        scheduler.alActualizarConfiguracion(
                new ConfiguracionActualizadaEvent(
                        ClavesConfiguracion.CRON_LIQUIDACION_COMISIONES,
                        "cron-invalido"
                )
        );

        verify(primeraTarea, never()).cancel(false);

        verify(taskScheduler, times(1))
                .schedule(
                        any(Runnable.class),
                        any(Trigger.class)
                );
    }

    /**
     * Comprueba que una modificación de otra configuración
     * no provoque la reprogramación del scheduler.
     */
    @Test
    void noDeberiaReprogramarPorOtraConfiguracion() {
        ComisionScheduler scheduler = crearScheduler();

        scheduler.alActualizarConfiguracion(
                new ConfiguracionActualizadaEvent(
                        ClavesConfiguracion.LIMITE_EXTRACCION_TITULAR,
                        "150000.00"
                )
        );

        verify(taskScheduler, never())
                .schedule(
                        any(Runnable.class),
                        any(Trigger.class)
                );
    }

    /**
     * Construye el scheduler utilizado en cada escenario de prueba.
     *
     * @return scheduler preparado con dependencias simuladas
     */
    private ComisionScheduler crearScheduler() {
        return new ComisionScheduler(
                taskScheduler,
                comisionService,
                configuracionService
        );
    }
}
