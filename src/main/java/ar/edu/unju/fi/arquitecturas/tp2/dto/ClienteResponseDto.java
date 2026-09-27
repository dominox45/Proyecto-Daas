package ar.edu.unju.fi.arquitecturas.tp2.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponseDto {

    private UUID id;
    private String nombre;
    private String cuil;
    private String email;
    private String telefono;
    private String direccion;
}