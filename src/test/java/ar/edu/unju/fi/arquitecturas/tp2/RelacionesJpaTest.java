package ar.edu.unju.fi.arquitecturas.tp2;

import ar.edu.unju.fi.arquitecturas.tp2.model.CajaDeAhorro;
import ar.edu.unju.fi.arquitecturas.tp2.model.Cliente;
import ar.edu.unju.fi.arquitecturas.tp2.model.CuentaCorriente;
import ar.edu.unju.fi.arquitecturas.tp2.model.CuentaFinanciera;
import ar.edu.unju.fi.arquitecturas.tp2.model.Transaccion;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoCliente;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoCuenta;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoTransaccion;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.TipoTransaccion;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

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
        assertEquals(
                transaccionId,
                cuentaRecuperada.getTransacciones().getFirst().getId()
        );
        assertNull(transaccionRecuperada.getOperador());
    }

    @Test
    @Transactional
    void deberiaPersistirRelacionReflexivaEntreClientes() {
        Cliente juan = new Cliente();
        juan.setNombre("Juan");
        juan.setCuil("20333333331");

        Cliente elena = new Cliente();
        elena.setNombre("Elena");
        elena.setCuil("27333333332");
        elena.setTitular(juan);

        Cliente pedro = new Cliente();
        pedro.setNombre("Pedro");
        pedro.setCuil("20333333333");
        pedro.setTitular(juan);

        // Persistimos cada cliente explícitamente: no configuramos cascadas.
        entityManager.persist(juan);
        entityManager.persist(elena);
        entityManager.persist(pedro);
        entityManager.flush();

        UUID juanId = juan.getId();
        UUID elenaId = elena.getId();
        UUID pedroId = pedro.getId();

        // Limpiamos el contexto para verificar lo recuperado desde la BD.
        entityManager.clear();

        Cliente juanRecuperado = entityManager.find(Cliente.class, juanId);
        Cliente elenaRecuperada = entityManager.find(Cliente.class, elenaId);
        Cliente pedroRecuperado = entityManager.find(Cliente.class, pedroId);

        assertNotNull(juanRecuperado);
        assertNotNull(elenaRecuperada);
        assertNotNull(pedroRecuperado);

        assertNull(juanRecuperado.getTitular());
        assertNotNull(elenaRecuperada.getTitular());
        assertNotNull(pedroRecuperado.getTitular());
        assertEquals(juanId, elenaRecuperada.getTitular().getId());
        assertEquals(juanId, pedroRecuperado.getTitular().getId());
    }

    @Test
    @Transactional
    void deberiaPersistirDatosDeActivacionDelCliente() {
        LocalDateTime vencimiento = LocalDateTime.now()
                .plusHours(24)
                .truncatedTo(ChronoUnit.MICROS);

        Cliente cliente = new Cliente();
        cliente.setNombre("Cliente Activacion");
        cliente.setCuil("20444444441");
        cliente.setTokenActivacion("550e8400-e29b-41d4-a716-446655440000");
        cliente.setTokenActivacionExpiraEn(vencimiento);

        entityManager.persist(cliente);
        entityManager.flush();

        UUID clienteId = cliente.getId();

        entityManager.clear();

        Cliente clientePendiente = entityManager.find(Cliente.class, clienteId);

        assertNotNull(clientePendiente);
        assertEquals(
                EstadoCliente.PENDIENTE_ACTIVACION,
                clientePendiente.getEstado()
        );
        assertEquals(
                "550e8400-e29b-41d4-a716-446655440000",
                clientePendiente.getTokenActivacion()
        );
        assertEquals(vencimiento, clientePendiente.getTokenActivacionExpiraEn());
        assertNull(clientePendiente.getFechaActivacion());

        LocalDateTime fechaActivacion = LocalDateTime.now()
                .truncatedTo(ChronoUnit.MICROS);

        clientePendiente.setEstado(EstadoCliente.ACTIVO);
        clientePendiente.setFechaActivacion(fechaActivacion);

        entityManager.flush();
        entityManager.clear();

        Cliente clienteActivo = entityManager.find(Cliente.class, clienteId);

        assertEquals(EstadoCliente.ACTIVO, clienteActivo.getEstado());
        assertEquals(fechaActivacion, clienteActivo.getFechaActivacion());
    }

    @Test
    @Transactional
    void deberiaPersistirOperadorDeTransaccion() {
        Cliente operador = new Cliente();
        operador.setNombre("Cliente Operador");
        operador.setCuil("20555555551");

        CuentaCorriente cuenta = new CuentaCorriente();
        cuenta.setCbu("2222333344445555666677");
        cuenta.setAlias("PRUEBA.OPERADOR.01");
        cuenta.setSaldoOperativo(new BigDecimal("100000.00"));
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setDescubiertoAutorizado(BigDecimal.ZERO);
        cuenta.setCostoComisionMantenimientoMensual(new BigDecimal("100.00"));

        entityManager.persist(operador);
        entityManager.persist(cuenta);

        Transaccion transaccion = new Transaccion();
        transaccion.setFechaHora(LocalDateTime.now());
        transaccion.setMonto(new BigDecimal("10000.00"));
        transaccion.setTipo(TipoTransaccion.EXTRACCION);
        transaccion.setEstadoTransaccion(EstadoTransaccion.COMPLETADA);
        transaccion.setCuenta(cuenta);
        transaccion.setOperador(operador);

        entityManager.persist(transaccion);
        entityManager.flush();

        UUID transaccionId = transaccion.getId();
        UUID operadorId = operador.getId();

        entityManager.clear();

        Transaccion transaccionRecuperada =
                entityManager.find(Transaccion.class, transaccionId);

        assertNotNull(transaccionRecuperada);
        assertNotNull(transaccionRecuperada.getOperador());
        assertEquals(
                operadorId,
                transaccionRecuperada.getOperador().getId()
        );
    }

    @Test
    @Transactional
    void deberiaPersistirDebitoComisionSinOperador() {
        CuentaCorriente cuenta = new CuentaCorriente();
        cuenta.setCbu("3333444455556666777788");
        cuenta.setAlias("PRUEBA.COMISION.01");
        cuenta.setSaldoOperativo(new BigDecimal("50000.00"));
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setDescubiertoAutorizado(BigDecimal.ZERO);
        cuenta.setCostoComisionMantenimientoMensual(new BigDecimal("100.00"));

        entityManager.persist(cuenta);

        Transaccion transaccion = new Transaccion();
        transaccion.setFechaHora(LocalDateTime.now());
        transaccion.setMonto(new BigDecimal("5000.00"));
        transaccion.setTipo(TipoTransaccion.DEBITO_COMISION);
        transaccion.setEstadoTransaccion(EstadoTransaccion.COMPLETADA);
        transaccion.setCuenta(cuenta);

        entityManager.persist(transaccion);
        entityManager.flush();

        UUID transaccionId = transaccion.getId();

        entityManager.clear();

        Transaccion transaccionRecuperada =
                entityManager.find(Transaccion.class, transaccionId);

        assertNotNull(transaccionRecuperada);
        assertEquals(
                TipoTransaccion.DEBITO_COMISION,
                transaccionRecuperada.getTipo()
        );
        assertNull(transaccionRecuperada.getOperador());
    }

    @Test
    @Transactional
    void deberiaPersistirCuentaAutorizadaParaAdherente() {
        Cliente titular = new Cliente();
        titular.setNombre("Titular Autorizacion");
        titular.setCuil("20666666661");

        Cliente adherente = new Cliente();
        adherente.setNombre("Adherente Autorizacion");
        adherente.setCuil("27666666662");
        adherente.setTitular(titular);

        CajaDeAhorro cuenta = new CajaDeAhorro();
        cuenta.setCbu("4444555566667777888899");
        cuenta.setAlias("PRUEBA.ADHERENTE.01");
        cuenta.setSaldoOperativo(new BigDecimal("10000.00"));
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setTasaInteresAnual(new BigDecimal("0.050000"));
        cuenta.setLimiteExtraccionesMensualesSinCosto(5);

        /*
         * Persistimos explícitamente porque las relaciones
         * no dependen de cascadas para crear las entidades.
         */
        entityManager.persist(titular);
        entityManager.persist(adherente);
        entityManager.persist(cuenta);

        /*
         * La cuenta pertenece al titular.
         */
        titular.agregarCuenta(cuenta);

        /*
         * El adherente recibe autorización explícita
         * únicamente para operar sobre esta cuenta.
         */
        adherente.autorizarCuenta(cuenta);

        entityManager.flush();

        UUID titularId = titular.getId();
        UUID adherenteId = adherente.getId();
        UUID cuentaId = cuenta.getId();

        /*
         * Limpiamos el contexto para asegurarnos de que
         * los datos siguientes se recuperen realmente desde la BD.
         */
        entityManager.clear();

        Cliente adherenteRecuperado =
                entityManager.find(
                        Cliente.class,
                        adherenteId
                );

        CuentaFinanciera cuentaRecuperada =
                entityManager.find(
                        CuentaFinanciera.class,
                        cuentaId
                );

        assertNotNull(adherenteRecuperado);
        assertNotNull(cuentaRecuperada);

        /*
         * La relación familiar debe mantenerse.
         */
        assertNotNull(
                adherenteRecuperado.getTitular()
        );

        assertEquals(
                titularId,
                adherenteRecuperado
                        .getTitular()
                        .getId()
        );

        /*
         * La autorización por cuenta debe persistirse.
         */
        assertEquals(
                1,
                adherenteRecuperado
                        .getCuentasAutorizadas()
                        .size()
        );

        assertTrue(
                adherenteRecuperado
                        .getCuentasAutorizadas()
                        .stream()
                        .anyMatch(cuentaAutorizada ->
                                cuentaAutorizada
                                        .getId()
                                        .equals(cuentaId)
                        )
        );

        /*
         * Autorizar una cuenta no convierte al adherente
         * en titular o propietario de ella.
         */
        assertTrue(
                adherenteRecuperado
                        .getCuentas()
                        .isEmpty()
        );

        assertTrue(
                cuentaRecuperada
                        .getTitulares()
                        .stream()
                        .anyMatch(cliente ->
                                cliente.getId()
                                        .equals(titularId)
                        )
        );

        assertTrue(
                cuentaRecuperada
                        .getTitulares()
                        .stream()
                        .noneMatch(cliente ->
                                cliente.getId()
                                        .equals(adherenteId)
                        )
        );
    }
}