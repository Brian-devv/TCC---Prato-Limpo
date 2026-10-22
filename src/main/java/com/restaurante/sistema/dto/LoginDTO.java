package com.restaurante.sistema.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/** DTO do formulário de entrar.html. */
@Getter
@Setter
public class LoginDTO {

    @NotBlank(message = "Informe seu e-mail.")
    @Email(message = "E-mail inválido.")
    private String email;

    @NotBlank(message = "Informe sua senha.")
    private String senha;

    private boolean lembrar;
}