
# AS ORDENS 🛠️

Este é o repositório do projeto **AS ORDENS**, uma API REST desenvolvida em Java com Spring Boot para o gerenciamento de ordens de serviço e chamados. O projeto utiliza o banco de dados PostgreSQL e está configurado para ser executado facilmente em containers Docker.

## 🚀 Tecnologias Utilizadas

* **Java 17+** (A aplicação é compilada para Java 17, mas a imagem Docker utiliza JDK 21)
* **Spring Boot** (WebMVC, Data JPA, Validation)
* **PostgreSQL** (Banco de dados relacional)
* **Lombok** (Redução de código boilerplate)
* **Maven** (Gerenciamento de dependências e build)
* **Docker & Docker Compose** (Containerização e orquestração)

## 📋 Pré-requisitos

Para executar este projeto localmente, você precisará ter instalado em sua máquina:

* [Docker](https://www.docker.com/) e [Docker Compose](https://docs.docker.com/compose/) (Recomendado)
* **OU** [Java 17+](https://adoptium.net/) e uma instância do PostgreSQL rodando localmente (Caso não queira utilizar o Docker).

---

## 🐳 Como executar com Docker (Recomendado)

O projeto já possui um arquivo `docker-compose.yml` pré-configurado que sobe tanto o banco de dados PostgreSQL quanto a aplicação Spring Boot de forma automatizada.

1. Clone o repositório ou navegue até a pasta raiz do projeto.
2. Abra o terminal e execute o seguinte comando para construir a imagem e subir os containers:

```bash
docker-compose up -d --build
```

3. A aplicação estará disponível na porta `8080`.
   * **Base URL da API:** `http://localhost:8080`

4. Para parar a execução dos containers, utilize:
```bash
docker-compose down
```

---

## 💻 Como executar localmente (Sem Docker)

Se preferir rodar a aplicação diretamente na sua máquina sem o Docker para a API, siga os passos abaixo:

### 1. Configurar o Banco de Dados
Certifique-se de ter o PostgreSQL rodando localmente (na porta `5432`) com as seguintes credenciais:
* **Host:** `localhost`
* **Porta:** `5432`
* **Database:** `chamados_db`
* **Usuário:** `postgres`
* **Senha:** `2030`

> *Nota: Você pode subir apenas o banco de dados utilizando o comando `docker-compose up -d db`.*

### 2. Configurar Variáveis de Ambiente
Como a aplicação local tentará se conectar à URL do Docker por padrão, você precisará sobrescrever as variáveis de ambiente antes de rodar o projeto, ou ajustá-las temporariamente no `application.properties`/`application.yml`:

```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/chamados_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=2030
```

### 3. Rodar o Projeto com Maven Wrapper
No terminal, na raiz do projeto, execute o comando para compilar e iniciar a aplicação:

**No Linux/macOS:**
```bash
./mvnw clean install -DskipTests
./mvnw spring-boot:run
```

**No Windows:**
```cmd
mvnw.cmd clean install -DskipTests
mvnw.cmd spring-boot:run
```

A API estará acessível em `http://localhost:8080`.

---

## 🏗️ Estrutura do Projeto (Build)

O projeto utiliza um build multi-stage no Docker (`Dockerfile`):
1. **Estágio de Build:** Utiliza a imagem `maven:3.9-eclipse-temurin-21` para baixar as dependências e gerar o pacote `.jar` (pulando os testes para maior agilidade).
2. **Estágio de Execução:** Utiliza a imagem `eclipse-temurin:21-jdk` apenas com o que é necessário para rodar a aplicação, expondo a porta `8080`.

---

## 🤝 Contribuindo

Se desejar contribuir com o projeto, fique à vontade para abrir uma *Issue* ou enviar um *Pull Request*.

## 📄 Licença
Projeto desenvolvido para fins educacionais / institucionais (Grupo Ser Educacional / Uninassau).
