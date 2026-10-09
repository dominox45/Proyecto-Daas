package ar.edu.unju.fi.arquitecturas.tp2.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifica el binding de las propiedades globales utilizadas
 * para la liquidación mensual de comisiones.
 */
class ComisionesPropertiesTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withUserConfiguration(TestConfiguration.class)
                    .withPropertyValues(
                            "app.comisiones.cuenta-corriente=5000.00",
                            "app.comisiones.caja-ahorro=2000.00"
                    );

    /**
     * Comprueba que Spring vincule correctamente los valores
     * definidos bajo el prefijo app.comisiones.
     */
    @Test
    void deberiaCargarMontosDeComisiones() {
        contextRunner.run(context -> {
            ComisionesProperties properties =
                    context.getBean(ComisionesProperties.class);

            assertEquals(
                    new BigDecimal("5000.00"),
                    properties.getCuentaCorriente()
            );

            assertEquals(
                    new BigDecimal("2000.00"),
                    properties.getCajaAhorro()
            );
        });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(ComisionesProperties.class)
    static class TestConfiguration {
    }
}
