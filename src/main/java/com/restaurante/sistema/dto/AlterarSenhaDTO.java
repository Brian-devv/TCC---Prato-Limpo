package com.restaurante.sistema.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO para o PUT /api/usuarios/me/senha.
 *
 * Exigimos a senha ATUAL antes de permitir a troca — mesmo com o
 * usuário já logado, isso evita que alguém que "pegou" a sessão
 * aberta em um computador desbloqueado consiga trocar a senha sem
 * saber a original.
 */
@Getter
@Setter
public class AlterarSenhaDTO {

    @NotBlank(message = "Informe sua senha atual.")
    private String senhaAtual;

    @NotBlank(message = "Informe a nova senha.")
    @Size(min = 8, message = "A nova senha deve ter ao menos 8 caracteres.")
    private String novaSenha;

    @NotBlank(message = "Confirme a nova senha.")
    private String confirmarNovaSenha;
}