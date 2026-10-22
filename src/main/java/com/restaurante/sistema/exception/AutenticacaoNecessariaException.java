package com.restaurante.sistema.exception;

/**
 * Lançada quando um endpoint exige um usuário logado (via sessão) e
 * não há sessão válida. Tratada no TratadorDeExcecoes como HTTP 401.
 */
public class AutenticacaoNecessariaException extends RuntimeException {
    public AutenticacaoNecessariaException(String mensagem) {
        super(mensagem);
    }
}