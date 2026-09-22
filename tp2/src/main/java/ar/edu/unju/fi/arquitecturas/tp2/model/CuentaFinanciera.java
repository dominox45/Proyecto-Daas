package ar.edu.unju.fi.arquitecturas.tp2.model;

import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoCuenta;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Representa una cuenta financiera del sistema bancario.
 *
 * <p>Es la clase base de {@link CajaDeAhorro} y
 * {@link CuentaCorriente}. Utiliza la estrategia de herencia
 * JOINED para almacenar los atributos comunes y específicos
 * en tablas relacionadas.</p>
 *
 * <p>Una cuenta puede tener varios titulares y registrar
 * múltiples transacciones.</p>
 *
 * @see Cliente
 * @see Transaccion
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cuentas_financieras")
@Inheritance(strategy = InheritanceType.JOINED)

public abstract class CuentaFinanciera extends EntidadAuditable {

    /** Identificador único de la cuenta, generado como UUID. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Clave bancaria uniforme (CBU), única y obligatoria. */
    @Column(unique = true, nullable = false, length = 22)
    private String cbu;

    /** Alias único y obligatorio de la cuenta. */
    @Column(unique = true, nullable = false, length = 100)
    private String alias;

    /** Saldo operativo de la cuenta, expresado con dos decimales. */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldoOperativo;

    /** Estado actual de la cuenta. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCuenta estado;

    /** Clientes titulares de esta cuenta; lado inverso de la co-titularidad. */
    @ManyToMany(mappedBy = "cuentas", fetch = FetchType.LAZY)
    private Set<Cliente> titulares = new HashSet<>();

    /** Transacciones asociadas a esta cuenta; lado inverso de la relación. */
    @OneToMany(mappedBy = "cuenta", fetch = FetchType.LAZY)
    private List<Transaccion> transacciones = new ArrayList<>();
}