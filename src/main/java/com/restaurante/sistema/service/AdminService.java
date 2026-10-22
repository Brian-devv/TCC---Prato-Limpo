package com.restaurante.sistema.service;

import com.restaurante.sistema.dto.UsuarioResumoDTO;
import com.restaurante.sistema.exception.RegraNegocioException;
import com.restaurante.sistema.model.Usuario;
import com.restaurante.sistema.repository.TokenRedefinicaoSenhaRepository;
import com.restaurante.sistema.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Regras de negócio exclusivas do painel administrativo.
 *
 * Este service NÃO cuida da criação de usuários — isso continua
 * reaproveitando o AutenticacaoService.cadastrarUsuario(), chamado
 * diretamente pelo AdminController. Aqui ficam só as regras
 * específicas do administrador: listar e excluir.
 */
@Service
public class AdminService {

    private final UsuarioRepository usuarioRepository;
    private final TokenRedefinicaoSenhaRepository tokenRedefinicaoSenhaRepository;

    public AdminService(UsuarioRepository usuarioRepository,
                        TokenRedefinicaoSenhaRepository tokenRedefinicaoSenhaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRedefinicaoSenhaRepository = tokenRedefinicaoSenhaRepository;
    }

    /**
     * Lista todos os usuários do sistema (comuns, administradores e,
     * no futuro, outros subtipos). Com a herança JOINED, consultar o
     * repositório da entidade base (Usuario) já traz todo mundo
     * automaticamente, sem precisar de union manual entre tabelas.
     */
    @Transactional(readOnly = true)
    public List<UsuarioResumoDTO> listarUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .map(UsuarioResumoDTO::new)
                .toList();
    }

    /**
     * Exclui um usuário pelo id.
     *
     * Regra de segurança: um administrador não pode excluir a própria
     * conta por aqui — evita que ele se tranque para fora do sistema
     * sem querer. Se quiser excluir a própria conta, o caminho é a
     * tela de perfil normal (/perfil).
     *
     * Antes de excluir o usuário, apagamos os tokens de redefinição
     * de senha associados a ele (tabela sem cascade mapeado em
     * Usuario) — senão o banco recusa a exclusão por causa da chave
     * estrangeira "usuario_id" em token_redefinicao_senha.
     */
    @Transactional
    public void excluirUsuario(Long idParaExcluir, Long idAdministradorLogado) {
        if (idParaExcluir.equals(idAdministradorLogado)) {
            throw new RegraNegocioException(
                    "Você não pode excluir sua própria conta por aqui. Use a opção \"Excluir minha conta\" no seu perfil."
            );
        }

        Usuario usuario = usuarioRepository.findById(idParaExcluir)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));

        tokenRedefinicaoSenhaRepository.deleteByUsuario_IdUsuario(idParaExcluir);
        usuarioRepository.delete(usuario);
    }
}