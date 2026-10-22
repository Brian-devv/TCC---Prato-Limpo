package com.restaurante.sistema.dto;

import com.restaurante.sistema.model.Usuario;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de saída para os endpoints de perfil.
 *
 * Nunca devolvemos a entidade Usuario diretamente na API — entre
 * outros motivos, porque ela carrega o campo "senha" (o hash). Esse
 * DTO expõe só o que é seguro/necessário mostrar ao front-end.
 */
@Getter
public class UsuarioRespostaDTO {

    private final Long id;
    private final String nome;
    private final String email;
    private final String cpf;
    private final LocalDate dataNascimento;
    private final String tipoPerfil;
    private final String status;
    private final LocalDate dataCriacaoConta;
    private final LocalDateTime dataAtualizacao;

    public UsuarioRespostaDTO(Usuario usuario) {
        this.id = usuario.getIdUsuario();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail() != null ? usuario.getEmail().getEnderecoCompleto() : null;
        this.cpf = usuario.getCpf();
        this.dataNascimento = usuario.getDataNascimento();
        this.tipoPerfil = usuario.getTipoPerfil();
        this.status = usuario.getStatus();
        this.dataCriacaoConta = usuario.getDataCriacaoConta();
        this.dataAtualizacao = usuario.getDataAtualizacao();
    }
}