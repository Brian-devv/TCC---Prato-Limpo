/**
 * cadastro.js
 * Lógica exclusiva da página cadastro.html.
 * Depende das funções definidas em utilitarios.js.
 */

document.addEventListener("DOMContentLoaded", () => {
  inicializarAlternadoresSenha();
  inicializarMedidorForcaSenha("cadastro-senha");
  inicializarMascaraCPF("cadastro-cpf");
  inicializarFormularioCadastro();
});

function inicializarFormularioCadastro() {
  const formulario = document.getElementById("formulario-cadastro");
  const botaoEnviar = formulario.querySelector("button[type='submit']");

  formulario.addEventListener("submit", (evento) => {
    evento.preventDefault();
    esconderAlerta();

    const nome = document.getElementById("cadastro-nome");
    const email = document.getElementById("cadastro-email");
    const cpf = document.getElementById("cadastro-cpf");
    const dataNascimento = document.getElementById("cadastro-data-nascimento");
    const senha = document.getElementById("cadastro-senha");
    const confirmarSenha = document.getElementById("cadastro-confirmar-senha");

    let valido = true;

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
        cpf: cpf.value.replace(/\D/g, ""), // envia só os dígitos, sem pontuação
        dataNascimento: dataNascimento.value, // já vem como "aaaa-mm-dd" do <input type="date">
        senha: senha.value,
        confirmarSenha: confirmarSenha.value,
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
      })
      .catch((erro) => mostrarAlerta(erro.message, "erro"))
      .finally(() => {
        botaoEnviar.disabled = false;
        botaoEnviar.textContent = "Criar conta";
      });
  });
}