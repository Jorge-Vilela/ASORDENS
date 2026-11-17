# AS ORDENS 🛠️

API REST desenvolvida em Java com Spring Boot para o gerenciamento de ordens de serviço, atendimento de chamados técnicos, equipes, categorias e relatórios gerenciais.

Projeto acadêmico da disciplina de **Back-End Frameworks** — Centro Universitário Maurício de Nassau (UNINASSAU).

---

## 👥 Integrantes

- **Jorge Vilela**
- **André Gomes Moura**
- **Lucas Matheus Pereira da Silva**
- **Bernardo Augusto Franco**
- **Kauã Nyllo**

---

## 🎯 Funcionalidades

- **Autenticação e autorização:** login via JWT (Bearer Token) com controle de acesso por perfis (`ADMIN`, `AGENTE` e `USER`) e senhas criptografadas com BCrypt.
- **Gestão de chamados:** abertura, listagem paginada e filtrada (por status, prioridade, categoria e texto), assunção de atendimento (`puxar`), interações/mensagens, anexos, resolução e avaliação do atendimento.
- **Equipes e categorias:** cadastro e manutenção de equipes técnicas e de categorias de atendimento com SLA (em horas).
- **Usuários e configurações:** gerenciamento de usuários e preferências do sistema.
- **Relatórios:** consulta de indicadores gerenciais de chamados.
- **Tratamento global de erros:** respostas JSON padronizadas para 400, 401, 403, 404, 409 e 500.
- **Monitoramento:** health check via Spring Boot Actuator.

---

## 🚀 Tecnologias

- **Java 17+** e **Spring Boot** (Web MVC, Data JPA, Validation, Security, Actuator)
- **PostgreSQL** (produção/Docker) e **H2** (testes)
- **JWT (`jjwt`)** — autenticação stateless
- **Springdoc OpenAPI / Swagger UI** — documentação interativa
- **JUnit 5 e Mockito** — testes unitários e de integração (MockMvc)
- **Lombok** e **Maven**
- **Docker e Docker Compose**

---

## 🏗️ Arquitetura

A aplicação segue a arquitetura em camadas, organizada em pacotes por camada:

```
Controller → Service → Repository → Spring Data JPA → PostgreSQL
```

```
br.edu.nassau.asordens
├── controller   # endpoints REST (sem regra de negócio)
├── service      # regras de negócio e transações
├── repository   # acesso a dados (Spring Data JPA)
├── model        # entidades JPA e enums
├── dto          # DTOs de entrada e saída
├── exception    # exceções de domínio e GlobalExceptionHandler
└── seguranca    # SecurityConfig, filtro JWT, JwtService
```

---

## 🗄️ Modelagem de entidades e relacionamentos

| Entidade | Descrição | Relacionamentos |
| :--- | :--- | :--- |
| **Usuario** | Solicitantes, agentes e administradores (`id`, `nome`, `email`, `senha`, `perfil`, `ativo`) | `N:1` com `Equipe` |
| **Equipe** | Grupos de atendimento técnico (`id`, `nome`, `descricao`, `ativo`) | `1:N` com `Usuario` e `Chamado` |
| **Categoria** | Classificação dos chamados e SLA (`id`, `nome`, `horas`) | `1:N` com `Chamado` |
| **Chamado** | Entidade central (`id`, `titulo`, `descricao`, `status`, `prioridade`, `dataAbertura`, `atualizadoEm`, `resolvidoEm`) | `N:1` com `Usuario` (solicitante e agente), `Categoria` e `Equipe`; `1:N` bidirecional com `InteracaoChamado` e `AnexoChamado`; `1:1` bidirecional com `AvaliacaoChamado` |
| **InteracaoChamado** | Histórico de mensagens do chamado (`id`, `texto`, `enviadoEm`, `chamado`, `autor`) | `N:1` com `Chamado` e `Usuario` |
| **AnexoChamado** | Arquivos vinculados ao chamado (`id`, `nome`, `tipo`, `tamanho`, `url`, `chamado`) | `N:1` com `Chamado` |
| **AvaliacaoChamado** | Nota e feedback após a resolução (`id`, `nota`, `comentario`, `dataAvaliacao`, `chamado`) | `1:1` com `Chamado` |
| **Configuracao** | Parâmetros e preferências operacionais da plataforma | — |

**Enums:** `Perfil`, `StatusChamado`, `PrioridadeChamado`.

---

## 📋 Pré-requisitos

