package ar.edu.unju.fi.arquitecturas.tp2.service.impl;

import ar.edu.unju.fi.arquitecturas.tp2.dto.TransferenciaRequestDto;
import ar.edu.unju.fi.arquitecturas.tp2.dto.TransferenciaResponseDto;
import ar.edu.unju.fi.arquitecturas.tp2.exception.RecursoNoEncontradoException;
import ar.edu.unju.fi.arquitecturas.tp2.exception.SaldoInsuficienteException;
import ar.edu.unju.fi.arquitecturas.tp2.model.CuentaCorriente;
import ar.edu.unju.fi.arquitecturas.tp2.model.CuentaFinanciera;
import ar.edu.unju.fi.arquitecturas.tp2.model.Transaccion;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoCuenta;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoTransaccion;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.TipoTransaccion;
import ar.edu.unju.fi.arquitecturas.tp2.repository.CuentaFinancieraRepository;
import ar.edu.unju.fi.arquitecturas.tp2.repository.TransaccionRepository;
import ar.edu.unju.fi.arquitecturas.tp2.service.TransferenciaService;
import ar.edu.unju.fi.arquitecturas.tp2.model.Cliente;
import ar.edu.unju.fi.arquitecturas.tp2.repository.ClienteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Implementación del servicio de transferencias.
 * Gestiona la lógica de negocio, validaciones de saldo (incluyendo descubiertos)
 * y persistencia para realizar transferencias entre cuentas financieras.
 */
@Service
public class TransferenciaServiceImpl implements TransferenciaService {

    private static final Logger log = LoggerFactory.getLogger(TransferenciaServiceImpl.class);

    private final CuentaFinancieraRepository cuentaRepository;
    private final TransaccionRepository transaccionRepository;
    private final ClienteRepository clienteRepository;

    /**
     * Constructor para la inyección de dependencias.
     *
     * @param cuentaRepository      Repositorio de cuentas financieras.
     * @param transaccionRepository Repositorio de transacciones.
     */
    public TransferenciaServiceImpl(
            CuentaFinancieraRepository cuentaRepository,
            TransaccionRepository transaccionRepository,
            ClienteRepository clienteRepository) {

        this.cuentaRepository = cuentaRepository;
        this.transaccionRepository = transaccionRepository;
        this.clienteRepository = clienteRepository;
    }

