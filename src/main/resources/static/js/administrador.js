/**
 * administrador.js
 * Lógica exclusiva da página administrador.html (login de administrador).
 * Depende das funções definidas em utilitarios.js.
 */

document.addEventListener("DOMContentLoaded", () => {
  inicializarAlternadoresSenha();
  inicializarFormularioAdministrador();
});

function inicializarFormularioAdministrador() {
  const formulario = document.getElementById("formulario-administrador");
  const botaoEnviar = formulario.querySelector("button[type='submit']");

  formulario.addEventListener("submit", (evento) => {
    evento.preventDefault();
    esconderAlerta();

    const email = document.getElementById("administrador-email");
    const senha = document.getElementById("administrador-senha");
    let valido = true;

    if (!email.value.trim()) {
      definirErroCampo("administrador-email", "Informe o e-mail.");
      valido = false;
    } else if (!validarEmail(email.value)) {
      definirErroCampo("administrador-email", "E-mail inválido.");
      valido = false;
    } else {
      definirErroCampo("administrador-email", null);
    }

    if (!senha.value) {
      definirErroCampo("administrador-senha", "Informe a senha.");
      valido = false;
    } else {
      definirErroCampo("administrador-senha", null);
    }

    if (!valido) {
      mostrarAlerta("Corrija os campos destacados para continuar.", "erro");
      return;
    }

    botaoEnviar.disabled = true;
    botaoEnviar.textContent = "Entrando...";

    fetch("/api/auth/administrador/entrar", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email: email.value.trim(), senha: senha.value, lembrar: false }),
    })
      .then(async (resposta) => {
        const corpo = await resposta.json();
        if (!resposta.ok || !corpo.sucesso) {
          throw new Error(corpo.mensagem || "Não foi possível entrar.");
        }
        return corpo;
      })
      .then((corpo) => {
        mostrarAlerta(corpo.mensagem, "sucesso");
        window.location.href = "/administrador/painel";
      })
      .catch((erro) => {
        mostrarAlerta(erro.message, "erro");
        botaoEnviar.disabled = false;
        botaoEnviar.textContent = "Entrar como administrador";
      });
  });
}