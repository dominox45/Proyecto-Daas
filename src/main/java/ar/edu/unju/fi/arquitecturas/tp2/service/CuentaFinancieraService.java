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
public class CuentaFinancieraService {

    private final CuentaFinancieraRepository cuentaRepository;
    private final TransaccionRepository transaccionRepository;

    public CuentaFinancieraService(
            CuentaFinancieraRepository cuentaRepository,
            TransaccionRepository transaccionRepository) {
        this.cuentaRepository = cuentaRepository;
        this.transaccionRepository = transaccionRepository;
    }

    @Transactional
    public CuentaFinanciera depositar(UUID cuentaId, BigDecimal monto) {
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException("El monto debe ser positivo");
        }

        CuentaFinanciera cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() ->
                        new IllegalArgumentException("La cuenta no existe"));

        if (cuenta.getEstado() != EstadoCuenta.ACTIVA) {
            throw new IllegalStateException("La cuenta no está activa");
        }

        cuenta.setSaldoOperativo(cuenta.getSaldoOperativo().add(monto));

        CuentaFinanciera cuentaGuardada = cuentaRepository.save(cuenta);

        Transaccion transaccion = Transaccion.builder()
                .fechaHora(LocalDateTime.now())
                .monto(monto)
                .tipo(TipoTransaccion.DEPOSITO)
                .estadoTransaccion(EstadoTransaccion.COMPLETADA)
                .cuenta(cuentaGuardada)
                .build();

        transaccionRepository.save(transaccion);

        return cuentaGuardada;
    }

    @Transactional
    public CuentaFinanciera extraer(UUID cuentaId, BigDecimal monto) {
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException("El monto debe ser positivo");
        }

        CuentaFinanciera cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() ->
                        new IllegalArgumentException("La cuenta no existe"));

        if (cuenta.getEstado() != EstadoCuenta.ACTIVA) {
            throw new IllegalStateException("La cuenta no está activa");
        }

        if (cuenta.getSaldoOperativo().compareTo(monto) < 0) {
            throw new IllegalStateException("Saldo insuficiente");
        }

        cuenta.setSaldoOperativo(cuenta.getSaldoOperativo().subtract(monto));

        CuentaFinanciera cuentaGuardada = cuentaRepository.save(cuenta);

        Transaccion transaccion = Transaccion.builder()
                .fechaHora(LocalDateTime.now())
                .monto(monto)
                .tipo(TipoTransaccion.EXTRACCION)
                .estadoTransaccion(EstadoTransaccion.COMPLETADA)
                .cuenta(cuentaGuardada)
                .build();

        transaccionRepository.save(transaccion);

        return cuentaGuardada;
    }
}