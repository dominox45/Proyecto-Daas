package ar.edu.unju.fi.arquitecturas.tp2.repository;

import ar.edu.unju.fi.arquitecturas.tp2.model.ConfiguracionGeneral;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para la persistencia y recuperación de parámetros de configuración.
 */
public interface ConfiguracionGeneralRepository extends JpaRepository<ConfiguracionGeneral, UUID> {

    /**
     * Recupera un parámetro de configuración buscando por su clave única.
     *
     * @param clave Clave del parámetro (ej. "LIMITE_EXTRACCION_TITULAR")
     * @return Optional con la configuración si existe
     */
    Optional<ConfiguracionGeneral> findByClave(String clave);
}