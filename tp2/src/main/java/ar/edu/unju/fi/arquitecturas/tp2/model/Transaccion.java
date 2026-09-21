package ar.edu.unju.fi.arquitecturas.tp2.model;

import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoTransaccion;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.TipoTransaccion;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

@Entity
@Table(name = "transacciones")

public class Transaccion extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private LocalDateTime fechaHora;

    @Column(precision = 19, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    private TipoTransaccion tipo;

    @Enumerated(EnumType.STRING)
    private EstadoTransaccion estadoTransaccion;

}
