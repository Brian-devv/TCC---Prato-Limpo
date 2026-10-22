package com.restaurante.sistema.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO do formulário "Cadastrar funcionário ou gestor", no painel
 * administrativo.
 *
 * "tipoConta" ("FUNCIONARIO" ou "GESTOR") decide se cargo/setor são
 * obrigatórios: um Gestor não tem cargo/setor no diagrama, só
 * registro profissional. Essa validação condicional acontece em
 * AutenticacaoService.cadastrarColaborador().
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

    @NotBlank(message = "Selecione o tipo de cadastro.")
    private String tipoConta; // "FUNCIONARIO" ou "GESTOR"

    // Obrigatórios só quando tipoConta = "FUNCIONARIO"
    private String cargo;
    private String setor;

    // Obrigatório para os dois tipos
    @NotBlank(message = "Informe o registro profissional.")
    private String registroProfissional;

    @NotBlank(message = "Informe uma senha provisória.")
    @Size(min = 8, message = "A senha deve ter ao menos 8 caracteres.")
    private String senha;

    @NotBlank(message = "Confirme a senha.")
    private String confirmarSenha;
}