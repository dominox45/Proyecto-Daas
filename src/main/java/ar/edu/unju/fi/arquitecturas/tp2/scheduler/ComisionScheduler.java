package ar.edu.unju.fi.arquitecturas.tp2.scheduler;

import ar.edu.unju.fi.arquitecturas.tp2.event.ConfiguracionActualizadaEvent;
import ar.edu.unju.fi.arquitecturas.tp2.service.ComisionService;
import ar.edu.unju.fi.arquitecturas.tp2.service.ConfiguracionService;
import ar.edu.unju.fi.arquitecturas.tp2.util.ClavesConfiguracion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledFuture;

/**
 * Componente dinámico encargado de programar la ejecución automatizada
 * del cobro de comisiones.
 *
 * <p>
 * A diferencia de un {@code @Scheduled} estático, este scheduler obtiene
 * su expresión Cron desde la configuración general almacenada en la base
 * de datos.
 * </p>
 *
 * <p>
 * La programación inicial se realiza cuando la aplicación se encuentra
 * completamente inicializada, garantizando que los parámetros generales
 * hayan sido creados previamente.
 * </p>
 *
 * <p>
 * Además, escucha eventos de actualización de configuración para
 * reprogramar la tarea dinámicamente cuando cambia la expresión Cron.
 * </p>
 */
@Slf4j
@Component
public class ComisionScheduler {

    private final TaskScheduler taskScheduler;
    private final ComisionService comisionService;
    private final ConfiguracionService configuracionService;

    private ScheduledFuture<?> tareaActual;

    /**
     * Construye el scheduler mediante inyección de dependencias.
     *
     * @param taskScheduler servicio encargado de programar tareas
     * @param comisionService servicio que ejecuta la liquidación de comisiones
     * @param configuracionService servicio para consultar la configuración general
     */
    public ComisionScheduler(
            @Qualifier("taskScheduler") TaskScheduler taskScheduler,
            ComisionService comisionService,
            ConfiguracionService configuracionService) {

        this.taskScheduler = taskScheduler;
        this.comisionService = comisionService;
        this.configuracionService = configuracionService;
    }

    /**
     * Realiza la programación inicial cuando Spring Boot terminó de
     * inicializar el contexto y ejecutar los runners de la aplicación.
     *
     * <p>
     * De esta forma, {@code ConfiguracionInicializador} ya tuvo la
     * oportunidad de crear {@code CRON_LIQUIDACION_COMISIONES}
     * en una base de datos nueva.
     * </p>
     *
     * @param evento evento emitido cuando la aplicación está lista
     */
    @EventListener(ApplicationReadyEvent.class)
    public void iniciarProgramacion(ApplicationReadyEvent evento) {
        programarTarea();
    }

    /**
     * Reacciona ante modificaciones de la configuración general.
     *
     * <p>
     * Solamente una modificación de la expresión Cron de liquidación
     * provoca la reprogramación de esta tarea.
     * </p>
     *
     * @param evento configuración que fue modificada
     */
    @EventListener
    public void alActualizarConfiguracion(
            ConfiguracionActualizadaEvent evento) {

        if (ClavesConfiguracion.CRON_LIQUIDACION_COMISIONES
                .equals(evento.clave())) {

            log.info(
                    "Se detectó un cambio en el CRON de comisiones. Reprogramando tarea..."
            );

            programarTarea();
        }
    }

    /**
     * Cancela la programación anterior, si existe, y programa una nueva
     * ejecución utilizando la expresión Cron almacenada en la configuración
     * general.
     */
    private synchronized void programarTarea() {

        String expresionCron =
                configuracionService.obtenerValorTexto(
                        ClavesConfiguracion.CRON_LIQUIDACION_COMISIONES
                );

        CronTrigger cronTrigger;

        try {
            cronTrigger = new CronTrigger(expresionCron);
        } catch (IllegalArgumentException e) {
            log.error(
                    "No se pudo programar la liquidación de comisiones. "
                            + "La expresión CRON '{}' es inválida.",
                    expresionCron
            );
            return;
        }

        if (tareaActual != null && !tareaActual.isCancelled()) {
            tareaActual.cancel(false);
            log.info(
                    "Tarea anterior de liquidación de comisiones cancelada."
            );
        }

        tareaActual = taskScheduler.schedule(
                () -> {
                    log.info(
                            "Ejecutando liquidación mensual de comisiones..."
                    );

                    comisionService.liquidarComisionesMensuales();

                    log.info(
                            "Liquidación mensual de comisiones finalizada."
                    );
                },
                cronTrigger
        );

        log.info(
                "Liquidación de comisiones programada con CRON: [{}]",
                expresionCron
        );
    }
}
