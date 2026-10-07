package ar.edu.unju.fi.arquitecturas.tp2.dto;

import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoTransaccion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferenciaResponseDto {

    private UUID cuentaOrigenId;
    private UUID cuentaDestinoId;
    private BigDecimal monto;
    private EstadoTransaccion estado;
    private LocalDateTime fechaHora;
}