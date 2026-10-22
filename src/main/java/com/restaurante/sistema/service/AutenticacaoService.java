package com.restaurante.sistema.service;

import com.restaurante.sistema.dto.CadastroFuncionarioDTO;
import com.restaurante.sistema.dto.CadastroUsuarioDTO;
import com.restaurante.sistema.dto.LoginDTO;
import com.restaurante.sistema.exception.RegraNegocioException;
import com.restaurante.sistema.model.*;
import com.restaurante.sistema.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AutenticacaoService {

    private final UsuarioRepository usuarioRepository;
    private final EmailRepository emailRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final GestorRepository gestorRepository;
    private final PacienteRepository pacienteRepository;
    private final AlunoRepository alunoRepository;
    private final EstagiarioRepository estagiarioRepository;
    private final NutricionistaRepository nutricionistaRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Regra do cadastro.html: qualquer pessoa pode criar uma conta,
     * escolhendo entre USUARIO, PACIENTE, ALUNO, ESTAGIARIO ou
     * NUTRICIONISTA (dto.getTipoConta()).
     *
     * A validação comum (senhas coincidem, CPF válido, e-mail livre)
     * acontece uma vez só; depois, o "switch" cria o subtipo certo,
     * validando só os campos extras daquele tipo específico.
     */
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

        Usuario usuarioCriado = switch (dto.getTipoConta()) {

            case "USUARIO" -> {
                Usuario usuario = preencherDadosComuns(
                        new Usuario(), dto.getNome(), cpfSomenteDigitos, dto.getDataNascimento(), dto.getSenha(), "USUARIO");
                anexarEmail(usuario, endereco, dominio);
                yield usuarioRepository.save(usuario);
            }

            case "PACIENTE" -> {
                if (isEmVazio(dto.getRestricoesAlimentares())) {
                    throw new RegraNegocioException("Informe as restrições alimentares.");
                }
                if (dto.getNumeroProntuario() == null) {
                    throw new RegraNegocioException("Informe o número de prontuário.");
                }
                Paciente paciente = preencherDadosComuns(
                        new Paciente(), dto.getNome(), cpfSomenteDigitos, dto.getDataNascimento(), dto.getSenha(), "PACIENTE");
                paciente.setRestricoesAlimentares(dto.getRestricoesAlimentares());
                paciente.setNumeroProntuario(dto.getNumeroProntuario());
                anexarEmail(paciente, endereco, dominio);
                yield pacienteRepository.save(paciente);
            }

            case "ALUNO" -> {
                if (isEmVazio(dto.getCurso()) || isEmVazio(dto.getInstituicao())) {
                    throw new RegraNegocioException("Informe o curso e a instituição de ensino.");
                }
                if (dto.getMatricula() == null) {
                    throw new RegraNegocioException("Informe a matrícula.");
                }
                Aluno aluno = preencherDadosComuns(
                        new Aluno(), dto.getNome(), cpfSomenteDigitos, dto.getDataNascimento(), dto.getSenha(), "ALUNO");
                aluno.setCurso(dto.getCurso());
                aluno.setInstituicao(dto.getInstituicao());
                aluno.setMatricula(dto.getMatricula());
                anexarEmail(aluno, endereco, dominio);
                yield alunoRepository.save(aluno);
            }

            case "ESTAGIARIO" -> {
                if (isEmVazio(dto.getCurso()) || isEmVazio(dto.getInstituicao())) {
                    throw new RegraNegocioException("Informe o curso e a instituição de ensino.");
                }
                if (dto.getMatricula() == null) {
                    throw new RegraNegocioException("Informe a matrícula.");
                }
                if (dto.getDataInicioEstagio() == null || dto.getDataFimEstagio() == null) {
                    throw new RegraNegocioException("Informe o período do estágio.");
                }
                if (dto.getDataFimEstagio().isBefore(dto.getDataInicioEstagio())) {
                    throw new RegraNegocioException("A data de término do estágio deve ser depois da data de início.");
                }
                Estagiario estagiario = preencherDadosComuns(
                        new Estagiario(), dto.getNome(), cpfSomenteDigitos, dto.getDataNascimento(), dto.getSenha(), "ESTAGIARIO");
                estagiario.setCurso(dto.getCurso());
                estagiario.setInstituicao(dto.getInstituicao());
                estagiario.setMatricula(dto.getMatricula());
                estagiario.setDataInicioEstagio(dto.getDataInicioEstagio());
                estagiario.setDataFimEstagio(dto.getDataFimEstagio());
                anexarEmail(estagiario, endereco, dominio);
                yield estagiarioRepository.save(estagiario);
            }

            case "NUTRICIONISTA" -> {
                if (isEmVazio(dto.getRegistroProfissional())) {
                    throw new RegraNegocioException("Informe o registro profissional.");
                }
                Nutricionista nutricionista = preencherDadosComuns(
                        new Nutricionista(), dto.getNome(), cpfSomenteDigitos, dto.getDataNascimento(), dto.getSenha(), "NUTRICIONISTA");
                nutricionista.setRegistroProfissional(dto.getRegistroProfissional());
                anexarEmail(nutricionista, endereco, dominio);
                yield nutricionistaRepository.save(nutricionista);
            }

            default -> throw new RegraNegocioException("Tipo de conta inválido.");
        };

        return usuarioCriado;
    }

    /**
     * Regra da caixa "Cadastrar funcionário ou gestor", no painel
     * administrativo. dto.getTipoConta() decide entre "FUNCIONARIO"
     * (com cargo/setor) ou "GESTOR" (sem cargo/setor, só registro
     * profissional).
     */
    @Transactional
    public Usuario cadastrarColaborador(CadastroFuncionarioDTO dto) {
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

        if (isEmVazio(dto.getRegistroProfissional())) {
            throw new RegraNegocioException("Informe o registro profissional.");
        }

        Usuario usuarioCriado;

        if ("GESTOR".equals(dto.getTipoConta())) {
            Gestor gestor = preencherDadosComuns(
                    new Gestor(), dto.getNome(), cpfSomenteDigitos, dto.getDataNascimento(), dto.getSenha(), "GESTOR");
            gestor.setRegistroProfissional(dto.getRegistroProfissional());
            anexarEmail(gestor, endereco, dominio);
            usuarioCriado = gestorRepository.save(gestor);

        } else if ("FUNCIONARIO".equals(dto.getTipoConta())) {
            if (isEmVazio(dto.getCargo()) || isEmVazio(dto.getSetor())) {
                throw new RegraNegocioException("Selecione o cargo e o setor.");
            }
            Funcionario funcionario = preencherDadosComuns(
                    new Funcionario(), dto.getNome(), cpfSomenteDigitos, dto.getDataNascimento(), dto.getSenha(), "FUNCIONARIO");
            funcionario.setCargo(dto.getCargo());
            funcionario.setSetor(dto.getSetor());
            funcionario.setRegistroProfissional(dto.getRegistroProfissional());
            anexarEmail(funcionario, endereco, dominio);
            usuarioCriado = funcionarioRepository.save(funcionario);

        } else {
            throw new RegraNegocioException("Tipo de cadastro inválido.");
        }

        return usuarioCriado;
    }

    /**
     * Regra do entrar.html: localiza o usuário pelo e-mail e confere a senha.
     * Funciona para QUALQUER subtipo (Usuario, Paciente, Aluno, Funcionario,
     * Gestor, Administrador...) porque a consulta é feita na entidade base.
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

    /** Regra do administrador.html: mesma checagem, mas exige tipoPerfil = ADMINISTRADOR. */
    @Transactional(readOnly = true)
    public Usuario autenticarAdministrador(LoginDTO dto) {
        Usuario usuario = autenticar(dto);

        if (!"ADMINISTRADOR".equals(usuario.getTipoPerfil())) {
            throw new RegraNegocioException("E-mail ou senha inválidos.");
        }

        return usuario;
    }

    /**
     * Preenche os campos comuns a QUALQUER subtipo de Usuario (nome,
     * cpf, data de nascimento, senha já com hash, status, etc.).
     * Reaproveitado por todos os "cases" do switch acima — evita
     * repetir essas 6 linhas para cada um dos 7 tipos de conta que
     * o sistema hoje sabe criar.
     */
    private <T extends Usuario> T preencherDadosComuns(
            T usuario, String nome, String cpfSomenteDigitos, LocalDate dataNascimento, String senhaEmTextoPuro, String tipoPerfil) {
        usuario.setNome(nome);
        usuario.setCpf(cpfSomenteDigitos);
        usuario.setDataNascimento(dataNascimento);
        usuario.setSenha(passwordEncoder.encode(senhaEmTextoPuro)); // nunca salvar senha em texto puro
        usuario.setTipoPerfil(tipoPerfil);
        usuario.setStatus("ATIVO");
        usuario.setDataCriacaoConta(LocalDate.now());
        usuario.setDataAtualizacao(LocalDateTime.now());
        return usuario;
    }

    /** Cria e associa o Email (relação 1:1) a qualquer subtipo de Usuario. */
    private void anexarEmail(Usuario usuario, String endereco, String dominio) {
        Email email = new Email();
        email.setEndereco(endereco);
        email.setDominio(dominio);
        email.setUsuario(usuario);
        usuario.setEmail(email);
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

    private boolean isEmVazio(String texto) {
        return texto == null || texto.isBlank();
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