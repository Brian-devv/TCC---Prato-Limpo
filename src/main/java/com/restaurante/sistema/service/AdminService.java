package com.restaurante.sistema.service;

import com.restaurante.sistema.dto.UsuarioResumoDTO;
import com.restaurante.sistema.exception.RegraNegocioException;
import com.restaurante.sistema.model.Usuario;
import com.restaurante.sistema.repository.TokenRedefinicaoSenhaRepository;
import com.restaurante.sistema.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Regras de negócio exclusivas do painel administrativo.
 *
 * A criação de funcionários continua reaproveitando
 * AutenticacaoService.cadastrarFuncionario(), e a edição de dados
 * (nome/e-mail) reaproveita UsuarioService.atualizarPerfil() — ambas
 * chamadas diretamente pelo AdminController. Aqui ficam só as regras
 * específicas do administrador: listar e alternar status.
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UsuarioRepository usuarioRepository;
    private final TokenRedefinicaoSenhaRepository tokenRedefinicaoSenhaRepository;

    /**
     * Lista todos os usuários do sistema (comuns, administradores,
     * funcionários e outros subtipos futuros). Com a herança JOINED,
     * consultar o repositório da entidade base (Usuario) já traz todo
     * mundo automaticamente.
     */
    @Transactional(readOnly = true)
    public List<UsuarioResumoDTO> listarUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .map(UsuarioResumoDTO::new)
                .toList();
    }

    /**
     * Alterna o status de um usuário entre ATIVO e BLOQUEADO — é o
     * botão "switch" da tabela. Um usuário BLOQUEADO não consegue
     * fazer login (ver AutenticacaoService.autenticar(), que já
     * confere o status).
     *
     * Regra de segurança: um administrador não pode bloquear a
     * própria conta por aqui (evitaria se trancar para fora do
     * sistema sem querer).
     */
    @Transactional
    public UsuarioResumoDTO alternarStatusUsuario(Long idParaAlterar, Long idAdministradorLogado) {
        if (idParaAlterar.equals(idAdministradorLogado)) {
            throw new RegraNegocioException("Você não pode bloquear a própria conta.");
        }

        Usuario usuario = usuarioRepository.findById(idParaAlterar)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));

        String novoStatus = "ATIVO".equals(usuario.getStatus()) ? "BLOQUEADO" : "ATIVO";
        usuario.setStatus(novoStatus);
        usuario.setDataAtualizacao(LocalDateTime.now());

        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        return new UsuarioResumoDTO(usuarioSalvo);
    }

    /**
     * Exclui um usuário pelo id.
     *
     * Mantido no backend mesmo sem botão correspondente na tela hoje
     * (o painel passou a usar bloquear/ativar em vez de excluir na
     * tabela) — pode ser reaproveitado no futuro se fizer sentido ter
     * uma exclusão definitiva em algum outro lugar da interface.
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