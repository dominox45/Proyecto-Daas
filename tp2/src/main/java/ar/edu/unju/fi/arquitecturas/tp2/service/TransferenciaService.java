
package ar.edu.unju.fi.arquitecturas.tp2.service;

import ar.edu.unju.fi.arquitecturas.tp2.model.CuentaFinanciera;
import ar.edu.unju.fi.arquitecturas.tp2.model.Transaccion;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoCuenta;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoTransaccion;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.TipoTransaccion;
import ar.edu.unju.fi.arquitecturas.tp2.repository.CuentaFinancieraRepository;
import ar.edu.unju.fi.arquitecturas.tp2.repository.TransaccionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TransferenciaService {

    private final CuentaFinancieraRepository cuentaRepository;
    private final TransaccionRepository transaccionRepository;

    public TransferenciaService(
            CuentaFinancieraRepository cuentaRepository,
            TransaccionRepository transaccionRepository) {
        this.cuentaRepository = cuentaRepository;
        this.transaccionRepository = transaccionRepository;
    }

    @Transactional
    public void transferir(UUID origenId, UUID destinoId, BigDecimal monto) {

        if (origenId == null || destinoId == null) {
            throw new IllegalArgumentException("Las cuentas son obligatorias");
        }

        if (origenId.equals(destinoId)) {
            throw new IllegalArgumentException(
                    "La cuenta de origen y destino deben ser diferentes");
        }

        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException("El monto debe ser positivo");
        }

        CuentaFinanciera origen = cuentaRepository.findById(origenId)
                .orElseThrow(() ->
                        new IllegalArgumentException("La cuenta de origen no existe"));

        CuentaFinanciera destino = cuentaRepository.findById(destinoId)
                .orElseThrow(() ->
                        new IllegalArgumentException("La cuenta de destino no existe"));

        if (origen.getEstado() != EstadoCuenta.ACTIVA
                || destino.getEstado() != EstadoCuenta.ACTIVA) {
            throw new IllegalStateException("Ambas cuentas deben estar activas");
        }

        if (origen.getSaldoOperativo().compareTo(monto) < 0) {
            throw new IllegalStateException("Saldo insuficiente");
        }

        // Modificar los saldos.
        origen.setSaldoOperativo(origen.getSaldoOperativo().subtract(monto));
        destino.setSaldoOperativo(destino.getSaldoOperativo().add(monto));

        cuentaRepository.save(origen);
        cuentaRepository.save(destino);

        LocalDateTime fechaHora = LocalDateTime.now();

        // Registrar el movimiento de salida.
        Transaccion enviada = Transaccion.builder()
                .fechaHora(fechaHora)
                .monto(monto)
                .tipo(TipoTransaccion.TRANSFERENCIA_ENVIADA)
                .estadoTransaccion(EstadoTransaccion.COMPLETADA)
                .cuenta(origen)
                .build();

        // Registrar el movimiento de entrada.
        Transaccion recibida = Transaccion.builder()
                .fechaHora(fechaHora)
                .monto(monto)
                .tipo(TipoTransaccion.TRANSFERENCIA_RECIBIDA)
                .estadoTransaccion(EstadoTransaccion.COMPLETADA)
                .cuenta(destino)
                .build();

        transaccionRepository.save(enviada);
        transaccionRepository.save(recibida);
    }
}