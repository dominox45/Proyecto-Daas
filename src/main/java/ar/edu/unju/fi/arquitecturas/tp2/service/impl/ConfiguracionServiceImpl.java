package ar.edu.unju.fi.arquitecturas.tp2.service.impl;

import ar.edu.unju.fi.arquitecturas.tp2.dto.ConfiguracionResponseDto;
import ar.edu.unju.fi.arquitecturas.tp2.dto.ConfiguracionUpdateRequestDto;
import ar.edu.unju.fi.arquitecturas.tp2.exception.RecursoNoEncontradoException;
import ar.edu.unju.fi.arquitecturas.tp2.model.ConfiguracionGeneral;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.TipoDatoConfiguracion;
import ar.edu.unju.fi.arquitecturas.tp2.repository.ConfiguracionGeneralRepository;
import ar.edu.unju.fi.arquitecturas.tp2.service.ConfiguracionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de configuración general del sistema.
 *
 * <p>
 * Recupera y actualiza los parámetros almacenados en la base de datos mediante
 * {@link ConfiguracionGeneralRepository}. Realiza las conversiones necesarias
 * desde el valor persistido como texto hacia el tipo requerido por la lógica de negocio,
 * y asegura la integridad de los datos validando los tipos antes de cualquier actualización.
 * </p>
 *
 * <p>
 * Las operaciones son de solo lectura por defecto, aplicando transaccionalidad
 * de escritura únicamente en las operaciones de modificación.
 * </p>
 *
 * @see ConfiguracionService
 * @see ConfiguracionGeneralRepository
 * @see ConfiguracionGeneral
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class ConfiguracionServiceImpl implements ConfiguracionService {

    private final ConfiguracionGeneralRepository configuracionRepository;

    /**
     * Crea el servicio utilizando el repositorio de configuración general.
     *
     * @param configuracionRepository repositorio utilizado para consultar
     *                                y persistir los parámetros
     */
    public ConfiguracionServiceImpl(ConfiguracionGeneralRepository configuracionRepository) {
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
            throw new IllegalStateException("El valor para la clave " + clave + " no es un decimal válido.");
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
            throw new IllegalStateException("El valor para la clave " + clave + " no es un entero válido.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ConfiguracionResponseDto> obtenerTodas() {
        return configuracionRepository.findAll().stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfiguracionResponseDto obtenerPorClave(String clave) {
        return mapearAResponse(buscarConfiguracion(clave));
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Antes de persistir el cambio, verifica de manera estricta que el nuevo valor
     * en formato texto sea compatible con el {@link TipoDatoConfiguracion} original
     * del registro.
     * </p>
     */
    @Override
    @Transactional
    public ConfiguracionResponseDto actualizar(String clave, ConfiguracionUpdateRequestDto request) {
        ConfiguracionGeneral config = buscarConfiguracion(clave);

        // Validación estricta según el tipo de dato original
        validarValorPorTipo(request.getValor(), config.getTipoDato());

        config.setValor(request.getValor());
        ConfiguracionGeneral actualizada = configuracionRepository.save(config);

        log.info("Configuración '{}' actualizada con el valor: {}", clave, actualizada.getValor());

        return mapearAResponse(actualizada);
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
                .orElseThrow(() -> new RecursoNoEncontradoException("Configuración no encontrada para la clave: " + clave));
    }

    /**
     * Verifica que la cadena de texto proporcionada pueda ser convertida
     * de manera segura al tipo de dato nativo correspondiente a la configuración.
     *
     * <p>
     * Esta validación previene que la base de datos almacene valores inconsistentes
     * (por ejemplo, texto alfabético en un parámetro configurado como DECIMAL),
     * lo cual provocaría excepciones posteriores en la lógica de negocio.
     * </p>
     *
     * @param valor valor en formato texto que se desea validar
     * @param tipo tipo de dato esperado por la configuración
     * @throws IllegalArgumentException si el valor no puede ser convertido al tipo esperado
     */
    private void validarValorPorTipo(String valor, TipoDatoConfiguracion tipo) {
        switch (tipo) {
            case DECIMAL -> {
                try {
                    new BigDecimal(valor);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("El valor ingresado no es un DECIMAL válido.");
                }
            }
            case ENTERO -> {
                try {
                    Integer.parseInt(valor);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("El valor ingresado no es un ENTERO válido.");
                }
            }
            case BOOLEANO -> {
                if (!valor.equalsIgnoreCase("true") && !valor.equalsIgnoreCase("false")) {
                    throw new IllegalArgumentException("El valor ingresado no es un BOOLEANO válido (true/false).");
                }
            }
            case TEXTO -> {
                // El texto admite cualquier valor
            }
        }
    }

    /**
     * Convierte una entidad {@link ConfiguracionGeneral} en su representación
     * de transferencia de datos (DTO).
     *
     * @param config entidad que se desea mapear
     * @return DTO con los detalles públicos de la configuración
     */
    private ConfiguracionResponseDto mapearAResponse(ConfiguracionGeneral config) {
        return new ConfiguracionResponseDto(
                config.getClave(),
                config.getValor(),
                config.getTipoDato(),
                config.getDescripcion()
        );
    }
}