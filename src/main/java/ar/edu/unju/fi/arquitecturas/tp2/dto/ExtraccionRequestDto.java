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
 * DTO utilizado para solicitar una extracción sobre una cuenta financiera.
 *
 * <p>
 * Identifica al cliente que ejecuta la operación y el monto
 * que desea extraer.
 * </p>
 *
 * @author MaxDz
 * @version 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExtraccionRequestDto {

    /**
     * Identificador del cliente que realiza la extracción.
     *
     * <p>
     * Puede corresponder al titular de la cuenta o a un adherente
     * autorizado explícitamente para operar sobre ella.
     * </p>
     */
    @NotNull
    private UUID operadorId;

    /**
     * Monto que se desea extraer.
     */
    @NotNull
    @Positive
    private BigDecimal monto;
}