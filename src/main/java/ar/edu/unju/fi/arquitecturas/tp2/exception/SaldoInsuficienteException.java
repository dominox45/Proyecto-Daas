package ar.edu.unju.fi.arquitecturas.tp2.exception;

public class SaldoInsuficienteException extends RuntimeException {

    public SaldoInsuficienteException(String mensaje) {
        super(mensaje);
    }
}