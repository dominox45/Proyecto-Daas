package ar.edu.unju.fi.arquitecturas.tp2.modelo;

import ar.edu.unju.fi.arquitecturas.tp2.modelo.enums.EstadoProcesamiento;
import ar.edu.unju.fi.arquitecturas.tp2.modelo.enums.TipoTransaccion;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Builder @AllArgsConstructor @NoArgsConstructor @Getter @Setter
@Entity
@Table(name = "transaccion")
public class Transaccion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fecha;
    private LocalTime hora;
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    private TipoTransaccion tipo;
    @Enumerated(EnumType.STRING)
    private EstadoProcesamiento estadoProcesamiento;

}
