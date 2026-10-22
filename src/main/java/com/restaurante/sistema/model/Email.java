package com.restaurante.sistema.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa o e-mail de um usuário, seguindo o diagrama (endereço +
 * domínio separados).
 *
 * A relação com Usuario é 1:1 — cada usuário tem exatamente um e-mail
 * de cadastro. A restrição "unique = true" na coluna usuario_id é o
 * que faz o próprio banco impedir dois e-mails para o mesmo usuário.
 *
 * Email é o lado "dono" da relação (tem a chave estrangeira), por
 * isso é ele quem carrega a anotação @JoinColumn.
 */
@Entity
@Table(name = "email")
@Getter
@Setter
@NoArgsConstructor
public class Email {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_email")
    private Long idEmail;

    @Column(nullable = false)
    private String endereco; // parte antes do @, ex: "maria.silva"

    @Column(nullable = false)
    private String dominio; // parte depois do @, ex: "gmail.com"

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    /** Facilita exibir/comparar o e-mail completo (não é uma coluna no banco). */
    @Transient
    public String getEnderecoCompleto() {
        return endereco + "@" + dominio;
    }
}