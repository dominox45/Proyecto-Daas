package ar.edu.unju.fi.arquitecturas.tp2.service;

import ar.edu.unju.fi.arquitecturas.tp2.dto.ClienteRequestDto;
import ar.edu.unju.fi.arquitecturas.tp2.dto.ClienteResponseDto;
import ar.edu.unju.fi.arquitecturas.tp2.exception.RecursoNoEncontradoException;
import ar.edu.unju.fi.arquitecturas.tp2.model.Cliente;
import ar.edu.unju.fi.arquitecturas.tp2.model.CuentaCorriente;
import ar.edu.unju.fi.arquitecturas.tp2.repository.ClienteRepository;
import ar.edu.unju.fi.arquitecturas.tp2.repository.CuentaFinancieraRepository;
import ar.edu.unju.fi.arquitecturas.tp2.service.impl.ClienteServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para {@link ClienteServiceImpl}.
 *
 * <p>
 * Verifica las operaciones de registro y búsqueda de clientes,
 * la gestión del grupo familiar y las autorizaciones explícitas
 * de cuentas para clientes adherentes.
 * </p>
 *
 * <p>
 * Los repositorios son simulados con Mockito para probar únicamente
 * el comportamiento de la capa de servicios sin depender de una
 * base de datos real.
 * </p>
 *
 * @author MaxDz
 * @version 1.3.0
 * @see ClienteServiceImpl
 * @see ClienteRepository
 * @see CuentaFinancieraRepository
 */
