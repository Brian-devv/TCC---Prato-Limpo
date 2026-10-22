package com.restaurante.sistema.repository;

import com.restaurante.sistema.model.Email;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailRepository extends JpaRepository<Email, Long> {

    boolean existsByEnderecoAndDominio(String endereco, String dominio);

    /**
     * Usado na edição de perfil: verifica se o e-mail já pertence a
     * OUTRO usuário (diferente do que está editando), pra permitir
     * que a pessoa salve o formulário sem mudar o próprio e-mail.
     */
    boolean existsByEnderecoAndDominioAndUsuario_IdUsuarioNot(String endereco, String dominio, Long idUsuario);
}