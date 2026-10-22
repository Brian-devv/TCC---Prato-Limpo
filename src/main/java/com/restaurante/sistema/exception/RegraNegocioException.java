package com.restaurante.sistema.exception;

/**
 * Lançada quando uma regra de negócio impede a operação
 * (ex: e-mail já cadastrado, senhas não coincidem).
 *
 * Vamos capturar essa exceção no controller pra devolver uma
 * resposta amigável ao front, em vez de um erro 500 genérico.
 */
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}