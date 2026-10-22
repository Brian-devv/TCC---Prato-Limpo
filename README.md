# TCC --- Prato Limpo

Sistema de gestão para restaurante corporativo, com foco em controle de desperdício e cadastro de usuários.

## Visão geral

Este projeto foi desenvolvido como TCC e possui uma primeira versão com CRUD de usuários, autenticação, perfil, alteração de senha e painel administrativo.

## Tecnologias

- Java 21
- Spring Boot 4.1.1
- Spring Data JPA
- Spring Security
- PostgreSQL
- Thymeleaf
- Maven

## Requisitos

Antes de rodar o projeto, certifique-se de ter instalado:

- JDK 21+
- Maven
- PostgreSQL
- Git

## Configuração do banco

Crie um banco PostgreSQL local com o nome:

```sql
CREATE DATABASE sistema_restaurante_db;
```

Depois configure as credenciais no arquivo:

```bash
src/main/resources/application.properties
```

Exemplo:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/sistema_restaurante_db
spring.datasource.username=postgres
spring.datasource.password=sua_senha
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

## Usuário administrador padrão

Ao iniciar a aplicação, se não existir nenhum administrador cadastrado, o sistema cria automaticamente um usuário padrão.

Credenciais padrão:

- E-mail: `admin@restaurante.com`
- Senha: `Admin@123`

Você pode alterar as credenciais no `application.properties`:

```properties
admin.email=admin@restaurante.com
admin.senha-inicial=Admin@123
```

## Como rodar o projeto

No terminal, na raiz do projeto:

```bash
./mvnw clean install
./mvnw spring-boot:run
```

No Windows PowerShell:

```powershell
./mvnw.cmd clean install
./mvnw.cmd spring-boot:run
```

Depois acesse:

- http://localhost:8080
- Página de cadastro: http://localhost:8080/cadastro
- Login: http://localhost:8080/entrar
- Painel administrativo: http://localhost:8080/administrador

## Primeira versão

Esta é a primeira versão do projeto e inclui:

- cadastro de usuários
- autenticação
- login e logout
- visualização e atualização de perfil
- alteração de senha
- exclusão de conta
- painel administrativo
- listagem e cadastro de funcionários

## Status

Este projeto está em evolução. Futuramente serão adicionadas novas funcionalidades e módulos do sistema.

## Autor

Davi Tales e Brian Ramos
