/**
 * entrar.js
 * Lógica exclusiva da página entrar.html.
 * Depende das funções definidas em utilitarios.js.
 */

document.addEventListener("DOMContentLoaded", () => {
  inicializarAlternadoresSenha();
  inicializarFormularioEntrar();
});

function inicializarFormularioEntrar() {
  const formulario = document.getElementById("formulario-entrar");
  const botaoEnviar = formulario.querySelector("button[type='submit']");

  formulario.addEventListener("submit", (evento) => {
    evento.preventDefault();
    esconderAlerta();

    const email = document.getElementById("entrar-email");
    const senha = document.getElementById("entrar-senha");
    const lembrar = document.getElementById("entrar-lembrar");
    let valido = true;

    if (!email.value.trim()) {
      definirErroCampo("entrar-email", "Informe seu e-mail.");
      valido = false;
    } else if (!validarEmail(email.value)) {
      definirErroCampo("entrar-email", "E-mail inválido.");
      valido = false;
    } else {
      definirErroCampo("entrar-email", null);
    }

    if (!senha.value) {
      definirErroCampo("entrar-senha", "Informe sua senha.");
      valido = false;
    } else {
      definirErroCampo("entrar-senha", null);
    }

    if (!valido) {
      mostrarAlerta("Corrija os campos destacados para continuar.", "erro");
      return;
    }

    botaoEnviar.disabled = true;
    botaoEnviar.textContent = "Entrando...";

    fetch("/api/auth/entrar", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        email: email.value.trim(),
        senha: senha.value,
        lembrar: lembrar.checked,
      }),
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
        window.location.href = "/painel";
      })
      .catch((erro) => {
        mostrarAlerta(erro.message, "erro");
        botaoEnviar.disabled = false;
        botaoEnviar.textContent = "Entrar";
      });
  });
}