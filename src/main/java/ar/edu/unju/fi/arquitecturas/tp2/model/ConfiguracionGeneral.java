package ar.edu.unju.fi.arquitecturas.tp2.model;

import ar.edu.unju.fi.arquitecturas.tp2.model.enums.TipoDatoConfiguracion;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Representa un parámetro de configuración dinámica del sistema bancario.
 *
 * Permite almacenar y modificar parámetros estructurales (como límites o
 * configuraciones del scheduler) sin requerir despliegues de código o
 * modificaciones en el properties.
 *
 * Hereda los campos de auditoría de {@link EntidadAuditable}.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "configuracion_general")
public class ConfiguracionGeneral extends EntidadAuditable {

    /** Identificador único del registro de configuración. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Clave única que identifica al parámetro de configuración.
     * Debe corresponder con las constantes definidas en {@link ar.edu.unju.fi.arquitecturas.tp2.util.ClavesConfiguracion}.
     */
    @Column(unique = true, nullable = false, length = 100)
    private String clave;

    /** Valor del parámetro almacenado genéricamente como cadena de texto. */
    @Column(nullable = false, length = 255)
    private String valor;

    /** Tipo de dato original del valor para facilitar su parseo posterior. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDatoConfiguracion tipoDato;

    /** Descripción opcional que explica la finalidad del parámetro. */
    @Column(length = 255)
    private String descripcion;
}