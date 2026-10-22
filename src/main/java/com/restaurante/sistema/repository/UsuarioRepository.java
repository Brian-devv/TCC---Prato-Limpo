package com.restaurante.sistema.repository;

import com.restaurante.sistema.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * O Spring Data JPA entende "Email_Endereco" e "Email_Dominio" como
     * "atravesse o campo 'email' do Usuario e filtre pelos campos
     * 'endereco' e 'dominio' dele" — sem precisarmos escrever o JOIN
     * na mão. O "_" deixa explícito onde um nome de campo termina e
     * o próximo começa (evita ambiguidade).
     */
    Optional<Usuario> findByEmail_EnderecoAndEmail_Dominio(String endereco, String dominio);

    boolean existsByCpf(String cpf);
}