package ar.edu.unju.fi.arquitecturas.tp2.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

//Atajos q proporciona la libreria lombok
@SuperBuilder @AllArgsConstructor @NoArgsConstructor @Getter @Setter
//Entity indica que esta clase se convertira en una tabla de la bd
//Table indica por otra parte el nombre que debera tener la bd
@Entity @Table(name = "cliente")


//implementamos la auditoria a travez de este comando, cual permite vijilar las fechas
//de creacion y actualizacion de la tabla cliente
@EntityListeners(AuditingEntityListener.class)

public class Cliente extends Persona {

    private String email;
    private Integer telefono;
    private String direccion;


    //aca ponemos lo nescesario para la auditoria. usando anotaciones
    //debajo de ellas debo colocar a la variable que modifican obligatoriamente
    @CreatedDate
    //indica la naturaleza de la fecha de creacion del registro y su inmutabilidad
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

}
