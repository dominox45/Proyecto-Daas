package ar.edu.unju.fi.arquitecturas.tp2.model;

import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoCuenta;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "cuentas_financieras")
@Inheritance(strategy = InheritanceType.JOINED)

public abstract class CuentaFinanciera extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 22)
    private String cbu;

    @Column(unique = true, nullable = false)
    private String alias;

    @Column(precision = 19, scale = 2)
    private BigDecimal saldoOperativo;

    @Enumerated(EnumType.STRING)
    private EstadoCuenta estado;
}