package com.restaurante.sistema.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidade que representa um usuário cadastrado no sistema.
 *
 * @Inheritance(strategy = JOINED): permite que subtipos (como
 * Administrador) tenham sua própria tabela, ligada pela mesma chave
 * primária (id_usuario) — exatamente como no diagrama físico do
 * projeto. A tabela "usuario" continua guardando os dados comuns a
 * todo mundo; cada subtipo guarda só os campos extras dele.
 *
 * Usuario continua sendo concreta (não abstrata): o cadastro público
 * (cadastro.html) cria um Usuario "puro", sem subtipo.
 */
@Entity
@Table(name = "usuario")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf; // somente dígitos, sem pontuação (ex: "12345678900")

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(nullable = false)
    private String senha;

    @Column(name = "tipo_perfil", nullable = false)
    private String tipoPerfil; // "USUARIO" ou "ADMINISTRADOR" por enquanto

    @Column(nullable = false)
    private String status = "ATIVO"; // ATIVO, INATIVO, BLOQUEADO

    @Column(name = "data_criacao_conta", nullable = false)
    private LocalDate dataCriacaoConta;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Email email;
}