package ar.edu.unju.fi.arquitecturas.tp2.service;

import ar.edu.unju.fi.arquitecturas.tp2.model.Cliente;

import java.util.Optional;

public interface ClienteService {

    Optional<Cliente> buscarPorCuil(String cuil);

    Cliente guardar(Cliente cliente);
}