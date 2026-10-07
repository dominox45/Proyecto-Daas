package ar.edu.unju.fi.arquitecturas.tp2.service;

import ar.edu.unju.fi.arquitecturas.tp2.exception.RecursoNoEncontradoException;
import ar.edu.unju.fi.arquitecturas.tp2.model.ConfiguracionGeneral;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.TipoDatoConfiguracion;
import ar.edu.unju.fi.arquitecturas.tp2.repository.ConfiguracionGeneralRepository;
import ar.edu.unju.fi.arquitecturas.tp2.service.impl.ConfiguracionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para {@link ConfiguracionServiceImpl}.
 *
 * <p>
 * Verifica la recuperación y conversión de los parámetros dinámicos
 * almacenados en la configuración general del sistema.
 * </p>
 *
 * <p>
 * El repositorio se simula con Mockito para probar únicamente
 * el comportamiento de la capa de servicios sin depender
 * de una base de datos real.
 * </p>
 *
 * @author MaxDz
 * @version 1.0.0
 * @see ConfiguracionServiceImpl
 * @see ConfiguracionGeneralRepository
 */
@ExtendWith(MockitoExtension.class)
class ConfiguracionServiceTest {

    /**
     * Repositorio simulado utilizado para recuperar
     * los parámetros de configuración.
     */
    @Mock
    private ConfiguracionGeneralRepository configuracionRepository;

    /**
     * Servicio bajo prueba con el repositorio simulado
     * inyectado automáticamente por Mockito.
     */
    @InjectMocks
    private ConfiguracionServiceImpl configuracionService;

    /**
     * Verifica que un parámetro pueda recuperarse correctamente
     * como texto.
     */
    @Test
    void deberiaObtenerValorTexto() {
        String clave = "CRON_PRUEBA";

        ConfiguracionGeneral configuracion = new ConfiguracionGeneral();
        configuracion.setClave(clave);
        configuracion.setValor("0 0 0 1 * ?");
        configuracion.setTipoDato(TipoDatoConfiguracion.TEXTO);

        when(configuracionRepository.findByClave(clave))
                .thenReturn(Optional.of(configuracion));

        String resultado =
                configuracionService.obtenerValorTexto(clave);

        assertEquals("0 0 0 1 * ?", resultado);

        verify(configuracionRepository)
                .findByClave(clave);
    }

    /**
     * Verifica que un parámetro decimal pueda convertirse
     * correctamente a {@link BigDecimal}.
     */
    @Test
    void deberiaObtenerValorDecimal() {
        String clave = "LIMITE_PRUEBA";

        ConfiguracionGeneral configuracion = new ConfiguracionGeneral();
        configuracion.setClave(clave);
        configuracion.setValor("100000.00");
        configuracion.setTipoDato(TipoDatoConfiguracion.DECIMAL);

        when(configuracionRepository.findByClave(clave))
                .thenReturn(Optional.of(configuracion));

        BigDecimal resultado =
                configuracionService.obtenerValorDecimal(clave);

        assertEquals(
                new BigDecimal("100000.00"),
                resultado
        );

        verify(configuracionRepository)
                .findByClave(clave);
    }

    /**
     * Verifica que un parámetro entero pueda convertirse
     * correctamente al tipo int.
     */
    @Test
    void deberiaObtenerValorEntero() {
        String clave = "CANTIDAD_PRUEBA";

        ConfiguracionGeneral configuracion = new ConfiguracionGeneral();
        configuracion.setClave(clave);
        configuracion.setValor("10");
        configuracion.setTipoDato(TipoDatoConfiguracion.ENTERO);

        when(configuracionRepository.findByClave(clave))
                .thenReturn(Optional.of(configuracion));

        int resultado =
                configuracionService.obtenerValorEntero(clave);

        assertEquals(10, resultado);

        verify(configuracionRepository)
                .findByClave(clave);
    }

    /**
     * Verifica que se produzca una excepción cuando
     * la clave solicitada no existe en la configuración.
     */
    @Test
    void deberiaRechazarConfiguracionInexistente() {
        String clave = "CONFIGURACION_INEXISTENTE";

        when(configuracionRepository.findByClave(clave))
                .thenReturn(Optional.empty());

        RecursoNoEncontradoException excepcion =
                assertThrows(
                        RecursoNoEncontradoException.class,
                        () -> configuracionService
                                .obtenerValorTexto(clave)
                );

        assertEquals(
                "Configuración no encontrada para la clave: " + clave,
                excepcion.getMessage()
        );

        verify(configuracionRepository)
                .findByClave(clave);
    }

    /**
     * Verifica que un valor no numérico sea rechazado
     * cuando se intenta obtener como decimal.
     */
    @Test
    void deberiaRechazarValorDecimalInvalido() {
        String clave = "DECIMAL_INVALIDO";

        ConfiguracionGeneral configuracion = new ConfiguracionGeneral();
        configuracion.setClave(clave);
        configuracion.setValor("valor-invalido");
        configuracion.setTipoDato(TipoDatoConfiguracion.DECIMAL);

        when(configuracionRepository.findByClave(clave))
                .thenReturn(Optional.of(configuracion));

        IllegalStateException excepcion =
                assertThrows(
                        IllegalStateException.class,
                        () -> configuracionService
                                .obtenerValorDecimal(clave)
                );

        assertEquals(
                "El valor para la clave "
                        + clave
                        + " no es un decimal válido.",
                excepcion.getMessage()
        );

        verify(configuracionRepository)
                .findByClave(clave);
    }

    /**
     * Verifica que un valor no numérico sea rechazado
     * cuando se intenta obtener como entero.
     */
    @Test
    void deberiaRechazarValorEnteroInvalido() {
        String clave = "ENTERO_INVALIDO";

        ConfiguracionGeneral configuracion = new ConfiguracionGeneral();
        configuracion.setClave(clave);
        configuracion.setValor("valor-invalido");
        configuracion.setTipoDato(TipoDatoConfiguracion.ENTERO);

        when(configuracionRepository.findByClave(clave))
                .thenReturn(Optional.of(configuracion));

        IllegalStateException excepcion =
                assertThrows(
                        IllegalStateException.class,
                        () -> configuracionService
                                .obtenerValorEntero(clave)
                );

        assertEquals(
                "El valor para la clave "
                        + clave
                        + " no es un entero válido.",
                excepcion.getMessage()
        );

        verify(configuracionRepository)
                .findByClave(clave);
    }
}