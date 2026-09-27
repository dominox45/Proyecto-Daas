package ar.edu.unju.fi.arquitecturas.tp2.service;

import ar.edu.unju.fi.arquitecturas.tp2.model.CuentaFinanciera;

import java.math.BigDecimal;
import java.util.UUID;

public interface CuentaFinancieraService {

    CuentaFinanciera depositar(UUID cuentaId, BigDecimal monto);

    CuentaFinanciera extraer(UUID cuentaId, BigDecimal monto);
}