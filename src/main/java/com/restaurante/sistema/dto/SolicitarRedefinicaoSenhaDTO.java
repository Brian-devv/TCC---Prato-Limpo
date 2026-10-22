package com.restaurante.sistema.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/** DTO do formulário de redefinir-senha.html (pedido do link). */
@Getter
@Setter
public class SolicitarRedefinicaoSenhaDTO {

    @NotBlank(message = "Informe seu e-mail.")
    @Email(message = "E-mail inválido.")
    private String email;
}