    /**
     * Ejecuta una transferencia de fondos entre dos cuentas financieras.
     *
     * @param request Objeto DTO que contiene los identificadores de cuenta y el monto.
     * @return TransferenciaResponseDto Objeto DTO con el resumen de la operación procesada.
     */
    @Override
    @Transactional
    public TransferenciaResponseDto transferir(
            TransferenciaRequestDto request) {

        validarRequest(request);

        Cliente operador = clienteRepository.findById(
                        request.getOperadorId())
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El operador no existe"
                        )
                );

        /*
         * Regla del TP5:
         * un cliente adherente solamente puede realizar extracciones.
         */
        if (operador.getTitular() != null) {
            throw new IllegalStateException(
                    "Los clientes adherentes solo pueden realizar extracciones"
            );
        }

        CuentaFinanciera origen = cuentaRepository.findById(
                        request.getCuentaOrigenId())
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La cuenta de origen no existe"));

        CuentaFinanciera destino = cuentaRepository.findById(
                        request.getCuentaDestinoId())
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La cuenta de destino no existe"));

        validarCuentasActivas(origen, destino);

        boolean operadorEsTitularDeOrigen =
                origen.getTitulares()
                        .stream()
                        .anyMatch(titular ->
                                titular.getId() != null
                                        && titular.getId().equals(
                                        operador.getId()
                                )
                        );

        if (!operadorEsTitularDeOrigen) {
            throw new IllegalStateException(
                    "El operador no está autorizado para transferir desde esta cuenta"
            );
        }

        BigDecimal monto = request.getMonto();

        validarFondosDisponibles(origen, monto);

        origen.setSaldoOperativo(
                origen.getSaldoOperativo().subtract(monto));

        destino.setSaldoOperativo(
                destino.getSaldoOperativo().add(monto));

        cuentaRepository.save(origen);
        cuentaRepository.save(destino);

        LocalDateTime fechaHora = LocalDateTime.now();

        Transaccion enviada = Transaccion.builder()
                .fechaHora(fechaHora)
                .monto(monto)
                .tipo(TipoTransaccion.TRANSFERENCIA_ENVIADA)
                .estadoTransaccion(EstadoTransaccion.COMPLETADA)
                .cuenta(origen)
                .operador(operador)
                .build();

        Transaccion recibida = Transaccion.builder()
                .fechaHora(fechaHora)
                .monto(monto)
                .tipo(TipoTransaccion.TRANSFERENCIA_RECIBIDA)
                .estadoTransaccion(EstadoTransaccion.COMPLETADA)
                .cuenta(destino)
                .operador(operador)
                .build();

        transaccionRepository.save(enviada);
        transaccionRepository.save(recibida);

        log.info(
                "Transferencia realizada correctamente. origen={}, destino={}, monto={}",
                origen.getId(),
                destino.getId(),
                monto);

        return new TransferenciaResponseDto(
                origen.getId(),
                destino.getId(),
                monto,
                EstadoTransaccion.COMPLETADA,
                fechaHora);
    }

    /**
     * Valida que los datos obligatorios de la solicitud de transferencia estén presentes y sean correctos.
     *
     * @param request DTO con los datos de la transferencia.
     * @throws IllegalArgumentException Si falta algún dato o si el monto no es positivo.
     */
    private void validarRequest(TransferenciaRequestDto request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "La solicitud de transferencia es obligatoria");
        }

        if (request.getOperadorId() == null) {
            throw new IllegalArgumentException(
                    "El operador es obligatorio"
            );
        }

        if (request.getCuentaOrigenId() == null
                || request.getCuentaDestinoId() == null) {
            throw new IllegalArgumentException(
                    "Las cuentas de origen y destino son obligatorias");
        }

        if (request.getCuentaOrigenId()
                .equals(request.getCuentaDestinoId())) {
            throw new IllegalArgumentException(
                    "La cuenta de origen y destino deben ser diferentes");
        }

        if (request.getMonto() == null
                || request.getMonto().signum() <= 0) {
            throw new IllegalArgumentException(
                    "El monto debe ser positivo");
        }
    }

    /**
     * Valida que tanto la cuenta de origen como la de destino se encuentren en estado activo.
     *
     * @param origen  Cuenta financiera de origen.
     * @param destino Cuenta financiera de destino.
     * @throws IllegalStateException Si alguna de las cuentas no está activa.
     */
    private void validarCuentasActivas(
            CuentaFinanciera origen,
            CuentaFinanciera destino) {

        if (origen.getEstado() != EstadoCuenta.ACTIVA
                || destino.getEstado() != EstadoCuenta.ACTIVA) {

            log.warn(
                    "Transferencia rechazada: cuenta inactiva. origen={}, destino={}",
                    origen.getId(),
                    destino.getId());

            throw new IllegalStateException(
                    "Ambas cuentas deben estar activas");
        }
    }

    /**
     * Verifica que la cuenta de origen posea los fondos suficientes para realizar la operación,
     * considerando el descubierto autorizado en caso de ser una Cuenta Corriente.
     *
     * @param origen Cuenta financiera de origen.
     * @param monto  Monto a transferir.
     * @throws SaldoInsuficienteException Si los fondos totales son menores al monto a transferir.
     */
    private void validarFondosDisponibles(
            CuentaFinanciera origen,
            BigDecimal monto) {

        BigDecimal fondosDisponibles =
                origen.getSaldoOperativo();

        if (origen instanceof CuentaCorriente cuentaCorriente) {
            fondosDisponibles = fondosDisponibles.add(
                    cuentaCorriente.getDescubiertoAutorizado());
        }

        if (fondosDisponibles.compareTo(monto) < 0) {

            log.warn(
                    "Transferencia rechazada por saldo insuficiente. origen={}, monto={}, fondosDisponibles={}",
                    origen.getId(),
                    monto,
                    fondosDisponibles);

            throw new SaldoInsuficienteException(
                    "La cuenta de origen no posee fondos suficientes "
                            + "incluyendo el descubierto autorizado");
        }
    }
}