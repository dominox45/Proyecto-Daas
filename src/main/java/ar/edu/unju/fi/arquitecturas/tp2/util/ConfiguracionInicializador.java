package ar.edu.unju.fi.arquitecturas.tp2.util;

import ar.edu.unju.fi.arquitecturas.tp2.model.ConfiguracionGeneral;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.TipoDatoConfiguracion;
import ar.edu.unju.fi.arquitecturas.tp2.repository.ConfiguracionGeneralRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Componente encargado de inicializar los parámetros globales
 * utilizados por el sistema bancario.
 *
 * <p>
 * Al iniciar la aplicación verifica la existencia de las configuraciones
 * requeridas por el Trabajo Práctico 5 y crea únicamente aquellas
 * que todavía no se encuentren almacenadas.
 * </p>
 *
 * <p>
 * Este comportamiento permite mantener valores modificados posteriormente
 * por administración sin sobrescribirlos cada vez que la aplicación
 * se reinicia.
 * </p>
 *
 * @author MaxDz
 * @version 1.0.0
 * @see ConfiguracionGeneral
 * @see ConfiguracionGeneralRepository
 * @see ClavesConfiguracion
 */
@Component
public class ConfiguracionInicializador implements CommandLineRunner {

    private static final Logger log =
            LoggerFactory.getLogger(ConfiguracionInicializador.class);

    private final ConfiguracionGeneralRepository repository;

    /**
     * Crea el inicializador utilizando el repositorio
     * de configuración general.
     *
     * @param repository repositorio utilizado para consultar
     *                   y persistir los parámetros iniciales
     */
    public ConfiguracionInicializador(
            ConfiguracionGeneralRepository repository) {
        this.repository = repository;
    }

    /**
     * Verifica y registra las configuraciones iniciales requeridas
     * al comenzar la ejecución de la aplicación.
     *
     * <p>
     * Actualmente se inicializan:
     * </p>
     *
     * <ul>
     *     <li>límite diario de extracción para titulares;</li>
     *     <li>límite diario de extracción para adherentes;</li>
     *     <li>expresión Cron para la liquidación mensual de comisiones.</li>
     * </ul>
     *
     * @param args argumentos recibidos durante el inicio de la aplicación
     */
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
                "0 0 0 1 * ?",
                TipoDatoConfiguracion.TEXTO,
                "Expresión CRON para la liquidación automática de comisiones"
        );

        log.info("Configuración general inicializada correctamente.");
    }

    /**
     * Crea un parámetro de configuración únicamente cuando
     * todavía no existe una entrada asociada a la clave indicada.
     *
     * <p>
     * Si la configuración ya existe se conserva su valor actual,
     * permitiendo que los parámetros puedan modificarse dinámicamente
     * sin ser reemplazados por los valores por defecto.
     * </p>
     *
     * @param clave identificador único del parámetro
     * @param valor valor inicial que se almacenará
     * @param tipo tipo de dato asociado al valor
     * @param descripcion descripción funcional del parámetro
     */
    private void crearSiNoExiste(
            String clave,
            String valor,
            TipoDatoConfiguracion tipo,
            String descripcion) {

        if (repository.findByClave(clave).isEmpty()) {
            ConfiguracionGeneral config =
                    new ConfiguracionGeneral();

            config.setClave(clave);
            config.setValor(valor);
            config.setTipoDato(tipo);
            config.setDescripcion(descripcion);

            repository.save(config);

            log.info("Parámetro creado: {}", clave);
        }
    }
}