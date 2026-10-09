package ar.edu.unju.fi.arquitecturas.tp2.repository;

import ar.edu.unju.fi.arquitecturas.tp2.model.Transaccion;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoTransaccion;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.TipoTransaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Repositorio para la persistencia y consulta de transacciones.
 */
public interface TransaccionRepository
        extends JpaRepository<Transaccion, UUID> {

    long countByEstadoTransaccion(
            EstadoTransaccion estadoTransaccion
    );

    long countByTipo(TipoTransaccion tipo);

    /**
     * Obtiene el monto total de las transacciones realizadas
     * por un operador dentro de un intervalo determinado.
     *
     * <p>
     * El tipo y el estado se reciben como parámetros para permitir
     * utilizar esta consulta exclusivamente sobre las operaciones
     * que correspondan a la regla de negocio requerida.
     * </p>
     *
     * @param operadorId identificador del operador
     * @param tipo tipo de transacción
     * @param estado estado de la transacción
     * @param desde inicio inclusivo del intervalo
     * @param hasta fin exclusivo del intervalo
     * @return suma de los montos; cero cuando no existen operaciones
     */
    @Query("""
            SELECT COALESCE(SUM(t.monto), 0)
            FROM Transaccion t
            WHERE t.operador.id = :operadorId
              AND t.tipo = :tipo
              AND t.estadoTransaccion = :estado
              AND t.fechaHora >= :desde
              AND t.fechaHora < :hasta
            """)
    BigDecimal sumarMontoPorOperadorTipoEstadoYPeriodo(
            @Param("operadorId") UUID operadorId,
            @Param("tipo") TipoTransaccion tipo,
            @Param("estado") EstadoTransaccion estado,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta
    );
}
