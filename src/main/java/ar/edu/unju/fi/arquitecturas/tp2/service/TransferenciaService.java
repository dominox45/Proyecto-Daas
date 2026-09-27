package ar.edu.unju.fi.arquitecturas.tp2.service;

import java.math.BigDecimal;
import java.util.UUID;

public interface TransferenciaService {

    void transferir(UUID origenId, UUID destinoId, BigDecimal monto);
}