package com.restaurante.sistema.dto;

import com.restaurante.sistema.model.Usuario;
import lombok.Getter;

/**
 * DTO usado na listagem de usuários do painel administrativo.
 *
 * De propósito, expõe SOMENTE nome, e-mail e tipo de perfil — nada
 * de CPF, data de nascimento ou qualquer outro dado sensível. É
 * exatamente o recorte de dados que o administrador tem permissão
 * de ver sobre as pessoas cadastradas no sistema.
 */
@Getter
public class UsuarioResumoDTO {

    private final Long id;
    private final String nome;
    private final String email;
    private final String tipoPerfil;
    private final String status;

    public UsuarioResumoDTO(Usuario usuario) {
        this.id = usuario.getIdUsuario();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail() != null ? usuario.getEmail().getEnderecoCompleto() : null;
        this.tipoPerfil = usuario.getTipoPerfil();
        this.status = usuario.getStatus();
    }
}