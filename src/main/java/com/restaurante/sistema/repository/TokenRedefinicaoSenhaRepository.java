package com.restaurante.sistema.repository;

import com.restaurante.sistema.model.TokenRedefinicaoSenha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRedefinicaoSenhaRepository extends JpaRepository<TokenRedefinicaoSenha, Long> {

    Optional<TokenRedefinicaoSenha> findByToken(String token);

    /**
     * Apaga todos os tokens de redefinição de senha associados a um
     * usuário. Precisa ser chamado ANTES de excluir o próprio usuário
     * — senão o banco recusa a exclusão por causa da chave
     * estrangeira "usuario_id" nesta tabela (foi exatamente o erro
     * que apareceu ao tentar excluir uma conta que já tinha
     * solicitado redefinição de senha alguma vez).
     */
    void deleteByUsuario_IdUsuario(Long idUsuario);
}