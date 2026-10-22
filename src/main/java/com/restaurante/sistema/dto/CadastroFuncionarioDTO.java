package com.restaurante.sistema.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO do formulário "Cadastrar funcionário", exibido dentro do
 * painel administrativo (administrador-painel.html). Reaproveita a
 * mesma ideia de validação dos outros formulários de cadastro do
 * sistema (CPF, e-mail, senha com confirmação), acrescentando os
 * campos específicos de um funcionário: cargo, registro profissional
 * e setor.
 */
@Getter
@Setter
public class CadastroFuncionarioDTO {

    @NotBlank(message = "Informe o nome completo.")
    private String nome;

    @NotBlank(message = "Informe o e-mail.")
    @Email(message = "E-mail inválido.")
    private String email;

    @NotBlank(message = "Informe o CPF.")
    private String cpf;

    @NotNull(message = "Informe a data de nascimento.")
    @Past(message = "A data de nascimento deve estar no passado.")
    private LocalDate dataNascimento;

    @NotBlank(message = "Informe o cargo.")
    private String cargo;

    @NotBlank(message = "Informe o registro profissional.")
    private String registroProfissional;

    @NotBlank(message = "Informe o setor.")
    private String setor;

    @NotBlank(message = "Informe uma senha provisória.")
    @Size(min = 8, message = "A senha deve ter ao menos 8 caracteres.")
    private String senha;

    @NotBlank(message = "Confirme a senha.")
    private String confirmarSenha;
}