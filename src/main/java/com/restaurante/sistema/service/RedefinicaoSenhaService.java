package com.restaurante.sistema.service;

import com.restaurante.sistema.dto.ConfirmarRedefinicaoSenhaDTO;
import com.restaurante.sistema.exception.RegraNegocioException;
import com.restaurante.sistema.model.TokenRedefinicaoSenha;
import com.restaurante.sistema.model.Usuario;
import com.restaurante.sistema.repository.TokenRedefinicaoSenhaRepository;
import com.restaurante.sistema.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RedefinicaoSenhaService {

    private static final Logger log = LoggerFactory.getLogger(RedefinicaoSenhaService.class);
    private static final long VALIDADE_TOKEN_HORAS = 1;

    private final UsuarioRepository usuarioRepository;
    private final TokenRedefinicaoSenhaRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    public RedefinicaoSenhaService(UsuarioRepository usuarioRepository,
                                   TokenRedefinicaoSenhaRepository tokenRepository,
                                   PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Regra do redefinir-senha.html.
     *
     * Por segurança, NUNCA lançamos exceção aqui, mesmo se o e-mail
     * não existir — assim quem usa o formulário não descobre quais
     * e-mails estão cadastrados no sistema. O controller sempre
     * devolve a mesma mensagem genérica de sucesso.
     *
     * OPÇÃO B (simulação): em vez de enviar um e-mail de verdade,
     * imprimimos o link no console/log da aplicação. Quando o
     * projeto integrar um provedor real (Spring Mail, SendGrid...),
     * troque só a chamada log.info(...) abaixo por um envio real —
     * o resto da lógica (gerar token, validar, expirar) não muda.
     */
    @Transactional
    public void solicitarRedefinicao(String emailCompleto) {
        String[] partesEmail = separarEmail(emailCompleto);
        Optional<Usuario> usuarioOpt = usuarioRepository
                .findByEmail_EnderecoAndEmail_Dominio(partesEmail[0], partesEmail[1]);

        if (usuarioOpt.isEmpty()) {
            log.info("Redefinição de senha solicitada para e-mail não cadastrado: {}", emailCompleto);
            return; // silencioso de propósito
        }

        TokenRedefinicaoSenha tokenEntidade = new TokenRedefinicaoSenha();
        tokenEntidade.setToken(UUID.randomUUID().toString());
        tokenEntidade.setUsuario(usuarioOpt.get());
        tokenEntidade.setDataCriacao(LocalDateTime.now());
        tokenEntidade.setDataExpiracao(LocalDateTime.now().plusHours(VALIDADE_TOKEN_HORAS));
        tokenEntidade.setUsado(false);
        tokenRepository.save(tokenEntidade);

        String link = "http://localhost:8080/redefinir-senha/confirmar?token=" + tokenEntidade.getToken();
        log.info("=====================================================================");
        log.info("[SIMULAÇÃO DE E-MAIL] Link de redefinição de senha para {}:", emailCompleto);
        log.info(link);
        log.info("(válido por {} hora(s))", VALIDADE_TOKEN_HORAS);
        log.info("=====================================================================");
    }

    /** Regra do redefinir-senha-confirmar.html: valida o token e troca a senha. */
    @Transactional
    public void confirmarRedefinicao(ConfirmarRedefinicaoSenhaDTO dto) {
        TokenRedefinicaoSenha tokenEntidade = tokenRepository.findByToken(dto.getToken())
                .orElseThrow(() -> new RegraNegocioException("Link inválido ou expirado."));

        if (tokenEntidade.isUsado() || tokenEntidade.getDataExpiracao().isBefore(LocalDateTime.now())) {
            throw new RegraNegocioException("Link inválido ou expirado.");
        }

        if (!dto.getNovaSenha().equals(dto.getConfirmarNovaSenha())) {
            throw new RegraNegocioException("As senhas não coincidem.");
        }

        Usuario usuario = tokenEntidade.getUsuario();
        usuario.setSenha(passwordEncoder.encode(dto.getNovaSenha())); // hash, nunca texto puro
        usuario.setDataAtualizacao(LocalDateTime.now());
        usuarioRepository.save(usuario);

        tokenEntidade.setUsado(true); // impede reaproveitar o mesmo link
        tokenRepository.save(tokenEntidade);
    }

    /** Quebra "maria@gmail.com" em ["maria", "gmail.com"]. */
    private String[] separarEmail(String emailCompleto) {
        int posicaoArroba = emailCompleto.indexOf('@');
        if (posicaoArroba < 0) {
            throw new RegraNegocioException("E-mail inválido.");
        }
        return new String[]{
                emailCompleto.substring(0, posicaoArroba),
                emailCompleto.substring(posicaoArroba + 1)
        };
    }
}