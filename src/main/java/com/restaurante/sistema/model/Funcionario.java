package com.restaurante.sistema.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Subtipo de Usuario. Herda todos os campos comuns (nome, cpf, senha,
 * e-mail, etc.) e adiciona o que é específico de um funcionário.
 *
 * Como Usuario já está anotada com @Inheritance(strategy = JOINED),
 * esta classe ganha sua própria tabela ("funcionario"), cuja chave
 * primária (id_usuario) é também chave estrangeira para "usuario" —
 * o mesmo padrão usado em Administrador.
 */
@Entity
@Table(name = "funcionario")
@Getter
@Setter
@NoArgsConstructor
public class Funcionario extends Usuario {

    private String cargo;
    private String registroProfissional;
    private String setor;
}