package com.restaurante.sistema.service;

import com.restaurante.sistema.dto.CadastroFuncionarioDTO;
import com.restaurante.sistema.dto.CadastroUsuarioDTO;
import com.restaurante.sistema.dto.LoginDTO;
import com.restaurante.sistema.exception.RegraNegocioException;
import com.restaurante.sistema.model.Email;
import com.restaurante.sistema.model.Funcionario;
import com.restaurante.sistema.model.Usuario;
import com.restaurante.sistema.repository.EmailRepository;
import com.restaurante.sistema.repository.FuncionarioRepository;
import com.restaurante.sistema.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class AutenticacaoService {

    private final UsuarioRepository usuarioRepository;
    private final EmailRepository emailRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AutenticacaoService(UsuarioRepository usuarioRepository,
                               EmailRepository emailRepository,
                               FuncionarioRepository funcionarioRepository,
                               PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.emailRepository = emailRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Regra do cadastro.html: qualquer pessoa pode criar uma conta comum. */
    @Transactional
    public Usuario cadastrarUsuario(CadastroUsuarioDTO dto) {
        if (!dto.getSenha().equals(dto.getConfirmarSenha())) {
            throw new RegraNegocioException("As senhas não coincidem.");
        }

        String cpfSomenteDigitos = dto.getCpf().replaceAll("\\D", "");
        if (!validarCPF(cpfSomenteDigitos)) {
            throw new RegraNegocioException("CPF inválido.");
        }
        if (usuarioRepository.existsByCpf(cpfSomenteDigitos)) {
            throw new RegraNegocioException("Já existe uma conta cadastrada com este CPF.");
        }

        String[] partesEmail = separarEmail(dto.getEmail());
        String endereco = partesEmail[0];
        String dominio = partesEmail[1];

        if (emailRepository.existsByEnderecoAndDominio(endereco, dominio)) {
            throw new RegraNegocioException("Já existe uma conta com este e-mail.");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setCpf(cpfSomenteDigitos);
        usuario.setDataNascimento(dto.getDataNascimento());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setTipoPerfil("USUARIO");
        usuario.setStatus("ATIVO");
        usuario.setDataCriacaoConta(LocalDate.now());
        usuario.setDataAtualizacao(LocalDateTime.now());

        Email email = new Email();
        email.setEndereco(endereco);
        email.setDominio(dominio);
        email.setUsuario(usuario);

        usuario.setEmail(email);

        return usuarioRepository.save(usuario);
    }

    /**
     * Regra da caixa "Cadastrar funcionário" no painel administrativo.
     *
     * Mesma validação de CPF/e-mail/senha do cadastro comum, mas cria
     * um Funcionario (subtipo de Usuario) em vez de um Usuario puro,
     * já com tipoPerfil = "FUNCIONARIO" e os campos extras (cargo,
     * registro profissional, setor).
     */
    @Transactional
    public Funcionario cadastrarFuncionario(CadastroFuncionarioDTO dto) {
        if (!dto.getSenha().equals(dto.getConfirmarSenha())) {
            throw new RegraNegocioException("As senhas não coincidem.");
        }

        String cpfSomenteDigitos = dto.getCpf().replaceAll("\\D", "");
        if (!validarCPF(cpfSomenteDigitos)) {
            throw new RegraNegocioException("CPF inválido.");
        }
        if (usuarioRepository.existsByCpf(cpfSomenteDigitos)) {
            throw new RegraNegocioException("Já existe uma conta cadastrada com este CPF.");
        }

        String[] partesEmail = separarEmail(dto.getEmail());
        String endereco = partesEmail[0];
        String dominio = partesEmail[1];

        if (emailRepository.existsByEnderecoAndDominio(endereco, dominio)) {
            throw new RegraNegocioException("Já existe uma conta com este e-mail.");
        }

        Funcionario funcionario = new Funcionario();
        funcionario.setNome(dto.getNome());
        funcionario.setCpf(cpfSomenteDigitos);
        funcionario.setDataNascimento(dto.getDataNascimento());
        funcionario.setSenha(passwordEncoder.encode(dto.getSenha()));
        funcionario.setTipoPerfil("FUNCIONARIO");
        funcionario.setStatus("ATIVO");
        funcionario.setDataCriacaoConta(LocalDate.now());
        funcionario.setDataAtualizacao(LocalDateTime.now());
        funcionario.setCargo(dto.getCargo());
        funcionario.setRegistroProfissional(dto.getRegistroProfissional());
        funcionario.setSetor(dto.getSetor());

        Email email = new Email();
        email.setEndereco(endereco);
        email.setDominio(dominio);
        email.setUsuario(funcionario);

        funcionario.setEmail(email);

        return funcionarioRepository.save(funcionario);
    }

    /**
     * Regra do entrar.html: localiza o usuário pelo e-mail e confere a senha.
     *
     * Por segurança, a mensagem de erro é sempre a mesma tanto se o
     * e-mail não existir quanto se a senha estiver errada.
     */
    @Transactional(readOnly = true)
    public Usuario autenticar(LoginDTO dto) {
        String[] partesEmail = separarEmail(dto.getEmail());
        String endereco = partesEmail[0];
        String dominio = partesEmail[1];

        Usuario usuario = usuarioRepository.findByEmail_EnderecoAndEmail_Dominio(endereco, dominio)
                .orElseThrow(() -> new RegraNegocioException("E-mail ou senha inválidos."));

        if (!passwordEncoder.matches(dto.getSenha(), usuario.getSenha())) {
            throw new RegraNegocioException("E-mail ou senha inválidos.");
        }

        if (!"ATIVO".equals(usuario.getStatus())) {
            throw new RegraNegocioException("Esta conta está inativa. Contate um administrador.");
        }

        return usuario;
    }

    /**
     * Regra do administrador.html: mesma checagem de e-mail/senha do
     * login comum, MAS exige que o usuário seja do tipo ADMINISTRADOR.
     */
    @Transactional(readOnly = true)
    public Usuario autenticarAdministrador(LoginDTO dto) {
        Usuario usuario = autenticar(dto);

        if (!"ADMINISTRADOR".equals(usuario.getTipoPerfil())) {
            throw new RegraNegocioException("E-mail ou senha inválidos.");
        }

        return usuario;
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

    /** Validação de CPF pelo algoritmo oficial de dígitos verificadores. */
    private boolean validarCPF(String cpf) {
        if (cpf.length() != 11 || cpf.chars().distinct().count() == 1) {
            return false;
        }

        int primeiroDigito = calcularDigitoVerificador(cpf, 9);
        int segundoDigito = calcularDigitoVerificador(cpf, 10);

        return primeiroDigito == Character.getNumericValue(cpf.charAt(9))
                && segundoDigito == Character.getNumericValue(cpf.charAt(10));
    }

    private int calcularDigitoVerificador(String cpf, int tamanho) {
        int soma = 0;
        for (int indice = 0; indice < tamanho; indice++) {
            soma += Character.getNumericValue(cpf.charAt(indice)) * (tamanho + 1 - indice);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}