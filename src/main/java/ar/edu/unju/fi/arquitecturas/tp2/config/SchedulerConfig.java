package ar.edu.unju.fi.arquitecturas.tp2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Configuración de la infraestructura de tareas programadas (Scheduling).
 * Se aísla en esta clase para no afectar la clase principal de la aplicación
 * y permitir una personalización del pool de hilos.
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("CronTask-");
        scheduler.initialize();
        return scheduler;
    }
}