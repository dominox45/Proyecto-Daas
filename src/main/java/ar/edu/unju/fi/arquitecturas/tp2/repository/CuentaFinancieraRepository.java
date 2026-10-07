package ar.edu.unju.fi.arquitecturas.tp2.repository;

import ar.edu.unju.fi.arquitecturas.tp2.model.CuentaFinanciera;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CuentaFinancieraRepository extends JpaRepository<CuentaFinanciera, UUID> {

    Optional<CuentaFinanciera> findByCbu(String cbu);

    Optional<CuentaFinanciera> findByAlias(String alias);

    /**
     * Recupera todas las cuentas financieras que se encuentran en un estado específico.
     * @param estado Estado a filtrar (ej. ACTIVA)
     * @return Lista de cuentas coincidentes
     */
    java.util.List<CuentaFinanciera> findAllByEstado(ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoCuenta estado);
}
