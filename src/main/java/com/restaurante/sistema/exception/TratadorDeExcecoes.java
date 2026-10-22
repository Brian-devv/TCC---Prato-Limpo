package com.restaurante.sistema.exception;

import com.restaurante.sistema.dto.RespostaApiDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TratadorDeExcecoes {

    /** Erros de regra de negócio (ex: "e-mail já cadastrado") -> HTTP 400. */
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<RespostaApiDTO> tratarRegraNegocio(RegraNegocioException ex) {
        return ResponseEntity.badRequest().body(new RespostaApiDTO(false, ex.getMessage()));
    }

    /** Usuário não logado tentando acessar rota que exige sessão -> HTTP 401. */
    @ExceptionHandler(AutenticacaoNecessariaException.class)
    public ResponseEntity<RespostaApiDTO> tratarAutenticacaoNecessaria(AutenticacaoNecessariaException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RespostaApiDTO(false, ex.getMessage()));
    }

    /** Usuário logado, mas sem o papel/permissão necessário -> HTTP 403. */
    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<RespostaApiDTO> tratarAcessoNegado(AcessoNegadoException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new RespostaApiDTO(false, ex.getMessage()));
    }

    /** Erros de validação do @Valid (ex: @NotBlank, @Email) -> HTTP 400. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespostaApiDTO> tratarValidacao(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(erro -> erro.getDefaultMessage())
                .orElse("Dados inválidos.");
        return ResponseEntity.badRequest().body(new RespostaApiDTO(false, mensagem));
    }

    /** Qualquer outro erro inesperado -> HTTP 500. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespostaApiDTO> tratarErroGenerico(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RespostaApiDTO(false, "Erro interno: " + ex.getMessage()));
    }
}