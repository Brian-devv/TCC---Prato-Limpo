package com.restaurante.sistema.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/** DTO para o PUT /api/usuarios/me (editar nome e e-mail). */
@Getter
@Setter
public class AtualizarPerfilDTO {

    @NotBlank(message = "Informe seu nome completo.")
    private String nome;

    @NotBlank(message = "Informe seu e-mail.")
    @Email(message = "E-mail inválido.")
    private String email;
}