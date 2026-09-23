package ar.edu.unju.fi.arquitecturas.tp2.service;

import ar.edu.unju.fi.arquitecturas.tp2.model.Cliente;
import ar.edu.unju.fi.arquitecturas.tp2.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)

class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

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

    @Test
    void deberiaDevolverVacioCuandoNoExisteCliente() {
        String cuil = "20999999999";

        when(clienteRepository.findByCuil(cuil))
                .thenReturn(Optional.empty());

        Optional<Cliente> resultado = clienteService.buscarPorCuil(cuil);

        assertTrue(resultado.isEmpty());
        verify(clienteRepository).findByCuil(cuil);
    }

    @Test
    void deberiaGuardarCliente() {
        Cliente cliente = new Cliente();
        cliente.setNombre("Juan Pérez");
        cliente.setCuil("20304050607");

        when(clienteRepository.save(cliente)).thenReturn(cliente);

        Cliente resultado = clienteService.guardar(cliente);

        assertSame(cliente, resultado);
        verify(clienteRepository).save(cliente);
    }
}