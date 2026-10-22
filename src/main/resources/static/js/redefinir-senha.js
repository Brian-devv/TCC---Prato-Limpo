/**
 * redefinir-senha.js
 * Lógica exclusiva da página redefinir-senha.html.
 * Depende das funções definidas em utilitarios.js.
 */

document.addEventListener("DOMContentLoaded", () => {
  inicializarFormularioRedefinir();
});

function inicializarFormularioRedefinir() {
  const formulario = document.getElementById("formulario-redefinir");
  const botaoEnviar = formulario.querySelector("button[type='submit']");

  formulario.addEventListener("submit", (evento) => {
    evento.preventDefault();
    esconderAlerta();

    const email = document.getElementById("redefinir-email");
    let valido = true;

    if (!email.value.trim()) {
      definirErroCampo("redefinir-email", "Informe seu e-mail.");
      valido = false;
    } else if (!validarEmail(email.value)) {
      definirErroCampo("redefinir-email", "E-mail inválido.");
      valido = false;
    } else {
      definirErroCampo("redefinir-email", null);
    }

    if (!valido) {
      mostrarAlerta("Corrija os campos destacados para continuar.", "erro");
      return;
    }

    botaoEnviar.disabled = true;
    botaoEnviar.textContent = "Enviando...";

    fetch("/api/auth/redefinir-senha/solicitar", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email: email.value.trim() }),
    })
      .then(async (resposta) => {
        const corpo = await resposta.json();
        if (!resposta.ok || !corpo.sucesso) {
          throw new Error(corpo.mensagem || "Não foi possível enviar o link.");
        }
        return corpo;
      })
      .then((corpo) => {
        mostrarAlerta(corpo.mensagem, "sucesso");
        formulario.reset();
      })
      .catch((erro) => mostrarAlerta(erro.message, "erro"))
      .finally(() => {
        botaoEnviar.disabled = false;
        botaoEnviar.textContent = "Enviar link de redefinição";
      });
  });
}