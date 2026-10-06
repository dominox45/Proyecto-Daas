package ar.edu.unju.fi.arquitecturas.tp2.util;

import ar.edu.unju.fi.arquitecturas.tp2.model.ConfiguracionGeneral;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.TipoDatoConfiguracion;
import ar.edu.unju.fi.arquitecturas.tp2.repository.ConfiguracionGeneralRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Componente encargado de cargar los parámetros de configuración iniciales
 * en la base de datos la primera vez que se levanta el sistema.
 */
@Component
public class ConfiguracionInicializador implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ConfiguracionInicializador.class);
    private final ConfiguracionGeneralRepository repository;

    public ConfiguracionInicializador(ConfiguracionGeneralRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        log.info("Verificando parámetros de configuración general...");

        crearSiNoExiste(
                ClavesConfiguracion.LIMITE_EXTRACCION_TITULAR,
                "100000.00",
                TipoDatoConfiguracion.DECIMAL,
                "Límite diario de extracción para clientes titulares"
        );

        crearSiNoExiste(
                ClavesConfiguracion.LIMITE_EXTRACCION_ADHERENTE,
                "70000.00",
                TipoDatoConfiguracion.DECIMAL,
                "Límite diario de extracción para clientes adherentes"
        );

        crearSiNoExiste(
                ClavesConfiguracion.CRON_LIQUIDACION_COMISIONES,
                "0 0 0 1 * ?", // Ejecutar el primer día de cada mes a las 00:00
                TipoDatoConfiguracion.TEXTO,
                "Expresión CRON para la liquidación automática de comisiones"
        );

        log.info("Configuración general inicializada correctamente.");
    }

    private void crearSiNoExiste(String clave, String valor, TipoDatoConfiguracion tipo, String descripcion) {
        if (repository.findByClave(clave).isEmpty()) {
            ConfiguracionGeneral config = new ConfiguracionGeneral();
            config.setClave(clave);
            config.setValor(valor);
            config.setTipoDato(tipo);
            config.setDescripcion(descripcion);
            repository.save(config);
            log.info("Parámetro creado: {}", clave);
        }
    }
}