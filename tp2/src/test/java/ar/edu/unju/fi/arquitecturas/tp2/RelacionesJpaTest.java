package ar.edu.unju.fi.arquitecturas.tp2;

import ar.edu.unju.fi.arquitecturas.tp2.model.CajaDeAhorro;
import ar.edu.unju.fi.arquitecturas.tp2.model.Cliente;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoCuenta;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unju.fi.arquitecturas.tp2.model.CuentaFinanciera;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import ar.edu.unju.fi.arquitecturas.tp2.model.CuentaCorriente;
import ar.edu.unju.fi.arquitecturas.tp2.model.Transaccion;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoTransaccion;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.TipoTransaccion;
import java.time.LocalDateTime;

@SpringBootTest
class RelacionesJpaTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @Transactional
    void deberiaPersistirCotitularidad() {
        CajaDeAhorro cuenta = new CajaDeAhorro();
        cuenta.setCbu("9876543210987654321098");
        cuenta.setAlias("PRUEBA.COTITULARIDAD.01");
        cuenta.setSaldoOperativo(BigDecimal.ZERO);
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setTasaInteresAnual(new BigDecimal("0.125000"));
        cuenta.setLimiteExtraccionesMensualesSinCosto(5);

        Cliente primerCliente = new Cliente();
        primerCliente.setNombre("Cliente Uno");
        primerCliente.setCuil("20123456789");

        Cliente segundoCliente = new Cliente();
        segundoCliente.setNombre("Cliente Dos");
        segundoCliente.setCuil("20987654321");

        entityManager.persist(cuenta);
        entityManager.persist(primerCliente);
        entityManager.persist(segundoCliente);

        primerCliente.agregarCuenta(cuenta);
        segundoCliente.agregarCuenta(cuenta);

        entityManager.flush();

        UUID cuentaId = cuenta.getId();
        UUID primerClienteId = primerCliente.getId();
        UUID segundoClienteId = segundoCliente.getId();

        entityManager.clear();

        CuentaFinanciera cuentaRecuperada =
                entityManager.find(CuentaFinanciera.class, cuentaId);
        Cliente primerClienteRecuperado =
                entityManager.find(Cliente.class, primerClienteId);
        Cliente segundoClienteRecuperado =
                entityManager.find(Cliente.class, segundoClienteId);

        assertNotNull(cuentaRecuperada);
        assertEquals(2, cuentaRecuperada.getTitulares().size());
        assertTrue(primerClienteRecuperado.getCuentas().contains(cuentaRecuperada));
        assertTrue(segundoClienteRecuperado.getCuentas().contains(cuentaRecuperada));

        primerClienteRecuperado.quitarCuenta(cuentaRecuperada);

        entityManager.flush();
        entityManager.clear();

        Cliente clienteDesvinculado =
                entityManager.find(Cliente.class, primerClienteId);
        CuentaFinanciera cuentaActualizada =
                entityManager.find(CuentaFinanciera.class, cuentaId);

        assertTrue(clienteDesvinculado.getCuentas().isEmpty());
        assertEquals(1, cuentaActualizada.getTitulares().size());
        assertEquals(
                segundoClienteId,
                cuentaActualizada.getTitulares().iterator().next().getId()
        );
    }

    @Test
    @Transactional
    void deberiaPersistirTransaccionAsociadaACuenta() {
        CuentaCorriente cuenta = new CuentaCorriente();
        cuenta.setCbu("1111222233334444555566");
        cuenta.setAlias("PRUEBA.TRANSACCION.01");
        cuenta.setSaldoOperativo(new BigDecimal("1500.00"));
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setDescubiertoAutorizado(new BigDecimal("500.00"));
        cuenta.setCostoComisionMantenimientoMensual(new BigDecimal("100.00"));

        entityManager.persist(cuenta);

        Transaccion transaccion = new Transaccion();
        transaccion.setFechaHora(LocalDateTime.now());
        transaccion.setMonto(new BigDecimal("500.00"));
        transaccion.setTipo(TipoTransaccion.DEPOSITO);
        transaccion.setEstadoTransaccion(EstadoTransaccion.COMPLETADA);
        transaccion.setCuenta(cuenta);

        entityManager.persist(transaccion);
        entityManager.flush();

        UUID cuentaId = cuenta.getId();
        UUID transaccionId = transaccion.getId();

        entityManager.clear();

        CuentaFinanciera cuentaRecuperada =
                entityManager.find(CuentaFinanciera.class, cuentaId);
        Transaccion transaccionRecuperada =
                entityManager.find(Transaccion.class, transaccionId);

        assertNotNull(cuentaRecuperada);
        assertNotNull(transaccionRecuperada);
        assertEquals(cuentaId, transaccionRecuperada.getCuenta().getId());
        assertEquals(1, cuentaRecuperada.getTransacciones().size());
        assertEquals(transaccionId,
                cuentaRecuperada.getTransacciones().getFirst().getId());
    }
}