package ar.edu.unju.fi.arquitecturas.tp2.dto;

import ar.edu.unju.fi.arquitecturas.tp2.model.enums.TipoDatoConfiguracion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionResponseDto{

    private String clave;
    private String valor;
    private TipoDatoConfiguracion tipoDato;
    private String descripcion;
}