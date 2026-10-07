package ar.edu.unju.fi.arquitecturas.tp2.service;

import ar.edu.unju.fi.arquitecturas.tp2.dto.CuentaRequestDto;
import ar.edu.unju.fi.arquitecturas.tp2.dto.CuentaResponseDto;
import ar.edu.unju.fi.arquitecturas.tp2.exception.RecursoNoEncontradoException;
import ar.edu.unju.fi.arquitecturas.tp2.exception.SaldoInsuficienteException;
import ar.edu.unju.fi.arquitecturas.tp2.model.CuentaFinanciera;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Contrato de servicios para la gestión de cuentas financieras
 * del sistema bancario.
 *
 * <p>
 * Define las operaciones de negocio relacionadas con la creación,
 * consulta y movimientos de fondos sobre las cuentas financieras.
 * </p>
 *
 * <p>
 * Las operaciones destinadas a la capa Controller trabajan con DTOs
 * para evitar la exposición directa de las entidades JPA.
 * </p>
 *
 * @author MaxDz
 * @version 1.1.0
 * @see CuentaFinanciera
 * @see CuentaRequestDto
 * @see CuentaResponseDto
 */
public interface CuentaFinancieraService {

    /**
     * Registra una nueva cuenta financiera asociada a un cliente.
     *
     * @param request datos necesarios para crear la cuenta
     * @return DTO con los datos de la cuenta registrada
     * @throws IllegalArgumentException si los datos incumplen
     *                                  una regla de negocio
     * @throws RecursoNoEncontradoException si el cliente indicado no existe
     */
    CuentaResponseDto crear(CuentaRequestDto request);

    /**
     * Busca una cuenta financiera mediante su CBU.
     *
     * @param cbu Clave Bancaria Uniforme de la cuenta
     * @return DTO con los datos de la cuenta encontrada
     * @throws RecursoNoEncontradoException si no existe una cuenta
     *                                      con el CBU indicado
     */
    CuentaResponseDto buscarPorCbu(String cbu);

    /**
     * Realiza un depósito sobre una cuenta existente.
     *
     * @param cuentaId identificador de la cuenta
     * @param monto monto que se desea depositar
     * @return cuenta financiera actualizada
     * @throws IllegalArgumentException si el monto es nulo o no es positivo
     * @throws RecursoNoEncontradoException si la cuenta no existe
     * @throws IllegalStateException si la cuenta no se encuentra activa
     */
    CuentaFinanciera depositar(UUID cuentaId, BigDecimal monto);

    /**
     * Realiza una extracción sobre una cuenta existente identificando
     * al cliente que ejecuta la operación.
     *
     * <p>
     * Un cliente titular solamente puede operar sobre cuentas de las
     * que sea titular. Un cliente adherente solamente puede realizar
     * extracciones sobre cuentas correspondientes a su titular.
     * </p>
     *
     * <p>
     * En una cuenta corriente la implementación puede considerar
     * el descubierto autorizado como parte de los fondos disponibles.
     * </p>
     *
     * @param cuentaId identificador de la cuenta
     * @param operadorId identificador del cliente que realiza la extracción
     * @param monto monto que se desea extraer
     * @return cuenta financiera actualizada
     * @throws IllegalArgumentException si algún dato obligatorio es inválido
     * @throws RecursoNoEncontradoException si la cuenta o el operador no existen
     * @throws IllegalStateException si la cuenta no se encuentra activa
     *                               o el operador no está autorizado
     * @throws SaldoInsuficienteException si los fondos disponibles
     *                                    no alcanzan para realizar la extracción
     */
    CuentaFinanciera extraer(
            UUID cuentaId,
            UUID operadorId,
            BigDecimal monto
    );
}