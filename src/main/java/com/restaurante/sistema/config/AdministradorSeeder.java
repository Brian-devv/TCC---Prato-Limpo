package com.restaurante.sistema.config;

import com.restaurante.sistema.model.Administrador;
import com.restaurante.sistema.model.Email;
import com.restaurante.sistema.repository.AdministradorRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Roda automaticamente toda vez que a aplicação inicia. Se ainda não
 * existir nenhum administrador no banco, cria um com as credenciais
 * definidas em application.properties (admin.email / admin.senha-inicial)
 * e imprime essas credenciais no console — mesmo espírito 
 * que já usamos no link de redefinição de senha: simples de testar
 * em desenvolvimento, e fácil de trocar por algo mais robusto depois
 * (ex: exigir troca de senha no primeiro login).
 */
@Component
@RequiredArgsConstructor
public class AdministradorSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdministradorSeeder.class);

    private final AdministradorRepository administradorRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email:admin@restaurante.com}")
    private String emailAdminPadrao;

    @Value("${admin.senha-inicial:Admin@123}")
    private String senhaAdminPadrao;

    @Override
    @Transactional
    public void run(String... args) {
        if (administradorRepository.count() > 0) {
            return; // já existe pelo menos um administrador; nada a fazer
        }

        int posicaoArroba = emailAdminPadrao.indexOf('@');
        String endereco = emailAdminPadrao.substring(0, posicaoArroba);
        String dominio = emailAdminPadrao.substring(posicaoArroba + 1);

        Administrador administrador = new Administrador();
        administrador.setNome("Administrador Padrão");
        administrador.setCpf("00000000000"); // conta de sistema, não representa uma pessoa real
        administrador.setDataNascimento(LocalDate.of(2000, 1, 1));
        administrador.setSenha(passwordEncoder.encode(senhaAdminPadrao));
        administrador.setTipoPerfil("ADMINISTRADOR");
        administrador.setStatus("ATIVO");
        administrador.setDataCriacaoConta(LocalDate.now());
        administrador.setDataAtualizacao(LocalDateTime.now());
        administrador.setRegistroProfissional("SEED-0001");

        Email email = new Email();
        email.setEndereco(endereco);
        email.setDominio(dominio);
        email.setUsuario(administrador);
        administrador.setEmail(email);

        administradorRepository.save(administrador);

        log.info("=====================================================================");
        log.info("[SEED] Nenhum administrador encontrado — criado um automaticamente:");
        log.info("  E-mail: {}", emailAdminPadrao);
        log.info("  Senha : {}", senhaAdminPadrao);
        log.info("  (defina admin.email / admin.senha-inicial no application.properties");
        log.info("   para personalizar essas credenciais)");
        log.info("=====================================================================");
    }
}