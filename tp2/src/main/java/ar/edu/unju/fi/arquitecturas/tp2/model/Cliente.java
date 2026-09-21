package ar.edu.unju.fi.arquitecturas.tp2.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

@Entity
@Table(name = "clientes")

public class Cliente extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String nombre;
    private String cuil;
    private String email;
    private String telefono;
    private String direccion;

}
