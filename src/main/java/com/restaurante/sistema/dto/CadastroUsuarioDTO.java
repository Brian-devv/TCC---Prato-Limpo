package com.restaurante.sistema.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO do formulário de cadastro.html.
 *
 * O campo "cpf" chega do front já sem pontuação (o cadastro.js remove
 * os caracteres não numéricos antes de enviar). "dataNascimento"
 * chega no formato "aaaa-mm-dd" (padrão do <input type="date">), que
 * o Spring já converte automaticamente para LocalDate.
 */
@Getter
@Setter
public class CadastroUsuarioDTO {

    @NotBlank(message = "Informe seu nome completo.")
    private String nome;

    @NotBlank(message = "Informe seu e-mail.")
    @Email(message = "E-mail inválido.")
    private String email;

    @NotBlank(message = "Informe seu CPF.")
    private String cpf;

    @NotNull(message = "Informe sua data de nascimento.")
    @Past(message = "A data de nascimento deve estar no passado.")
    private LocalDate dataNascimento;

    @NotBlank(message = "Informe uma senha.")
    @Size(min = 8, message = "A senha deve ter ao menos 8 caracteres.")
    private String senha;

    @NotBlank(message = "Confirme a senha.")
    private String confirmarSenha;
}