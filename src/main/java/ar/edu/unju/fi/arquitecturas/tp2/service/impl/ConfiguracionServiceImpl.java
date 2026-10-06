package ar.edu.unju.fi.arquitecturas.tp2.service.impl;

import ar.edu.unju.fi.arquitecturas.tp2.exception.RecursoNoEncontradoException;
import ar.edu.unju.fi.arquitecturas.tp2.model.ConfiguracionGeneral;
import ar.edu.unju.fi.arquitecturas.tp2.repository.ConfiguracionGeneralRepository;
import ar.edu.unju.fi.arquitecturas.tp2.service.ConfiguracionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional(readOnly = true)
public class ConfiguracionServiceImpl implements ConfiguracionService {

    private final ConfiguracionGeneralRepository configuracionRepository;

    public ConfiguracionServiceImpl(ConfiguracionGeneralRepository configuracionRepository) {
        this.configuracionRepository = configuracionRepository;
    }

    @Override
    public String obtenerValorTexto(String clave) {
        return buscarConfiguracion(clave).getValor();
    }

    @Override
    public BigDecimal obtenerValorDecimal(String clave) {
        ConfiguracionGeneral config = buscarConfiguracion(clave);
        try {
            return new BigDecimal(config.getValor());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("El valor para la clave " + clave + " no es un decimal válido.");
        }
    }

    @Override
    public int obtenerValorEntero(String clave) {
        ConfiguracionGeneral config = buscarConfiguracion(clave);
        try {
            return Integer.parseInt(config.getValor());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("El valor para la clave " + clave + " no es un entero válido.");
        }
    }

    private ConfiguracionGeneral buscarConfiguracion(String clave) {
        return configuracionRepository.findByClave(clave)
                .orElseThrow(() -> new RecursoNoEncontradoException("Configuración no encontrada para la clave: " + clave));
    }
}