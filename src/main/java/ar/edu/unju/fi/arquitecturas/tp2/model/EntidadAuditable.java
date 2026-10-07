package ar.edu.unju.fi.arquitecturas.tp2.model;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Clase base que proporciona campos de auditoría a las entidades del dominio.
 *
 * <p>Sus atributos se heredan y se almacenan en las tablas de las entidades
 * concretas. No representa una entidad independiente.</p>
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class EntidadAuditable {

    /**
     * Fecha y hora de creación de la entidad.
     */
    @CreatedDate
    private LocalDateTime fechaCreacion;

    /**
     * Fecha y hora de la última modificación de la entidad.
     */
    @LastModifiedDate
    private LocalDateTime fechaModificacion;
}