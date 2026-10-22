/**
 * cadastro.js
 * Lógica exclusiva da página cadastro.html.
 * Depende das funções definidas em utilitarios.js.
 */

document.addEventListener("DOMContentLoaded", () => {
  inicializarAlternadoresSenha();
  inicializarMedidorForcaSenha("cadastro-senha");
  inicializarMascaraCPF("cadastro-cpf");
  inicializarSeletorTipoConta();
  inicializarFormularioCadastro();
});

/* ============================================================
 * SELETOR DE TIPO DE CONTA
 * Ao trocar a opção marcada, esconde todos os blocos de campos
 * condicionais e mostra só o que corresponde à opção escolhida
 * (se for "USUARIO", nenhum bloco extra é exibido).
 * ============================================================ */
function inicializarSeletorTipoConta() {
  const opcoes = document.querySelectorAll('input[name="tipoConta"]');

  opcoes.forEach((opcao) => {
    opcao.addEventListener("change", () => {
      document.querySelectorAll(".campos-condicionais").forEach((bloco) => {
        bloco.hidden = bloco.dataset.camposPara !== opcao.value;
      });
    });
  });
}

function obterTipoContaSelecionado() {
  return document.querySelector('input[name="tipoConta"]:checked').value;
}

function inicializarFormularioCadastro() {
  const formulario = document.getElementById("formulario-cadastro");
  const botaoEnviar = formulario.querySelector("button[type='submit']");

  formulario.addEventListener("submit", (evento) => {
    evento.preventDefault();
    esconderAlerta();

    const tipoConta = obterTipoContaSelecionado();

    const nome = document.getElementById("cadastro-nome");
    const email = document.getElementById("cadastro-email");
    const cpf = document.getElementById("cadastro-cpf");
    const dataNascimento = document.getElementById("cadastro-data-nascimento");
    const senha = document.getElementById("cadastro-senha");
    const confirmarSenha = document.getElementById("cadastro-confirmar-senha");

    let valido = true;

    // ---- Validação dos campos comuns a qualquer tipo de conta ----
    if (!nome.value.trim()) {
      definirErroCampo("cadastro-nome", "Informe seu nome completo.");
      valido = false;
    } else {
      definirErroCampo("cadastro-nome", null);
    }

    if (!email.value.trim()) {
      definirErroCampo("cadastro-email", "Informe seu e-mail.");
      valido = false;
    } else if (!validarEmail(email.value)) {
      definirErroCampo("cadastro-email", "E-mail inválido.");
      valido = false;
    } else {
      definirErroCampo("cadastro-email", null);
    }

    if (!validarCPF(cpf.value)) {
      definirErroCampo("cadastro-cpf", "CPF inválido.");
      valido = false;
    } else {
      definirErroCampo("cadastro-cpf", null);
    }

    if (!dataNascimento.value) {
      definirErroCampo("cadastro-data-nascimento", "Informe sua data de nascimento.");
      valido = false;
    } else if (new Date(dataNascimento.value) > new Date()) {
      definirErroCampo("cadastro-data-nascimento", "A data de nascimento não pode ser no futuro.");
      valido = false;
    } else {
      definirErroCampo("cadastro-data-nascimento", null);
    }

    if (calcularPontuacaoSenha(senha.value) < 2) {
      definirErroCampo("cadastro-senha", "Use ao menos 8 caracteres, com letras e números.");
      valido = false;
    } else {
      definirErroCampo("cadastro-senha", null);
    }

    if (confirmarSenha.value !== senha.value || !confirmarSenha.value) {
      definirErroCampo("cadastro-confirmar-senha", "As senhas não coincidem.");
      valido = false;
    } else {
      definirErroCampo("cadastro-confirmar-senha", null);
    }

    // ---- Validação + montagem dos campos extras, conforme o tipo escolhido ----
    const camposExtras = {};

    if (tipoConta === "PACIENTE") {
      const restricoes = document.getElementById("cadastro-restricoes");
      const prontuario = document.getElementById("cadastro-prontuario");

      if (!restricoes.value.trim()) {
        definirErroCampo("cadastro-restricoes", "Informe as restrições alimentares.");
        valido = false;
      } else {
        definirErroCampo("cadastro-restricoes", null);
      }
      if (!prontuario.value) {
        definirErroCampo("cadastro-prontuario", "Informe o número de prontuário.");
        valido = false;
      } else {
        definirErroCampo("cadastro-prontuario", null);
      }

      camposExtras.restricoesAlimentares = restricoes.value.trim();
      camposExtras.numeroProntuario = Number(prontuario.value);
    }

    if (tipoConta === "ALUNO" || tipoConta === "ESTAGIARIO") {
      const prefixo = tipoConta === "ALUNO" ? "aluno" : "estagiario";
      const curso = document.getElementById(`cadastro-${prefixo}-curso`);
      const matricula = document.getElementById(`cadastro-${prefixo}-matricula`);
      const instituicao = document.getElementById(`cadastro-${prefixo}-instituicao`);

      if (!curso.value.trim()) {
        definirErroCampo(`cadastro-${prefixo}-curso`, "Informe o curso.");
        valido = false;
      } else {
        definirErroCampo(`cadastro-${prefixo}-curso`, null);
      }
      if (!instituicao.value.trim()) {
        definirErroCampo(`cadastro-${prefixo}-instituicao`, "Informe a instituição de ensino.");
        valido = false;
      } else {
        definirErroCampo(`cadastro-${prefixo}-instituicao`, null);
      }
      if (!matricula.value) {
        definirErroCampo(`cadastro-${prefixo}-matricula`, "Informe a matrícula.");
        valido = false;
      } else {
        definirErroCampo(`cadastro-${prefixo}-matricula`, null);
      }

      camposExtras.curso = curso.value.trim();
      camposExtras.instituicao = instituicao.value.trim();
      camposExtras.matricula = Number(matricula.value);
    }

    if (tipoConta === "ESTAGIARIO") {
      const dataInicio = document.getElementById("cadastro-estagiario-data-inicio");
      const dataFim = document.getElementById("cadastro-estagiario-data-fim");

      if (!dataInicio.value || !dataFim.value) {
        const idCampo = !dataInicio.value ? "cadastro-estagiario-data-inicio" : "cadastro-estagiario-data-fim";
        definirErroCampo(idCampo, "Informe o período do estágio.");
        valido = false;
      } else if (new Date(dataFim.value) < new Date(dataInicio.value)) {
        definirErroCampo("cadastro-estagiario-data-fim", "Deve ser depois da data de início.");
        valido = false;
      } else {
        definirErroCampo("cadastro-estagiario-data-inicio", null);
        definirErroCampo("cadastro-estagiario-data-fim", null);
      }

      camposExtras.dataInicioEstagio = dataInicio.value;
      camposExtras.dataFimEstagio = dataFim.value;
    }

    if (tipoConta === "NUTRICIONISTA") {
      const registro = document.getElementById("cadastro-nutricionista-registro");

      if (!registro.value.trim()) {
        definirErroCampo("cadastro-nutricionista-registro", "Informe o registro profissional.");
        valido = false;
      } else {
        definirErroCampo("cadastro-nutricionista-registro", null);
      }

      camposExtras.registroProfissional = registro.value.trim();
    }

    if (!valido) {
      mostrarAlerta("Corrija os campos destacados para continuar.", "erro");
      return;
    }

    botaoEnviar.disabled = true;
    botaoEnviar.textContent = "Criando conta...";

    fetch("/api/auth/cadastro", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        nome: nome.value.trim(),
        email: email.value.trim(),
        cpf: cpf.value.replace(/\D/g, ""),
        dataNascimento: dataNascimento.value,
        tipoConta: tipoConta,
        senha: senha.value,
        confirmarSenha: confirmarSenha.value,
        ...camposExtras,
      }),
    })
      .then(async (resposta) => {
        const corpo = await resposta.json();
        if (!resposta.ok || !corpo.sucesso) {
          throw new Error(corpo.mensagem || "Não foi possível concluir o cadastro.");
        }
        return corpo;
      })
      .then((corpo) => {
        mostrarAlerta(corpo.mensagem, "sucesso");
        formulario.reset();
        document.querySelectorAll("[data-barra]").forEach((barra) => {
          barra.style.background = "var(--cor-borda)";
        });
        document.querySelector("[data-rotulo-forca]").textContent = "Digite uma senha";
        // Volta a exibir só os campos de "Usuário comum" (opção padrão após o reset)
        document.querySelectorAll(".campos-condicionais").forEach((bloco) => (bloco.hidden = true));
      })
      .catch((erro) => mostrarAlerta(erro.message, "erro"))
      .finally(() => {
        botaoEnviar.disabled = false;
        botaoEnviar.textContent = "Criar conta";
      });
  });
}