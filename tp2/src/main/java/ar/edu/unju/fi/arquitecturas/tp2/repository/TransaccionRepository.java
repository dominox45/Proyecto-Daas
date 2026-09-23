package ar.edu.unju.fi.arquitecturas.tp2.repository;

import ar.edu.unju.fi.arquitecturas.tp2.model.Transaccion;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoTransaccion;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.TipoTransaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface TransaccionRepository extends JpaRepository<Transaccion, UUID> {

    long countByEstadoTransaccion(EstadoTransaccion estadoTransaccion);

    long countByTipo(TipoTransaccion tipo);
}