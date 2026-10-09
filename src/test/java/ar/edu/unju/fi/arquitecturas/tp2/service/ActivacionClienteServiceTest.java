package ar.edu.unju.fi.arquitecturas.tp2.service;

import ar.edu.unju.fi.arquitecturas.tp2.dto.ClienteRequestDto;
import ar.edu.unju.fi.arquitecturas.tp2.event.ClienteCreadoEvent;
import ar.edu.unju.fi.arquitecturas.tp2.model.Cliente;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoCliente;
import ar.edu.unju.fi.arquitecturas.tp2.repository.ClienteRepository;
import ar.edu.unju.fi.arquitecturas.tp2.repository.CuentaFinancieraRepository;
import ar.edu.unju.fi.arquitecturas.tp2.service.impl.ClienteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del proceso de alta y activación
 * de clientes correspondiente al TP5.
 */
@ExtendWith(MockitoExtension.class)
class ActivacionClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private CuentaFinancieraRepository cuentaRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ClienteServiceImpl clienteService;

    @BeforeEach
    void configurarServicio() {
        clienteService = new ClienteServiceImpl(
                clienteRepository,
                cuentaRepository,
                eventPublisher
        );
    }

    /**
     * Verifica que un nuevo cliente quede pendiente de activación,
     * reciba un UUID con vigencia aproximada de 24 horas
     * y publique el evento correspondiente.
     */
    @Test
    void deberiaCrearClientePendienteConTokenYPublicarEvento() {

        UUID clienteId = UUID.randomUUID();

        ClienteRequestDto request = new ClienteRequestDto(
                "Ana López",
                "27304050608",
                "ana.lopez@email.com",
                "3884000000",
                "San Salvador de Jujuy"
        );

        when(clienteRepository.findByCuil(request.getCuil()))
                .thenReturn(Optional.empty());

        when(clienteRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());

        when(clienteRepository.save(any(Cliente.class)))
                .thenAnswer(invocation -> {
                    Cliente cliente = invocation.getArgument(0);
                    cliente.setId(clienteId);
                    return cliente;
                });

        LocalDateTime minimoEsperado =
                LocalDateTime.now()
                        .plusHours(24)
                        .minusSeconds(2);

        clienteService.crear(request);

        LocalDateTime maximoEsperado =
                LocalDateTime.now()
                        .plusHours(24)
                        .plusSeconds(2);

        ArgumentCaptor<Cliente> clienteCaptor =
                ArgumentCaptor.forClass(Cliente.class);

        verify(clienteRepository)
                .save(clienteCaptor.capture());

        Cliente cliente =
                clienteCaptor.getValue();

        assertEquals(
                EstadoCliente.PENDIENTE_ACTIVACION,
                cliente.getEstado()
        );

        assertNotNull(cliente.getTokenActivacion());

        /*
         * Comprueba además que el valor generado tenga
         * realmente formato UUID.
         */
        assertNotNull(
                UUID.fromString(
                        cliente.getTokenActivacion()
                )
        );

        assertNotNull(
                cliente.getTokenActivacionExpiraEn()
        );

        assertFalse(
                cliente.getTokenActivacionExpiraEn()
                        .isBefore(minimoEsperado)
        );

        assertFalse(
                cliente.getTokenActivacionExpiraEn()
                        .isAfter(maximoEsperado)
        );

        ArgumentCaptor<ClienteCreadoEvent> eventoCaptor =
                ArgumentCaptor.forClass(
                        ClienteCreadoEvent.class
                );

        verify(eventPublisher)
                .publishEvent(eventoCaptor.capture());

        ClienteCreadoEvent evento =
                eventoCaptor.getValue();

        assertEquals(
                clienteId,
                evento.clienteId()
        );

        assertEquals(
                request.getEmail(),
                evento.email()
        );

        assertEquals(
                cliente.getTokenActivacion(),
                evento.tokenActivacion()
        );
    }

    /**
     * Verifica la activación correcta de un cliente
     * utilizando un token vigente.
     */
    @Test
    void deberiaActivarClienteConTokenValido() {

        String token =
                UUID.randomUUID().toString();

        Cliente cliente = Cliente.builder()
                .id(UUID.randomUUID())
                .estado(EstadoCliente.PENDIENTE_ACTIVACION)
                .tokenActivacion(token)
                .tokenActivacionExpiraEn(
                        LocalDateTime.now().plusHours(1)
                )
                .build();

        when(clienteRepository.findByTokenActivacion(token))
                .thenReturn(Optional.of(cliente));

        when(clienteRepository.save(cliente))
                .thenReturn(cliente);

        clienteService.activar(token);

        assertEquals(
                EstadoCliente.ACTIVO,
                cliente.getEstado()
        );

        assertNotNull(
                cliente.getFechaActivacion()
        );

        assertNull(
                cliente.getTokenActivacion()
        );

        assertNull(
                cliente.getTokenActivacionExpiraEn()
        );

        verify(clienteRepository)
                .save(cliente);
    }

    /**
     * Verifica que un token inexistente sea rechazado.
     */
    @Test
    void deberiaRechazarTokenInexistente() {

        String token =
                UUID.randomUUID().toString();

        when(clienteRepository.findByTokenActivacion(token))
                .thenReturn(Optional.empty());

        IllegalArgumentException excepcion =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> clienteService.activar(token)
                );

        assertEquals(
                "El token de activación no existe",
                excepcion.getMessage()
        );

        verify(clienteRepository, never())
                .save(any(Cliente.class));
    }

    /**
     * Verifica que un token vencido no permita activar al cliente
     * ni modificar su estado.
     */
    @Test
    void deberiaRechazarTokenVencido() {

        String token =
                UUID.randomUUID().toString();

        Cliente cliente = Cliente.builder()
                .id(UUID.randomUUID())
                .estado(EstadoCliente.PENDIENTE_ACTIVACION)
                .tokenActivacion(token)
                .tokenActivacionExpiraEn(
                        LocalDateTime.now().minusMinutes(1)
                )
                .build();

        when(clienteRepository.findByTokenActivacion(token))
                .thenReturn(Optional.of(cliente));

        IllegalArgumentException excepcion =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> clienteService.activar(token)
                );

        assertEquals(
                "El token de activación está vencido",
                excepcion.getMessage()
        );

        assertEquals(
                EstadoCliente.PENDIENTE_ACTIVACION,
                cliente.getEstado()
        );

        assertNull(
                cliente.getFechaActivacion()
        );

        verify(clienteRepository, never())
                .save(any(Cliente.class));
    }
}