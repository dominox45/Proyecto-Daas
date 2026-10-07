package ar.edu.unju.fi.arquitecturas.tp2.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionUpdateRequestDto {

    @NotBlank(message = "El valor no puede estar vacío")
    private String valor;
}