package ar.edu.unju.fi.arquitecturas.tp2.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class TransferenciaRequestDto {

    @NotNull
    private UUID cuentaOrigenId;

    @NotNull
    private UUID cuentaDestinoId;

    @NotNull
    @Positive
    private BigDecimal monto;
}