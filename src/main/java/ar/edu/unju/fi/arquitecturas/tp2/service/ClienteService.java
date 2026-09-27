package ar.edu.unju.fi.arquitecturas.tp2.service;

import ar.edu.unju.fi.arquitecturas.tp2.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import ar.edu.unju.fi.arquitecturas.tp2.model.Cliente;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public Optional<Cliente> buscarPorCuil(String cuil) {
        return clienteRepository.findByCuil(cuil);
    }

    public Cliente guardar(Cliente cliente) {
        return clienteRepository.save(cliente);
    }
}