package com.restaurante.sistema.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Subtipo de Usuario. Herda todos os campos (nome, cpf, senha, etc.)
 * e adiciona o que é específico de um administrador.
 *
 * Como usamos @Inheritance(strategy = JOINED) em Usuario, esta classe
 * ganha sua própria tabela ("administrador"), cuja chave primária
 * (id_usuario) é também chave estrangeira para a tabela "usuario".
 */
@Entity
@Table(name = "administrador")
@Getter
@Setter
@NoArgsConstructor
public class Administrador extends Usuario {

    private String registroProfissional;
}