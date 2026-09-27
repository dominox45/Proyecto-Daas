package ar.edu.unju.fi.arquitecturas.tp2.model;

import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoTransaccion;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.TipoTransaccion;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representa una transacción registrada en una cuenta financiera.
 *
 * <p>Cada transacción pertenece a una única {@link CuentaFinanciera}.
 * Hereda los campos de auditoría de {@link EntidadAuditable}.</p>
 */

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "transacciones")

public class Transaccion extends EntidadAuditable {

    /** Identificador único de la transacción, generado como UUID. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Fecha y hora en que se realiza la transacción. */
    @Column(nullable = false)
    private LocalDateTime fechaHora;

    /** Importe de la transacción, expresado con dos decimales. */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monto;

    /** Tipo de operación registrada. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTransaccion tipo;

    /** Estado de la transacción. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoTransaccion estadoTransaccion;

    /** Cuenta financiera a la que pertenece esta transacción. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuenta_id", nullable = false)
    private CuentaFinanciera cuenta;
}