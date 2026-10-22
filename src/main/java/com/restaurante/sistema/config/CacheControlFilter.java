package com.restaurante.sistema.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Impede que o navegador cacheie respostas de endpoints da API
 * (/api/**). Sem isso, um GET repetido para a mesma URL (ex:
 * /api/usuarios/me) pode ser respondido pelo cache do navegador em
 * vez de ir ao servidor de novo — o que causa um bug sutil e
 * confuso: ao trocar de conta na mesma aba/sessão de testes, a tela
 * pode continuar mostrando dados da conta anterior, mesmo com o
 * backend já retornando os dados corretos.
 *
 * Só afeta rotas de API (/api/**) — arquivos estáticos (CSS, JS,
 * imagens) continuam sendo cacheados normalmente pelo navegador, o
 * que é bom para performance e não tem o mesmo risco de "dado
 * desatualizado".
 */
@Component
public class CacheControlFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest httpRequest
                && response instanceof HttpServletResponse httpResponse
                && httpRequest.getRequestURI().startsWith("/api/")) {

            httpResponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
            httpResponse.setHeader("Pragma", "no-cache");
        }

        chain.doFilter(request, response);
    }
}