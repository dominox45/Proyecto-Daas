package ar.edu.unju.fi.arquitecturas.tp2.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "cuentas_corrientes")

public class CuentaCorriente extends CuentaFinanciera {

    @Column(precision = 19, scale = 2)
    private BigDecimal descubiertoAutorizado;

    @Column(precision = 19, scale = 2)
    private BigDecimal costoComisionMantenimientoMensual;
}