- [Docker](https://www.docker.com/) e [Docker Compose](https://docs.docker.com/compose/) **(recomendado)**
- **ou** [Java 17+](https://adoptium.net/) e [PostgreSQL](https://www.postgresql.org/) instalados localmente.

---

## ⚙️ Variáveis de ambiente

Configure as variáveis abaixo no ambiente ou em um arquivo `.env` (**não versione valores reais**):

| Variável | Descrição | Exemplo |
| :--- | :--- | :--- |
| `SPRING_DATASOURCE_URL` | URL JDBC do PostgreSQL | `jdbc:postgresql://localhost:5432/chamados_db` |
| `SPRING_DATASOURCE_USERNAME` | Usuário do banco | `seu_usuario` |
| `SPRING_DATASOURCE_PASSWORD` | Senha do banco | `sua_senha` |
| `JWT_SECRET` | Chave usada na assinatura dos tokens JWT | `sua_chave_secreta_jwt_aqui` |

---

## 🐳 Como executar com Docker (recomendado)

Na raiz do projeto (onde está o `docker-compose.yml`):

```bash
docker-compose up -d --build
```

A API ficará disponível em `http://localhost:8080`.

Para encerrar os containers:

```bash
docker-compose down
```

---

## 💻 Como executar localmente (sem Docker)

1. Garanta que o PostgreSQL está rodando na porta `5432` com o banco `chamados_db` criado.
2. Defina as variáveis de ambiente da seção anterior.
3. Na pasta `backend/`, execute com o Maven Wrapper:

**Linux/macOS**

```bash
./mvnw spring-boot:run
```

**Windows**

```bat
mvnw.cmd spring-boot:run
```

---

## 🧪 Como executar os testes

Na pasta `backend/`:

**Linux/macOS**

```bash
./mvnw test
```

**Windows**

```bat
mvnw.cmd test
```

---

## 🔑 Usuários para teste (seed inicial)

Na inicialização (`DataLoader`), os usuários abaixo são criados automaticamente caso o banco esteja vazio:

| Perfil | E-mail | Senha |
| :--- | :--- | :--- |
| `ADMIN` | `admin@asordens.com` | `Admin123` |
| `AGENTE` | `agente@asordens.com` | `Agente123` |

> Credenciais de **demonstração**, usadas apenas para testes locais.

Para acessar endpoints protegidos, faça login e envie o token no cabeçalho:

```
Authorization: Bearer <seu_token_jwt>
```

---

## 📖 Documentação da API (Swagger / OpenAPI)

Com a aplicação em execução:

- **Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

---

## 🔌 Principais endpoints

### Autenticação e monitoramento (públicos)

| Método | Rota | Descrição |
| :--- | :--- | :--- |
| `POST` | `/api/auth/login` | Autentica o usuário e retorna o token JWT |
| `GET` | `/actuator/health` | Status de saúde da aplicação e do banco |

### Chamados (protegidos)

| Método | Rota | Descrição |
| :--- | :--- | :--- |
| `GET` | `/api/chamados` | Lista com paginação, ordenação e filtros (`status`, `prioridade`, `categoriaId`, `busca`) |
| `GET` | `/api/chamados/{id}` | Detalhes de um chamado |
| `POST` | `/api/chamados` | Cria um chamado |
| `PATCH` | `/api/chamados/{id}/assumir` | Atribui o chamado ao agente autenticado e muda o status para `EM_ANDAMENTO` |
| `POST` | `/api/chamados/{id}/interacoes` | Adiciona uma interação/mensagem |
| `PATCH` | `/api/chamados/{id}/finalizar` | Marca o chamado como `RESOLVIDO` |
| `POST` | `/api/chamados/{id}/avaliacao` | Registra a avaliação do atendimento |
| `DELETE` | `/api/chamados/{id}` | Remove um chamado (**somente `ADMIN`**) |

### Demais recursos (protegidos)

| Método | Rota | Descrição |
| :--- | :--- | :--- |
| `GET/POST/PUT/DELETE` | `/api/usuarios` | Gerenciamento de usuários |
| `GET/POST/PUT/DELETE` | `/api/equipes` | Gerenciamento de equipes técnicas |
| `GET/POST/PUT/DELETE` | `/api/categorias` | Gerenciamento de categorias e SLA |
| `GET` | `/api/relatorios/chamados` | Dados consolidados para relatórios |
| `GET/PUT` | `/api/configuracoes` | Consulta e atualização das configurações |

---

## 🚨 Padrão de erros

Todas as falhas retornam JSON no formato:

```json
{
  "timestamp": "2026-11-16T12:00:00",
  "status": 404,
  "erro": "Chamado não encontrado",
  "detalhes": null
}
```

| Status | Situação |
| :--- | :--- |
| `400` | Dados inválidos (Bean Validation) |
| `401` | Credenciais inválidas ou token ausente |
| `403` | Perfil sem permissão |
| `404` | Recurso inexistente |
| `409` | Conflito de regra de negócio |
| `500` | Erro interno inesperado |

---

## 📄 Licença

Projeto acadêmico desenvolvido para a disciplina de Back-End Frameworks — Centro Universitário Maurício de Nassau (UNINASSAU).
