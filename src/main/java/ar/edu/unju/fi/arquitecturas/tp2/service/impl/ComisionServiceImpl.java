package ar.edu.unju.fi.arquitecturas.tp2.service.impl;

import ar.edu.unju.fi.arquitecturas.tp2.config.ComisionesProperties;
import ar.edu.unju.fi.arquitecturas.tp2.model.CajaDeAhorro;
import ar.edu.unju.fi.arquitecturas.tp2.model.CuentaCorriente;
import ar.edu.unju.fi.arquitecturas.tp2.model.CuentaFinanciera;
import ar.edu.unju.fi.arquitecturas.tp2.model.Transaccion;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoCuenta;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoTransaccion;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.TipoTransaccion;
import ar.edu.unju.fi.arquitecturas.tp2.repository.CuentaFinancieraRepository;
import ar.edu.unju.fi.arquitecturas.tp2.repository.TransaccionRepository;
import ar.edu.unju.fi.arquitecturas.tp2.service.ComisionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación del servicio encargado de la liquidación automatizada
 * de comisiones mensuales del sistema bancario.
 *
 * <p>
 * Este servicio procesa de forma masiva todas las cuentas financieras
 * en estado activo, aplicando el débito correspondiente al costo de
 * mantenimiento según el tipo específico de cuenta configurado
 * globalmente en el sistema.
 * </p>
 *
 * <p>
 * Las operaciones generadas por este servicio no poseen un operador
 * humano asociado, ya que corresponden a un proceso *batch* del sistema.
 * </p>
 *
 * @see ComisionService
 * @see ComisionesProperties
 */
@Slf4j
@Service
public class ComisionServiceImpl implements ComisionService {

    private final CuentaFinancieraRepository cuentaRepository;
    private final TransaccionRepository transaccionRepository;
    private final ComisionesProperties propiedadesComisiones;

    /**
     * Construye el servicio mediante inyección de dependencias.
     *
     * @param cuentaRepository      repositorio para consultar y actualizar cuentas financieras
     * @param transaccionRepository repositorio para registrar los débitos automáticos
     * @param propiedadesComisiones configuración con los montos globales a debitar
     */
    public ComisionServiceImpl(
            CuentaFinancieraRepository cuentaRepository,
            TransaccionRepository transaccionRepository,
            ComisionesProperties propiedadesComisiones) {

        this.cuentaRepository = cuentaRepository;
        this.transaccionRepository = transaccionRepository;
        this.propiedadesComisiones = propiedadesComisiones;
    }

    /**
     * Ejecuta el proceso masivo de cobro de mantenimiento mensual
     * sobre todas las cuentas activas del sistema.
     *
     * <p>
     * El proceso itera sobre las cuentas, descuenta el saldo operativo
     * y genera una transacción de tipo {@link TipoTransaccion#DEBITO_COMISION}.
     * Las cuentas que no poseen saldo suficiente son omitidas temporalmente
     * hasta que se defina la regla de negocio correspondiente.
     * </p>
     */
    @Override
    @Transactional
    public void liquidarComisionesMensuales() {
        log.info("Iniciando proceso de liquidación masiva de comisiones mensuales...");

        List<CuentaFinanciera> cuentasActivas = cuentaRepository.findAllByEstado(EstadoCuenta.ACTIVA);

        int procesadas = 0;
        int omitidas = 0;

        for (CuentaFinanciera cuenta : cuentasActivas) {
            BigDecimal montoComision = determinarMontoComision(cuenta);

            // TODO: REGLA BLOQUEADA. Qué hacer si la cuenta ACTIVA no tiene fondos suficientes.
            // Según la coordinación del equipo, NO inventar regla. Se omite temporalmente.
            if (cuenta.getSaldoOperativo().compareTo(montoComision) < 0) {
                log.warn("Cuenta ID {} no posee saldo suficiente para cubrir la comisión. Pendiente definición de negocio. Se omite.", cuenta.getId());
                omitidas++;
                continue;
            }

            // Descontar saldo
            cuenta.setSaldoOperativo(cuenta.getSaldoOperativo().subtract(montoComision));
            cuentaRepository.save(cuenta);

            // Registrar transacción automática (sin operador humano)
            Transaccion transaccion = Transaccion.builder()
                    .fechaHora(LocalDateTime.now())
                    .monto(montoComision)
                    .tipo(TipoTransaccion.DEBITO_COMISION)
                    .estadoTransaccion(EstadoTransaccion.COMPLETADA)
                    .cuenta(cuenta)
                    .operador(null)
                    .build();

            transaccionRepository.save(transaccion);
            procesadas++;
        }

        log.info("Liquidación de comisiones finalizada. Procesadas: {}, Omitidas por falta de fondos: {}", procesadas, omitidas);
    }

    /**
     * Determina el monto exacto de la comisión a debitar evaluando el
     * subtipo concreto de la cuenta financiera.
     *
     * @param cuenta entidad financiera a evaluar
     * @return importe correspondiente a la comisión según las propiedades globales,
     *         o {@link BigDecimal#ZERO} si el tipo de cuenta no está soportado.
     */
    private BigDecimal determinarMontoComision(CuentaFinanciera cuenta) {
        if (cuenta instanceof CuentaCorriente) {
            return propiedadesComisiones.getCuentaCorriente();
        } else if (cuenta instanceof CajaDeAhorro) {
            return propiedadesComisiones.getCajaAhorro();
        }
        return BigDecimal.ZERO;
    }
}