package ar.edu.unju.fi.arquitecturas.tp2.modelo;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Builder @AllArgsConstructor @NoArgsConstructor @Getter @Setter

@Entity @Table(name = "cliente")

public class Cliente{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String nombre;
    private String cuil;
    private String email;
    private String telefono;
    private String direccion;

}
