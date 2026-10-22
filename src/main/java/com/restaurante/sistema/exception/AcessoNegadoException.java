package com.restaurante.sistema.exception;

/**
 * Lançada quando o usuário ESTÁ logado, mas não tem o papel/permissão
 * necessário para o recurso (ex: usuário comum tentando acessar rota
 * de administrador). Diferente de AutenticacaoNecessariaException,
 * que é para quando nem sequer existe uma sessão válida.
 *
 * Tratada no TratadorDeExcecoes como HTTP 403 (Forbidden).
 */
public class AcessoNegadoException extends RuntimeException {
    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}