package ar.edu.unju.fi.arquitecturas.tp2.service;

import java.math.BigDecimal;

/**
 * Contrato de servicios para consultar los parámetros dinámicos
 * de configuración general del sistema bancario.
 *
 * <p>
 * Los valores se almacenan en la base de datos como cadenas de texto
 * y son convertidos al tipo requerido por la lógica de negocio.
 * Esto permite modificar parámetros como límites de extracción
 * o configuraciones del scheduler sin recompilar la aplicación.
 * </p>
 *
 * @author MaxDz
 * @version 1.0.0
 */
public interface ConfiguracionService {

    /**
     * Obtiene el valor de una configuración como texto.
     *
     * @param clave clave única que identifica al parámetro
     * @return valor almacenado para la clave solicitada
     * @throws ar.edu.unju.fi.arquitecturas.tp2.exception.RecursoNoEncontradoException
     *         si no existe una configuración asociada a la clave
     */
    String obtenerValorTexto(String clave);

    /**
     * Obtiene el valor de una configuración convertido a decimal.
     *
     * <p>
     * Este método está destinado principalmente a parámetros monetarios,
     * como los límites diarios de extracción.
     * </p>
     *
     * @param clave clave única que identifica al parámetro
     * @return valor convertido a {@link BigDecimal}
     * @throws ar.edu.unju.fi.arquitecturas.tp2.exception.RecursoNoEncontradoException
     *         si no existe una configuración asociada a la clave
     * @throws IllegalStateException si el valor almacenado no puede
     *                               convertirse a decimal
     */
    BigDecimal obtenerValorDecimal(String clave);

    /**
     * Obtiene el valor de una configuración convertido a entero.
     *
     * @param clave clave única que identifica al parámetro
     * @return valor convertido a entero
     * @throws ar.edu.unju.fi.arquitecturas.tp2.exception.RecursoNoEncontradoException
     *         si no existe una configuración asociada a la clave
     * @throws IllegalStateException si el valor almacenado no puede
     *                               convertirse a entero
     */
    int obtenerValorEntero(String clave);

    /**
     * Recupera todos los parámetros de configuración registrados.
     * @return Lista de DTOs con la configuración.
     */
    java.util.List<ar.edu.unju.fi.arquitecturas.tp2.dto.ConfiguracionResponseDto> obtenerTodas();

    /**
     * Recupera los detalles de un parámetro de configuración específico.
     * @param clave Clave del parámetro.
     * @return DTO con los detalles.
     */
    ar.edu.unju.fi.arquitecturas.tp2.dto.ConfiguracionResponseDto obtenerPorClave(String clave);

    /**
     * Actualiza el valor de un parámetro de configuración existente.
     * @param clave Clave del parámetro a actualizar.
     * @param request Nuevo valor a almacenar.
     * @return DTO con la configuración actualizada.
     */
    ar.edu.unju.fi.arquitecturas.tp2.dto.ConfiguracionResponseDto actualizar(String clave, ar.edu.unju.fi.arquitecturas.tp2.dto.ConfiguracionUpdateRequestDto request);
}