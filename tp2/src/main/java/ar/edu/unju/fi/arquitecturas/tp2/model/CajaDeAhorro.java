package ar.edu.unju.fi.arquitecturas.tp2.model;

import jakarta.persistence.Entity;
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

public class CajaDeAhorro extends CuentaFinanciera {

    private BigDecimal tasaInteresAnual;
    private int limiteExtraccionesMensualesSinCosto;
}