@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    /**
     * Repositorio simulado utilizado para las operaciones
     * relacionadas con clientes.
     */
    @Mock
    private ClienteRepository clienteRepository;

    /**
     * Repositorio simulado utilizado para recuperar las cuentas
     * financieras que serán autorizadas a los adherentes.
     */
    @Mock
    private CuentaFinancieraRepository cuentaRepository;

    /**
     * Publicador simulado para los eventos generados
     * durante el alta de clientes.
     */
    @Mock
    private ApplicationEventPublisher eventPublisher;

    /**
     * Servicio bajo prueba con sus dependencias simuladas.
     */
    @InjectMocks
    private ClienteServiceImpl clienteService;

    /**
     * Verifica que un cliente pueda buscarse correctamente mediante su CUIL.
     */
    @Test
    void deberiaBuscarClientePorCuil() {
        String cuil = "20304050607";

        Cliente cliente = new Cliente();
        cliente.setCuil(cuil);

        when(clienteRepository.findByCuil(cuil))
                .thenReturn(Optional.of(cliente));

        Optional<Cliente> resultado =
                clienteService.buscarPorCuil(cuil);

        assertSame(cliente, resultado.orElseThrow());
        verify(clienteRepository).findByCuil(cuil);
    }

    /**
     * Verifica que la búsqueda retorne vacío cuando no existe
     * un cliente con el CUIL solicitado.
     */
    @Test
    void deberiaDevolverVacioCuandoNoExisteCliente() {
        String cuil = "20999999999";

        when(clienteRepository.findByCuil(cuil))
                .thenReturn(Optional.empty());

        Optional<Cliente> resultado =
                clienteService.buscarPorCuil(cuil);

        assertTrue(resultado.isEmpty());
        verify(clienteRepository).findByCuil(cuil);
    }

    /**
     * Verifica la persistencia directa de un cliente.
     */
    @Test
    void deberiaGuardarCliente() {
        Cliente cliente = new Cliente();
        cliente.setNombre("Juan Pérez");
        cliente.setCuil("20304050607");

        when(clienteRepository.save(cliente))
                .thenReturn(cliente);

        Cliente resultado =
                clienteService.guardar(cliente);

        assertSame(cliente, resultado);
        verify(clienteRepository).save(cliente);
    }

    /**
     * Verifica que un cliente válido pueda registrarse correctamente.
     */
    @Test
    void deberiaCrearCliente() {
        UUID id = UUID.randomUUID();

        ClienteRequestDto request = new ClienteRequestDto(
                "Ana López",
                "27304050608",
                "ana.lopez@email.com",
                "3884000000",
                "San Salvador de Jujuy"
        );

        Cliente clienteGuardado = Cliente.builder()
                .id(id)
                .nombre(request.getNombre())
                .cuil(request.getCuil())
                .email(request.getEmail())
                .telefono(request.getTelefono())
                .direccion(request.getDireccion())
                .build();

        when(clienteRepository.findByCuil(request.getCuil()))
                .thenReturn(Optional.empty());

        when(clienteRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());

        when(clienteRepository.save(any(Cliente.class)))
                .thenReturn(clienteGuardado);

        ClienteResponseDto resultado =
                clienteService.crear(request);

        assertEquals(id, resultado.getId());
        assertEquals(request.getNombre(), resultado.getNombre());
        assertEquals(request.getCuil(), resultado.getCuil());
        assertEquals(request.getEmail(), resultado.getEmail());
        assertEquals(request.getTelefono(), resultado.getTelefono());
        assertEquals(request.getDireccion(), resultado.getDireccion());

        verify(clienteRepository)
                .findByCuil(request.getCuil());

        verify(clienteRepository)
                .findByEmail(request.getEmail());

        verify(clienteRepository)
                .save(any(Cliente.class));
    }

    /**
     * Verifica que no pueda registrarse un CUIL duplicado.
     */
    @Test
    void deberiaRechazarClienteConCuilDuplicado() {
        ClienteRequestDto request = new ClienteRequestDto(
                "Ana López",
                "27304050608",
                "ana.lopez@email.com",
                "3884000000",
                "San Salvador de Jujuy"
        );

        Cliente existente = new Cliente();
        existente.setCuil(request.getCuil());

        when(clienteRepository.findByCuil(request.getCuil()))
                .thenReturn(Optional.of(existente));

        IllegalArgumentException excepcion =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> clienteService.crear(request)
                );

        assertEquals(
                "Ya existe un cliente con el CUIL indicado",
                excepcion.getMessage()
        );

        verify(clienteRepository, never())
                .save(any(Cliente.class));
    }

    /**
     * Verifica que no pueda registrarse un email duplicado.
     */
    @Test
    void deberiaRechazarClienteConEmailDuplicado() {
        ClienteRequestDto request = new ClienteRequestDto(
                "Ana López",
                "27304050608",
                "ana.lopez@email.com",
                "3884000000",
                "San Salvador de Jujuy"
        );

        Cliente existente = new Cliente();
        existente.setEmail(request.getEmail());

        when(clienteRepository.findByCuil(request.getCuil()))
                .thenReturn(Optional.empty());

        when(clienteRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(existente));

        IllegalArgumentException excepcion =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> clienteService.crear(request)
                );

        assertEquals(
                "Ya existe un cliente con el email indicado",
                excepcion.getMessage()
        );

        verify(clienteRepository, never())
                .save(any(Cliente.class));
    }

    /**
     * Verifica que un cliente pueda asociarse correctamente
     * como adherente de un titular.
     */
    @Test
    void deberiaAsociarAdherenteATitular() {
        UUID titularId = UUID.randomUUID();
        UUID adherenteId = UUID.randomUUID();

        Cliente titular = Cliente.builder()
                .id(titularId)
                .build();

        Cliente adherente = Cliente.builder()
                .id(adherenteId)
                .build();

        when(clienteRepository.findById(titularId))
                .thenReturn(Optional.of(titular));

        when(clienteRepository.findById(adherenteId))
                .thenReturn(Optional.of(adherente));

        clienteService.asociarAdherente(
                titularId,
                adherenteId
        );

        assertSame(
                titular,
                adherente.getTitular()
        );

        verify(clienteRepository)
                .save(adherente);
    }

    /**
     * Verifica el rechazo cuando el titular no existe.
     */
    @Test
    void deberiaRechazarAsociacionCuandoTitularNoExiste() {
        UUID titularId = UUID.randomUUID();
        UUID adherenteId = UUID.randomUUID();

        when(clienteRepository.findById(titularId))
                .thenReturn(Optional.empty());

        RecursoNoEncontradoException excepcion =
                assertThrows(
                        RecursoNoEncontradoException.class,
                        () -> clienteService.asociarAdherente(
                                titularId,
                                adherenteId
                        )
                );

        assertEquals(
                "El cliente titular no existe",
                excepcion.getMessage()
        );

        verify(clienteRepository, never())
                .findById(adherenteId);

        verify(clienteRepository, never())
                .save(any(Cliente.class));
    }

    /**
     * Verifica el rechazo cuando el adherente no existe.
     */
    @Test
    void deberiaRechazarAsociacionCuandoAdherenteNoExiste() {
        UUID titularId = UUID.randomUUID();
        UUID adherenteId = UUID.randomUUID();

        Cliente titular = Cliente.builder()
                .id(titularId)
                .build();

        when(clienteRepository.findById(titularId))
                .thenReturn(Optional.of(titular));

        when(clienteRepository.findById(adherenteId))
                .thenReturn(Optional.empty());

        RecursoNoEncontradoException excepcion =
                assertThrows(
                        RecursoNoEncontradoException.class,
                        () -> clienteService.asociarAdherente(
                                titularId,
                                adherenteId
                        )
                );

        assertEquals(
                "El cliente adherente no existe",
                excepcion.getMessage()
        );

        verify(clienteRepository, never())
                .save(any(Cliente.class));
    }

    /**
     * Verifica que un cliente no pueda ser adherente de sí mismo.
     */
    @Test
    void deberiaRechazarClienteComoSuPropioAdherente() {
        UUID clienteId = UUID.randomUUID();

        IllegalArgumentException excepcion =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> clienteService.asociarAdherente(
                                clienteId,
                                clienteId
                        )
                );

        assertEquals(
                "Un cliente no puede ser adherente de sí mismo",
                excepcion.getMessage()
        );

        verifyNoInteractions(clienteRepository);
    }

    /**
     * Verifica que un adherente no pueda actuar como titular.
     */
    @Test
    void deberiaRechazarAdherenteComoTitular() {
        UUID titularId = UUID.randomUUID();
        UUID adherenteId = UUID.randomUUID();

        Cliente titularDelSupuestoTitular =
                Cliente.builder()
                        .id(UUID.randomUUID())
                        .build();

        Cliente supuestoTitular =
                Cliente.builder()
                        .id(titularId)
                        .titular(titularDelSupuestoTitular)
                        .build();

        Cliente adherente =
                Cliente.builder()
                        .id(adherenteId)
                        .build();

        when(clienteRepository.findById(titularId))
                .thenReturn(Optional.of(supuestoTitular));

        when(clienteRepository.findById(adherenteId))
                .thenReturn(Optional.of(adherente));

        IllegalArgumentException excepcion =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> clienteService.asociarAdherente(
                                titularId,
                                adherenteId
                        )
                );

        assertEquals(
                "Un cliente adherente no puede actuar como titular",
                excepcion.getMessage()
        );

        verify(clienteRepository, never())
                .save(any(Cliente.class));
    }

    /**
     * Verifica que no pueda repetirse una relación familiar existente.
     */
    @Test
    void deberiaRechazarAdherenteYaAsociadoAlMismoTitular() {
        UUID titularId = UUID.randomUUID();
        UUID adherenteId = UUID.randomUUID();

        Cliente titular = Cliente.builder()
                .id(titularId)
                .build();

        Cliente adherente = Cliente.builder()
                .id(adherenteId)
                .titular(titular)
                .build();

        when(clienteRepository.findById(titularId))
                .thenReturn(Optional.of(titular));

        when(clienteRepository.findById(adherenteId))
                .thenReturn(Optional.of(adherente));

        IllegalArgumentException excepcion =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> clienteService.asociarAdherente(
                                titularId,
                                adherenteId
                        )
                );

        assertEquals(
                "El cliente ya es adherente del titular indicado",
                excepcion.getMessage()
        );

        verify(clienteRepository, never())
                .save(any(Cliente.class));
    }

    /**
     * Verifica que un adherente no pueda reasignarse
     * silenciosamente a otro titular.
     */
    @Test
    void deberiaRechazarAdherenteQueYaPoseeOtroTitular() {
        UUID nuevoTitularId = UUID.randomUUID();
        UUID adherenteId = UUID.randomUUID();

        Cliente nuevoTitular = Cliente.builder()
                .id(nuevoTitularId)
                .build();

        Cliente titularActual = Cliente.builder()
                .id(UUID.randomUUID())
                .build();

        Cliente adherente = Cliente.builder()
                .id(adherenteId)
                .titular(titularActual)
                .build();

        when(clienteRepository.findById(nuevoTitularId))
                .thenReturn(Optional.of(nuevoTitular));

        when(clienteRepository.findById(adherenteId))
                .thenReturn(Optional.of(adherente));

        IllegalArgumentException excepcion =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> clienteService.asociarAdherente(
                                nuevoTitularId,
                                adherenteId
                        )
                );

        assertEquals(
                "El cliente ya posee un titular asociado",
                excepcion.getMessage()
        );

        assertSame(
                titularActual,
                adherente.getTitular()
        );

        verify(clienteRepository, never())
                .save(any(Cliente.class));
    }

    /**
     * Verifica que una cuenta perteneciente al titular pueda
     * autorizarse correctamente a uno de sus adherentes.
     */
    @Test
    void deberiaAutorizarCuentaAAdherente() {
        UUID titularId = UUID.randomUUID();
        UUID adherenteId = UUID.randomUUID();
        UUID cuentaId = UUID.randomUUID();

        Cliente titular = Cliente.builder()
                .id(titularId)
                .build();

        Cliente adherente = Cliente.builder()
                .id(adherenteId)
                .titular(titular)
                .build();

        CuentaCorriente cuenta =
                new CuentaCorriente();

        cuenta.setId(cuentaId);
        cuenta.getTitulares().add(titular);

        when(clienteRepository.findById(titularId))
                .thenReturn(Optional.of(titular));

        when(clienteRepository.findById(adherenteId))
                .thenReturn(Optional.of(adherente));

        when(cuentaRepository.findById(cuentaId))
                .thenReturn(Optional.of(cuenta));

        clienteService.autorizarCuentaAdherente(
                titularId,
                adherenteId,
                cuentaId
        );

        assertTrue(
                adherente.getCuentasAutorizadas()
                        .contains(cuenta)
        );

        assertTrue(
                adherente.getCuentas().isEmpty()
        );

        verify(clienteRepository)
                .save(adherente);
    }

    /**
     * Verifica que no pueda autorizarse una cuenta cuando el cliente
     * no pertenece al grupo familiar del titular indicado.
     */
    @Test
    void deberiaRechazarAutorizacionSiNoEsAdherenteDelTitular() {
        UUID titularId = UUID.randomUUID();
        UUID adherenteId = UUID.randomUUID();
        UUID cuentaId = UUID.randomUUID();

        Cliente titular = Cliente.builder()
                .id(titularId)
                .build();

        Cliente otroTitular = Cliente.builder()
                .id(UUID.randomUUID())
                .build();

        Cliente adherente = Cliente.builder()
                .id(adherenteId)
                .titular(otroTitular)
                .build();

        CuentaCorriente cuenta =
                new CuentaCorriente();

        cuenta.setId(cuentaId);
        cuenta.getTitulares().add(titular);

        when(clienteRepository.findById(titularId))
                .thenReturn(Optional.of(titular));

        when(clienteRepository.findById(adherenteId))
                .thenReturn(Optional.of(adherente));

        when(cuentaRepository.findById(cuentaId))
                .thenReturn(Optional.of(cuenta));

        IllegalArgumentException excepcion =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> clienteService.autorizarCuentaAdherente(
                                titularId,
                                adherenteId,
                                cuentaId
                        )
                );

        assertEquals(
                "El cliente no es adherente del titular indicado",
                excepcion.getMessage()
        );

        verify(clienteRepository, never())
                .save(any(Cliente.class));
    }

    /**
     * Verifica que no pueda autorizarse al adherente una cuenta
     * perteneciente a otro titular.
     */
    @Test
    void deberiaRechazarAutorizacionDeCuentaAjena() {
        UUID titularId = UUID.randomUUID();
        UUID adherenteId = UUID.randomUUID();
        UUID cuentaId = UUID.randomUUID();

        Cliente titular = Cliente.builder()
                .id(titularId)
                .build();

        Cliente adherente = Cliente.builder()
                .id(adherenteId)
                .titular(titular)
                .build();

        Cliente titularAjeno = Cliente.builder()
                .id(UUID.randomUUID())
                .build();

        CuentaCorriente cuentaAjena =
                new CuentaCorriente();

        cuentaAjena.setId(cuentaId);
        cuentaAjena.getTitulares().add(titularAjeno);

        when(clienteRepository.findById(titularId))
                .thenReturn(Optional.of(titular));

        when(clienteRepository.findById(adherenteId))
                .thenReturn(Optional.of(adherente));

        when(cuentaRepository.findById(cuentaId))
                .thenReturn(Optional.of(cuentaAjena));

        IllegalArgumentException excepcion =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> clienteService.autorizarCuentaAdherente(
                                titularId,
                                adherenteId,
                                cuentaId
                        )
                );

        assertEquals(
                "La cuenta no pertenece al titular indicado",
                excepcion.getMessage()
        );

        assertTrue(
                adherente.getCuentasAutorizadas()
                        .isEmpty()
        );

        verify(clienteRepository, never())
                .save(any(Cliente.class));
    }

    /**
     * Verifica que una misma cuenta no pueda autorizarse
     * dos veces al mismo adherente.
     */
    @Test
    void deberiaRechazarCuentaYaAutorizada() {
        UUID titularId = UUID.randomUUID();
        UUID adherenteId = UUID.randomUUID();
        UUID cuentaId = UUID.randomUUID();

        Cliente titular = Cliente.builder()
                .id(titularId)
                .build();

        Cliente adherente = Cliente.builder()
                .id(adherenteId)
                .titular(titular)
                .build();

        CuentaCorriente cuenta =
                new CuentaCorriente();

        cuenta.setId(cuentaId);
        cuenta.getTitulares().add(titular);

        adherente.autorizarCuenta(cuenta);

        when(clienteRepository.findById(titularId))
                .thenReturn(Optional.of(titular));

        when(clienteRepository.findById(adherenteId))
                .thenReturn(Optional.of(adherente));

        when(cuentaRepository.findById(cuentaId))
                .thenReturn(Optional.of(cuenta));

        IllegalArgumentException excepcion =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> clienteService.autorizarCuentaAdherente(
                                titularId,
                                adherenteId,
                                cuentaId
                        )
                );

        assertEquals(
                "La cuenta ya está autorizada para el adherente",
                excepcion.getMessage()
        );

        verify(clienteRepository, never())
                .save(any(Cliente.class));
    }

    /**
     * Verifica que no pueda realizarse una autorización
     * cuando la cuenta solicitada no existe.
     */
    @Test
    void deberiaRechazarAutorizacionCuandoCuentaNoExiste() {
        UUID titularId = UUID.randomUUID();
        UUID adherenteId = UUID.randomUUID();
        UUID cuentaId = UUID.randomUUID();

        Cliente titular = Cliente.builder()
                .id(titularId)
                .build();

        Cliente adherente = Cliente.builder()
                .id(adherenteId)
                .titular(titular)
                .build();

        when(clienteRepository.findById(titularId))
                .thenReturn(Optional.of(titular));

        when(clienteRepository.findById(adherenteId))
                .thenReturn(Optional.of(adherente));

        when(cuentaRepository.findById(cuentaId))
                .thenReturn(Optional.empty());

        RecursoNoEncontradoException excepcion =
                assertThrows(
                        RecursoNoEncontradoException.class,
                        () -> clienteService.autorizarCuentaAdherente(
                                titularId,
                                adherenteId,
                                cuentaId
                        )
                );

        assertEquals(
                "La cuenta no existe",
                excepcion.getMessage()
        );

        verify(clienteRepository, never())
                .save(any(Cliente.class));
    }
}