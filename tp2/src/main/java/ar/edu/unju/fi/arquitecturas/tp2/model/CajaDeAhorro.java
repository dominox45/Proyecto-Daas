package ar.edu.unju.fi.arquitecturas.tp2.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
@Table(name = "cajas_de_ahorro")

public class CajaDeAhorro extends CuentaFinanciera {

    private BigDecimal tasaInteresAnual;
    private int limiteExtraccionesMensualesSinCosto;
}