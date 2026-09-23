package ar.edu.unju.fi.arquitecturas.tp2.repository;

import ar.edu.unju.fi.arquitecturas.tp2.model.CuentaCorriente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.UUID;

public interface CuentaCorrienteRepository extends JpaRepository<CuentaCorriente, UUID> {

    long countByDescubiertoAutorizadoGreaterThan(BigDecimal descubiertoAutorizado);

    long countByCostoComisionMantenimientoMensualGreaterThan(BigDecimal costoComisionMantenimientoMensual);
}