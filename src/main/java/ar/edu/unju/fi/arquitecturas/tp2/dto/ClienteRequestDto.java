package ar.edu.unju.fi.arquitecturas.tp2.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClienteRequestDto {

    @NotBlank
    @Size(max = 120)
    private String nombre;

    @NotBlank
    @Pattern(regexp = "\\d{11}", message = "El CUIL debe contener exactamente 11 dígitos")
    private String cuil;

    @NotBlank
    @Email
    @Size(max = 254)
    private String email;

    @Size(max = 30)
    private String telefono;

    @Size(max = 255)
    private String direccion;
}