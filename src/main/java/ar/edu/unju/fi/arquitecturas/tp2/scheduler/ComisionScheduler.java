package ar.edu.unju.fi.arquitecturas.tp2.scheduler;

import ar.edu.unju.fi.arquitecturas.tp2.event.ConfiguracionActualizadaEvent;
import ar.edu.unju.fi.arquitecturas.tp2.service.ComisionService;
import ar.edu.unju.fi.arquitecturas.tp2.service.ConfiguracionService;
import ar.edu.unju.fi.arquitecturas.tp2.util.ClavesConfiguracion;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
 * A diferencia del @Scheduled estático, este scheduler lee su expresión Cron
 * desde la base de datos al iniciar. Además, escucha eventos de actualización
 * para reprogramarse en caliente si el administrador modifica el horario.
 * </p>
 */
@Slf4j
@Component
public class ComisionScheduler {

    private final TaskScheduler taskScheduler;
    private final ComisionService comisionService;
    private final ConfiguracionService configuracionService;

    private ScheduledFuture<?> tareaActual;

    public ComisionScheduler(
            @Qualifier("taskScheduler") TaskScheduler taskScheduler,
            ComisionService comisionService,
            ConfiguracionService configuracionService) {
        this.taskScheduler = taskScheduler;
        this.comisionService = comisionService;
        this.configuracionService = configuracionService;
    }

    @PostConstruct
    public void iniciarProgramacion() {
        programarTarea();
    }

    @EventListener
    public void alActualizarConfiguracion(ConfiguracionActualizadaEvent evento) {
        if (ClavesConfiguracion.CRON_LIQUIDACION_COMISIONES.equals(evento.clave())) {
            log.warn("Se detectó un cambio en el CRON de comisiones. Reprogramando tarea...");
            programarTarea();
        }
    }

    private synchronized void programarTarea() {
        if (tareaActual != null && !tareaActual.isCancelled()) {
            tareaActual.cancel(false);
            log.info("Tarea de liquidación anterior cancelada.");
        }

        String expresionCron = configuracionService.obtenerValorTexto(ClavesConfiguracion.CRON_LIQUIDACION_COMISIONES);
        log.info("Programando próxima liquidación de comisiones con CRON: [{}]", expresionCron);

        try {
            tareaActual = taskScheduler.schedule(
                    () -> {
                        log.info(">>> DISPARADOR CRON: Ejecutando liquidación mensual de comisiones...");
                        comisionService.liquidarComisionesMensuales();
                        log.info("<<< DISPARADOR CRON: Ejecución finalizada.");
                    },
                    new CronTrigger(expresionCron)
            );
        } catch (IllegalArgumentException e) {
            log.error("Fallo crítico al programar comisiones. La expresión CRON '{}' es inválida.", expresionCron);
        }
    }
}