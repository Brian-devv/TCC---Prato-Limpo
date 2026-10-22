package com.restaurante.sistema.service;

import com.restaurante.sistema.dto.AlterarSenhaDTO;
import com.restaurante.sistema.dto.AtualizarPerfilDTO;
import com.restaurante.sistema.dto.UsuarioRespostaDTO;
import com.restaurante.sistema.exception.RegraNegocioException;
import com.restaurante.sistema.model.Usuario;
import com.restaurante.sistema.repository.EmailRepository;
import com.restaurante.sistema.repository.TokenRedefinicaoSenhaRepository;
import com.restaurante.sistema.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Regras de negócio do CRUD do usuário logado (perfil).
 *
 * Separado do AutenticacaoService de propósito: aquele cuida de
 * "entrar no sistema" (cadastro + login); este cuida de "gerenciar a
 * própria conta depois de já estar logado".
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmailRepository emailRepository;
    private final TokenRedefinicaoSenhaRepository tokenRedefinicaoSenhaRepository;
    private final PasswordEncoder passwordEncoder;

    /** READ — usado pela tela de perfil pra exibir os dados atuais. */
    @Transactional(readOnly = true)
    public UsuarioRespostaDTO buscarPerfil(Long idUsuario) {
        Usuario usuario = buscarUsuarioOuFalhar(idUsuario);
        return new UsuarioRespostaDTO(usuario);
    }

    /** UPDATE (dados) — nome e e-mail. */
    @Transactional
    public UsuarioRespostaDTO atualizarPerfil(Long idUsuario, AtualizarPerfilDTO dto) {
        Usuario usuario = buscarUsuarioOuFalhar(idUsuario);

        String[] partesEmail = separarEmail(dto.getEmail());
        String endereco = partesEmail[0];
        String dominio = partesEmail[1];

        boolean emailPertenceAOutroUsuario =
                emailRepository.existsByEnderecoAndDominioAndUsuario_IdUsuarioNot(endereco, dominio, idUsuario);
        if (emailPertenceAOutroUsuario) {
            throw new RegraNegocioException("Este e-mail já está em uso por outra conta.");
        }

        usuario.setNome(dto.getNome());
        usuario.getEmail().setEndereco(endereco);
        usuario.getEmail().setDominio(dominio);
        usuario.setDataAtualizacao(LocalDateTime.now());

        Usuario usuarioAtualizado = usuarioRepository.save(usuario);
        return new UsuarioRespostaDTO(usuarioAtualizado);
    }

    /** UPDATE (senha) — exige a senha atual antes de trocar. */
    @Transactional
    public void alterarSenha(Long idUsuario, AlterarSenhaDTO dto) {
        Usuario usuario = buscarUsuarioOuFalhar(idUsuario);

        if (!passwordEncoder.matches(dto.getSenhaAtual(), usuario.getSenha())) {
            throw new RegraNegocioException("A senha atual informada está incorreta.");
        }

        if (!dto.getNovaSenha().equals(dto.getConfirmarNovaSenha())) {
            throw new RegraNegocioException("A nova senha e a confirmação não coincidem.");
        }

        if (passwordEncoder.matches(dto.getNovaSenha(), usuario.getSenha())) {
            throw new RegraNegocioException("A nova senha deve ser diferente da senha atual.");
        }

        usuario.setSenha(passwordEncoder.encode(dto.getNovaSenha()));
        usuario.setDataAtualizacao(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }

    /**
     * DELETE — exclui a conta.
     *
     * A ordem importa: primeiro apagamos os tokens de redefinição de
     * senha (tabela sem relação JPA mapeada em Usuario, então o
     * Hibernate não sabe apagá-los sozinho via cascade). O Email
     * associado é excluído automaticamente pelo cascade = ALL já
     * configurado em Usuario.email.
     */
    @Transactional
    public void excluirConta(Long idUsuario) {
        Usuario usuario = buscarUsuarioOuFalhar(idUsuario);
        tokenRedefinicaoSenhaRepository.deleteByUsuario_IdUsuario(idUsuario);
        usuarioRepository.delete(usuario);
    }

    private Usuario buscarUsuarioOuFalhar(Long idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));
    }

    /** Quebra "maria@gmail.com" em ["maria", "gmail.com"]. */
    private String[] separarEmail(String emailCompleto) {
        int posicaoArroba = emailCompleto.indexOf('@');
        if (posicaoArroba < 0) {
            throw new RegraNegocioException("E-mail inválido.");
        }
        String endereco = emailCompleto.substring(0, posicaoArroba);
        String dominio = emailCompleto.substring(posicaoArroba + 1);
        return new String[]{endereco, dominio};
    }
}