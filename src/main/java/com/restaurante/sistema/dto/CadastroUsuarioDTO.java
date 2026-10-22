package com.restaurante.sistema.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO do formulário de cadastro.html.
 *
 * "tipoConta" decide qual tipo de conta a pessoa está criando:
 * "USUARIO", "PACIENTE", "ALUNO", "ESTAGIARIO" ou "NUTRICIONISTA".
 * Os campos abaixo dele são obrigatórios OU NÃO dependendo desse
 * valor — por exemplo, "restricoesAlimentares" só é obrigatório
 * quando tipoConta = "PACIENTE". O Bean Validation (@NotBlank, etc.)
 * não tem um jeito nativo de expressar "obrigatório só quando outro
 * campo for X", então essa validação condicional é feita na mão,
 * dentro de AutenticacaoService.cadastrarUsuario().
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

    @NotBlank(message = "Selecione o tipo de conta.")
    private String tipoConta;

    @NotBlank(message = "Informe uma senha.")
    @Size(min = 8, message = "A senha deve ter ao menos 8 caracteres.")
    private String senha;

    @NotBlank(message = "Confirme a senha.")
    private String confirmarSenha;

    // ---- Específicos de PACIENTE ----
    private String restricoesAlimentares;
    private Integer numeroProntuario;

    // ---- Específicos de ALUNO e ESTAGIARIO ----
    private String curso;
    private String instituicao;
    private Integer matricula;

    // ---- Específicos de ESTAGIARIO ----
    private LocalDate dataInicioEstagio;
    private LocalDate dataFimEstagio;

    // ---- Específico de NUTRICIONISTA ----
    private String registroProfissional;
}