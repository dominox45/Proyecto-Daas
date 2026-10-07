package ar.edu.unju.fi.arquitecturas.tp2.util;

import ar.edu.unju.fi.arquitecturas.tp2.model.ConfiguracionGeneral;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.TipoDatoConfiguracion;
import ar.edu.unju.fi.arquitecturas.tp2.repository.ConfiguracionGeneralRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para {@link ConfiguracionInicializador}.
 *
 * <p>
 * Verifica la carga inicial de los parámetros globales utilizados
 * por el Trabajo Práctico 5 y garantiza que los valores existentes
 * en la base de datos no sean sobrescritos al reiniciar la aplicación.
 * </p>
 *
 * <p>
 * El repositorio se simula con Mockito para probar el comportamiento
 * del inicializador sin depender de una base de datos real.
 * </p>
 *
 * @author MaxDz
 * @version 1.0.0
 * @see ConfiguracionInicializador
 * @see ConfiguracionGeneralRepository
 */
@ExtendWith(MockitoExtension.class)
class ConfiguracionInicializadorTest {

    /**
     * Repositorio simulado utilizado por el inicializador.
     */
    @Mock
    private ConfiguracionGeneralRepository configuracionRepository;

    /**
     * Verifica que, cuando los parámetros todavía no existen,
     * el inicializador cree los tres valores requeridos por el TP5.
     */
    @Test
    void deberiaCrearConfiguracionesInicialesCuandoNoExisten() {
        when(configuracionRepository.findByClave(
                ClavesConfiguracion.LIMITE_EXTRACCION_TITULAR
        )).thenReturn(Optional.empty());

        when(configuracionRepository.findByClave(
                ClavesConfiguracion.LIMITE_EXTRACCION_ADHERENTE
        )).thenReturn(Optional.empty());

        when(configuracionRepository.findByClave(
                ClavesConfiguracion.CRON_LIQUIDACION_COMISIONES
        )).thenReturn(Optional.empty());

        ConfiguracionInicializador inicializador =
                new ConfiguracionInicializador(configuracionRepository);

        inicializador.run();

        ArgumentCaptor<ConfiguracionGeneral> captor =
                ArgumentCaptor.forClass(ConfiguracionGeneral.class);

        verify(configuracionRepository,
                org.mockito.Mockito.times(3))
                .save(captor.capture());

        List<ConfiguracionGeneral> configuraciones =
                captor.getAllValues();

        assertEquals(3, configuraciones.size());

        ConfiguracionGeneral limiteTitular =
                buscarPorClave(
                        configuraciones,
                        ClavesConfiguracion.LIMITE_EXTRACCION_TITULAR
                );

        assertEquals("100000.00", limiteTitular.getValor());
        assertEquals(
                TipoDatoConfiguracion.DECIMAL,
                limiteTitular.getTipoDato()
        );

        ConfiguracionGeneral limiteAdherente =
                buscarPorClave(
                        configuraciones,
                        ClavesConfiguracion.LIMITE_EXTRACCION_ADHERENTE
                );

        assertEquals("70000.00", limiteAdherente.getValor());
        assertEquals(
                TipoDatoConfiguracion.DECIMAL,
                limiteAdherente.getTipoDato()
        );

        ConfiguracionGeneral cron =
                buscarPorClave(
                        configuraciones,
                        ClavesConfiguracion.CRON_LIQUIDACION_COMISIONES
                );

        assertEquals("0 0 0 1 * ?", cron.getValor());
        assertEquals(
                TipoDatoConfiguracion.TEXTO,
                cron.getTipoDato()
        );
    }

    /**
     * Verifica que los parámetros existentes no sean sobrescritos
     * cuando la aplicación vuelva a ejecutar el inicializador.
     *
     * <p>
     * Esto permite que un administrador pueda modificar los valores
     * dinámicamente sin que sean restaurados a sus valores por defecto
     * en cada reinicio.
     * </p>
     */
    @Test
    void deberiaConservarConfiguracionesExistentes() {
        ConfiguracionGeneral limiteTitular =
                crearConfiguracionExistente(
                        ClavesConfiguracion.LIMITE_EXTRACCION_TITULAR,
                        "150000.00",
                        TipoDatoConfiguracion.DECIMAL
                );

        ConfiguracionGeneral limiteAdherente =
                crearConfiguracionExistente(
                        ClavesConfiguracion.LIMITE_EXTRACCION_ADHERENTE,
                        "90000.00",
                        TipoDatoConfiguracion.DECIMAL
                );

        ConfiguracionGeneral cron =
                crearConfiguracionExistente(
                        ClavesConfiguracion.CRON_LIQUIDACION_COMISIONES,
                        "0 0 3 1 * ?",
                        TipoDatoConfiguracion.TEXTO
                );

        when(configuracionRepository.findByClave(
                ClavesConfiguracion.LIMITE_EXTRACCION_TITULAR
        )).thenReturn(Optional.of(limiteTitular));

        when(configuracionRepository.findByClave(
                ClavesConfiguracion.LIMITE_EXTRACCION_ADHERENTE
        )).thenReturn(Optional.of(limiteAdherente));

        when(configuracionRepository.findByClave(
                ClavesConfiguracion.CRON_LIQUIDACION_COMISIONES
        )).thenReturn(Optional.of(cron));

        ConfiguracionInicializador inicializador =
                new ConfiguracionInicializador(configuracionRepository);

        inicializador.run();

        verify(configuracionRepository, never())
                .save(org.mockito.ArgumentMatchers.any(
                        ConfiguracionGeneral.class
                ));

        assertEquals("150000.00", limiteTitular.getValor());
        assertEquals("90000.00", limiteAdherente.getValor());
        assertEquals("0 0 3 1 * ?", cron.getValor());
    }

    /**
     * Busca una configuración dentro de la colección capturada
     * utilizando su clave única.
     *
     * @param configuraciones configuraciones creadas por el inicializador
     * @param clave clave que se desea localizar
     * @return configuración asociada a la clave
     */
    private ConfiguracionGeneral buscarPorClave(
            List<ConfiguracionGeneral> configuraciones,
            String clave) {

        Optional<ConfiguracionGeneral> resultado =
                configuraciones.stream()
                        .filter(configuracion ->
                                clave.equals(configuracion.getClave()))
                        .findFirst();

        assertTrue(resultado.isPresent());

        return resultado.orElseThrow();
    }

    /**
     * Construye una configuración existente para utilizarla
     * en los escenarios de prueba.
     *
     * @param clave identificador del parámetro
     * @param valor valor almacenado
     * @param tipoDato tipo de dato asociado
     * @return configuración preparada para la prueba
     */
    private ConfiguracionGeneral crearConfiguracionExistente(
            String clave,
            String valor,
            TipoDatoConfiguracion tipoDato) {

        ConfiguracionGeneral configuracion =
                new ConfiguracionGeneral();

        configuracion.setClave(clave);
        configuracion.setValor(valor);
        configuracion.setTipoDato(tipoDato);

        return configuracion;
    }
}