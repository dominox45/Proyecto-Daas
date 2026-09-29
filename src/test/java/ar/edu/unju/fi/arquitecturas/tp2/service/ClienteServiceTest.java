package ar.edu.unju.fi.arquitecturas.tp2.service;

import ar.edu.unju.fi.arquitecturas.tp2.dto.ClienteRequestDto;
import ar.edu.unju.fi.arquitecturas.tp2.dto.ClienteResponseDto;
import ar.edu.unju.fi.arquitecturas.tp2.model.Cliente;
import ar.edu.unju.fi.arquitecturas.tp2.repository.ClienteRepository;
import ar.edu.unju.fi.arquitecturas.tp2.service.impl.ClienteServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para {@link ClienteServiceImpl}.
 *
 * <p>
 * Verifica tanto las operaciones desarrolladas en etapas anteriores
 * como la lógica incorporada para el registro de clientes mediante DTOs.
 * </p>
 *
 * <p>
 * Los repositorios son simulados con Mockito para probar únicamente
 * el comportamiento de la capa de servicios sin depender de la base
 * de datos.
 * </p>
 *
 * @author MaxDz
 * @version 1.1.0
 * @see ClienteServiceImpl
 * @see ClienteRepository
 */
@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    /**
     * Repositorio simulado utilizado por el servicio durante las pruebas.
     */
    @Mock
    private ClienteRepository clienteRepository;

    /**
     * Servicio bajo prueba con sus dependencias simuladas inyectadas.
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

        Optional<Cliente> resultado = clienteService.buscarPorCuil(cuil);

        assertSame(cliente, resultado.orElseThrow());
        verify(clienteRepository).findByCuil(cuil);
    }

    /**
     * Verifica que la búsqueda retorne un Optional vacío cuando
     * no existe un cliente con el CUIL solicitado.
     */
    @Test
    void deberiaDevolverVacioCuandoNoExisteCliente() {
        String cuil = "20999999999";

        when(clienteRepository.findByCuil(cuil))
                .thenReturn(Optional.empty());

        Optional<Cliente> resultado = clienteService.buscarPorCuil(cuil);

        assertTrue(resultado.isEmpty());
        verify(clienteRepository).findByCuil(cuil);
    }

    /**
     * Verifica la persistencia directa de una entidad Cliente.
     */
    @Test
    void deberiaGuardarCliente() {
        Cliente cliente = new Cliente();
        cliente.setNombre("Juan Pérez");
        cliente.setCuil("20304050607");

        when(clienteRepository.save(cliente))
                .thenReturn(cliente);

        Cliente resultado = clienteService.guardar(cliente);

        assertSame(cliente, resultado);
        verify(clienteRepository).save(cliente);
    }

    /**
     * Verifica que un cliente válido pueda registrarse correctamente
     * y que el servicio devuelva un DTO con los datos persistidos.
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

        ClienteResponseDto resultado = clienteService.crear(request);

        assertEquals(id, resultado.getId());
        assertEquals(request.getNombre(), resultado.getNombre());
        assertEquals(request.getCuil(), resultado.getCuil());
        assertEquals(request.getEmail(), resultado.getEmail());
        assertEquals(request.getTelefono(), resultado.getTelefono());
        assertEquals(request.getDireccion(), resultado.getDireccion());

        verify(clienteRepository).findByCuil(request.getCuil());
        verify(clienteRepository).findByEmail(request.getEmail());
        verify(clienteRepository).save(any(Cliente.class));
    }

    /**
     * Verifica que no pueda registrarse un cliente cuyo CUIL
     * ya se encuentre almacenado.
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

        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> clienteService.crear(request)
        );

        assertEquals(
                "Ya existe un cliente con el CUIL indicado",
                excepcion.getMessage()
        );

        verify(clienteRepository).findByCuil(request.getCuil());
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    /**
     * Verifica que no pueda registrarse un cliente cuyo correo
     * electrónico ya se encuentre asociado a otro registro.
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

        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> clienteService.crear(request)
        );

        assertEquals(
                "Ya existe un cliente con el email indicado",
                excepcion.getMessage()
        );

        verify(clienteRepository).findByCuil(request.getCuil());
        verify(clienteRepository).findByEmail(request.getEmail());
        verify(clienteRepository, never()).save(any(Cliente.class));
    }
}