package ar.edu.unju.fi.arquitecturas.tp2;

import ar.edu.unju.fi.arquitecturas.tp2.model.CuentaCorriente;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoCuenta;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuditoriaTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @Transactional
    void deberiaRegistrarFechasDeAuditoria() {

        CuentaCorriente cuenta = new CuentaCorriente();

        cuenta.setCbu("1234567890123456789012");
        cuenta.setAlias("PRUEBA.AUDITORIA.01");
        cuenta.setSaldoOperativo(BigDecimal.ZERO);
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setDescubiertoAutorizado(BigDecimal.ZERO);
        cuenta.setCostoComisionMantenimientoMensual(BigDecimal.ZERO);

        entityManager.persist(cuenta);
        entityManager.flush();

        assertNotNull(cuenta.getFechaCreacion());
        assertNotNull(cuenta.getFechaModificacion());

        entityManager.clear();

        CuentaCorriente cuentaPersistida =
                entityManager.find(CuentaCorriente.class, cuenta.getId());

        assertNotNull(cuentaPersistida);

        LocalDateTime fechaCreacion = cuentaPersistida.getFechaCreacion();
        LocalDateTime fechaModificacionInicial =
                cuentaPersistida.getFechaModificacion();

        cuentaPersistida.setSaldoOperativo(new BigDecimal("1000.00"));
        entityManager.flush();
        entityManager.clear();

        CuentaCorriente cuentaRecuperada =
                entityManager.find(CuentaCorriente.class, cuenta.getId());

        assertNotNull(cuentaRecuperada);
        assertEquals(fechaCreacion, cuentaRecuperada.getFechaCreacion());
        assertEquals(0, new BigDecimal("1000.00")
                .compareTo(cuentaRecuperada.getSaldoOperativo()));
        assertTrue(cuentaRecuperada.getFechaModificacion()
                .isAfter(fechaModificacionInicial));
    }
}