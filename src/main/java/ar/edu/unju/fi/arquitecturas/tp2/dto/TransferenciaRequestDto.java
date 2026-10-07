package ar.edu.unju.fi.arquitecturas.tp2.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO utilizado para solicitar una transferencia entre cuentas financieras.
 *
 * <p>
 * Además de identificar las cuentas de origen y destino y el monto,
 * identifica al cliente que intenta ejecutar la operación.
 * </p>
 *
 * @author MaxDz
 * @version 1.1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferenciaRequestDto {

    /**
     * Cliente que intenta realizar la transferencia.
     */
    @NotNull
    private UUID operadorId;

    /**
     * Cuenta desde la que se debitarán los fondos.
     */
    @NotNull
    private UUID cuentaOrigenId;

    /**
     * Cuenta que recibirá los fondos.
     */
    @NotNull
    private UUID cuentaDestinoId;

    /**
     * Monto que se desea transferir.
     */
    @NotNull
    @Positive
    private BigDecimal monto;
}