package ar.edu.unju.fi.arquitecturas.tp2.repository;

import ar.edu.unju.fi.arquitecturas.tp2.model.CuentaFinanciera;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CuentaFinancieraRepository extends JpaRepository<CuentaFinanciera, UUID> {

    Optional<CuentaFinanciera> findByCbu(String cbu);

    Optional<CuentaFinanciera> findByAlias(String alias);
}
