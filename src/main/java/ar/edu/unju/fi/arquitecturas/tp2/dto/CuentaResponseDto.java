package ar.edu.unju.fi.arquitecturas.tp2.dto;

import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoCuenta;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.TipoCuenta;
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
public class CuentaResponseDto {

    private UUID id;
    private TipoCuenta tipoCuenta;
    private String cbu;
    private String alias;
    private BigDecimal saldoOperativo;
    private EstadoCuenta estado;
}