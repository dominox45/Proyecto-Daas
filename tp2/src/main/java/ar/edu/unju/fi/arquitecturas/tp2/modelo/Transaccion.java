package ar.edu.unju.fi.arquitecturas.tp2.modelo;

import ar.edu.unju.fi.arquitecturas.tp2.modelo.enums.EstadoProcesamiento;
import ar.edu.unju.fi.arquitecturas.tp2.modelo.enums.TipoTransaccion;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Builder @AllArgsConstructor @NoArgsConstructor @Getter @Setter
@Entity // indica que sera una tabla
@Table(name = "transaccion") //Indicamos el nombre de la tabla
@EntityListeners(AuditingEntityListener.class) //añadimos campo para la auditoria
public class Transaccion {
    @Id //Indicamos la clave primaria para nuestra tabla
    @GeneratedValue(strategy = GenerationType.IDENTITY) //indicamos que sera de autogeneracion
    private Long id;

    private LocalDate fecha;
    private LocalTime hora;
    private Double monto;
//añado los tipo enum
// con EnumType.STRING indicamos que debe
// Guardar el texto  especificado en la clase enum correspodiente por ejemplo
// ("DEPOSITO") en vez del número (0, 1) para evitar que se rompa la BD si reordenamos el Enum
    @Enumerated(EnumType.STRING)
    private TipoTransaccion tipo;
    @Enumerated(EnumType.STRING)
    private EstadoProcesamiento estadoProcesamiento;

    //etiqueta de auditoria que sera usada para que se guarde la fecha de creacion del
    //registro correspondiente
    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;//atributo que almacenara dicha info
    @LastModifiedDate
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;



}
