package ar.edu.unju.fi.arquitecturas.tp2.dto;

import ar.edu.unju.fi.arquitecturas.tp2.model.enums.TipoCuenta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CuentaRequestDto {

    @NotNull
    private UUID clienteId;

    @NotNull
    private TipoCuenta tipoCuenta;

    @NotBlank
    @Size(min = 22, max = 22)
    private String cbu;

    @NotBlank
    @Size(max = 100)
    private String alias;

    @PositiveOrZero
    private BigDecimal tasaInteresAnual;

    @PositiveOrZero
    private Integer limiteExtraccionesMensualesSinCosto;

    @PositiveOrZero
    private BigDecimal descubiertoAutorizado;

    @PositiveOrZero
    private BigDecimal costoComisionMantenimientoMensual;
}