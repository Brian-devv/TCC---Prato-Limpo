package com.restaurante.sistema.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** DTO do formulário de redefinir-senha-confirmar.html (nova senha + token). */
@Getter
@Setter
public class ConfirmarRedefinicaoSenhaDTO {

    @NotBlank(message = "Link inválido.")
    private String token;

    @NotBlank(message = "Informe a nova senha.")
    @Size(min = 8, message = "A nova senha deve ter ao menos 8 caracteres.")
    private String novaSenha;

    @NotBlank(message = "Confirme a nova senha.")
    private String confirmarNovaSenha;
}