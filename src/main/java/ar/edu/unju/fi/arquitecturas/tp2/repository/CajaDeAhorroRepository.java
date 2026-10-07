package ar.edu.unju.fi.arquitecturas.tp2.repository;

import ar.edu.unju.fi.arquitecturas.tp2.model.CajaDeAhorro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.UUID;

public interface CajaDeAhorroRepository extends JpaRepository<CajaDeAhorro, UUID> {


    long countByLimiteExtraccionesMensualesSinCostoGreaterThan(int limiteExtraccionesMensualesSinCosto);

    long countByTasaInteresAnualGreaterThan(BigDecimal tasaInteresAnual);
}