package ar.edu.unju.fi.arquitecturas.tp2.model;

import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoCliente;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Representa a una persona registrada como cliente del sistema bancario.
 *
 * <p>
 * Un cliente puede ser titular de varias cuentas financieras y una
 * cuenta puede tener varios titulares.
 * </p>
 *
 * <p>
 * A partir del TP5, un cliente puede pertenecer a un grupo familiar
 * mediante la referencia a otro cliente como titular principal.
 * Los clientes adherentes pueden además poseer un conjunto explícito
 * de cuentas autorizadas sobre las cuales podrán realizar extracciones.
 * </p>
 *
 * <p>
 * Todo cliente posee además un estado de activación y puede almacenar
 * la información necesaria para completar el proceso de activación
 * mediante token.
 * </p>
 *
 * <p>
 * Hereda las fechas de auditoría de {@link EntidadAuditable}.
 * </p>
 *
 * @see CuentaFinanciera
 * @see EstadoCliente
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "clientes")
public class Cliente extends EntidadAuditable {

    /** Identificador único del cliente, generado como UUID. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Nombre del cliente. Es obligatorio. */
    @Column(nullable = false, length = 120)
    private String nombre;

    /** CUIL único del cliente. Su formato debe validarse por separado. */
    @Column(nullable = false, unique = true, length = 11)
    private String cuil;

    /** Correo electrónico de contacto del cliente. */
    @Column(length = 254)
    private String email;

    /** Número telefónico de contacto del cliente. */
    @Column(length = 30)
    private String telefono;

    /** Dirección de contacto del cliente. */
    @Column(length = 255)
    private String direccion;

    /**
     * Estado actual del cliente dentro del proceso de activación.
     *
     * <p>
     * Los nuevos clientes comienzan pendientes de activación hasta
     * completar correctamente la validación del token correspondiente.
     * </p>
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoCliente estado = EstadoCliente.PENDIENTE_ACTIVACION;

    /**
     * Token utilizado para completar la activación del cliente.
     *
     * <p>
     * Puede ser nulo cuando todavía no fue generado o cuando el flujo
     * de negocio no requiera un token.
     * </p>
     */
    @Column(unique = true, length = 36)
    private String tokenActivacion;

    /**
     * Fecha y hora límite hasta la cual el token de activación
     * puede ser utilizado.
     */
    @Column(name = "token_activacion_expira_en")
    private LocalDateTime tokenActivacionExpiraEn;

    /**
     * Fecha y hora en que el cliente completó correctamente
     * su proceso de activación.
     *
     * <p>
     * Permanece nula mientras el cliente se encuentre pendiente.
     * </p>
     */
    @Column(name = "fecha_activacion")
    private LocalDateTime fechaActivacion;

    /**
     * Cliente titular principal del grupo familiar.
     *
     * <p>
     * Si este atributo es nulo, el cliente se considera titular.
     * Si contiene una referencia, el cliente se considera adherente
     * del cliente referenciado.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "titular_id")
    private Cliente titular;

    /**
     * Cuentas financieras de las que el cliente es titular.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "clientes_cuentas",
            joinColumns = @JoinColumn(name = "cliente_id"),
            inverseJoinColumns = @JoinColumn(name = "cuenta_id")
    )
    @Builder.Default
    private Set<CuentaFinanciera> cuentas = new HashSet<>();

    /**
     * Cuentas pertenecientes al titular sobre las que este cliente,
     * cuando actúa como adherente, fue autorizado explícitamente
     * para realizar extracciones.
     *
     * <p>
     * Esta relación es independiente de {@link #cuentas}, ya que una
     * autorización de operación no convierte al adherente en titular
     * de la cuenta.
     * </p>
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "adherentes_cuentas_autorizadas",
            joinColumns = @JoinColumn(name = "adherente_id"),
            inverseJoinColumns = @JoinColumn(name = "cuenta_id")
    )
    @Builder.Default
    private Set<CuentaFinanciera> cuentasAutorizadas = new HashSet<>();

    /**
     * Asocia una cuenta al cliente como titular y actualiza
     * ambos lados de la relación en memoria.
     *
     * @param cuenta cuenta que se desea asociar
     * @throws IllegalArgumentException si la cuenta es nula
     */
    public void agregarCuenta(CuentaFinanciera cuenta) {
        if (cuenta == null) {
            throw new IllegalArgumentException(
                    "La cuenta no puede ser nula"
            );
        }

        if (cuentas.add(cuenta)) {
            cuenta.getTitulares().add(this);
        }
    }

    /**
     * Desasocia una cuenta de aquellas en las que el cliente
     * figura como titular.
     *
     * @param cuenta cuenta que se desea desasociar
     */
    public void quitarCuenta(CuentaFinanciera cuenta) {
        if (cuenta != null && cuentas.remove(cuenta)) {
            cuenta.getTitulares().remove(this);
        }
    }

    /**
     * Agrega una cuenta al conjunto de cuentas autorizadas
     * para operar como adherente.
     *
     * <p>
     * Las reglas que determinan si la cuenta pertenece realmente
     * al titular del adherente deben validarse previamente
     * en la capa de servicios.
     * </p>
     *
     * @param cuenta cuenta que se desea autorizar
     * @throws IllegalArgumentException si la cuenta es nula
     */
    public void autorizarCuenta(CuentaFinanciera cuenta) {
        if (cuenta == null) {
            throw new IllegalArgumentException(
                    "La cuenta autorizada no puede ser nula"
            );
        }

        cuentasAutorizadas.add(cuenta);
    }

    /**
     * Elimina una cuenta del conjunto de cuentas autorizadas
     * para operar como adherente.
     *
     * @param cuenta cuenta cuya autorización se desea revocar
     */
    public void revocarCuentaAutorizada(CuentaFinanciera cuenta) {
        if (cuenta != null) {
            cuentasAutorizadas.remove(cuenta);
        }
    }
}