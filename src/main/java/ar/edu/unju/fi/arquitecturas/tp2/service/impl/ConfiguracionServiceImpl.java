package ar.edu.unju.fi.arquitecturas.tp2.service.impl;

import ar.edu.unju.fi.arquitecturas.tp2.exception.RecursoNoEncontradoException;
import ar.edu.unju.fi.arquitecturas.tp2.model.ConfiguracionGeneral;
import ar.edu.unju.fi.arquitecturas.tp2.repository.ConfiguracionGeneralRepository;
import ar.edu.unju.fi.arquitecturas.tp2.service.ConfiguracionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Implementación del servicio de configuración general del sistema.
 *
 * <p>
 * Recupera los parámetros almacenados en la base de datos mediante
 * {@link ConfiguracionGeneralRepository} y realiza las conversiones
 * necesarias desde el valor persistido como texto hacia el tipo
 * requerido por la lógica de negocio.
 * </p>
 *
 * <p>
 * Las operaciones son de solo lectura porque este servicio se utiliza
 * exclusivamente para consultar parámetros dinámicos del sistema.
 * </p>
 *
 * @author MaxDz
 * @version 1.0.0
 * @see ConfiguracionService
 * @see ConfiguracionGeneralRepository
 * @see ConfiguracionGeneral
 */
@Service
@Transactional(readOnly = true)
public class ConfiguracionServiceImpl implements ConfiguracionService {

    private final ConfiguracionGeneralRepository configuracionRepository;

    /**
     * Crea el servicio utilizando el repositorio de configuración general.
     *
     * @param configuracionRepository repositorio utilizado para recuperar
     *                                los parámetros persistidos
     */
    public ConfiguracionServiceImpl(
            ConfiguracionGeneralRepository configuracionRepository) {
        this.configuracionRepository = configuracionRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String obtenerValorTexto(String clave) {
        return buscarConfiguracion(clave).getValor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal obtenerValorDecimal(String clave) {
        ConfiguracionGeneral config = buscarConfiguracion(clave);

        try {
            return new BigDecimal(config.getValor());
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "El valor para la clave " + clave
                            + " no es un decimal válido."
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int obtenerValorEntero(String clave) {
        ConfiguracionGeneral config = buscarConfiguracion(clave);

        try {
            return Integer.parseInt(config.getValor());
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "El valor para la clave " + clave
                            + " no es un entero válido."
            );
        }
    }

    /**
     * Recupera una configuración mediante su clave única.
     *
     * @param clave clave del parámetro solicitado
     * @return configuración asociada a la clave
     * @throws RecursoNoEncontradoException si no existe un parámetro
     *                                      asociado a la clave indicada
     */
    private ConfiguracionGeneral buscarConfiguracion(String clave) {
        return configuracionRepository.findByClave(clave)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Configuración no encontrada para la clave: " + clave
                ));
    }
}