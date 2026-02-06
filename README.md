# TeamFoundry

Bem-vindo ao **TeamFoundry**. Esta aplicação é uma plataforma dedicada à gestão e recrutamento de equipas, permitindo a administração de perfis de utilizadores, competências, currículos e certificados. O sistema é composto por um backend robusto em Spring Boot e um frontend moderno em React.

## Visão Geral

-   **Backend**: Gerencia a lógica de negócios, autenticação (JWT e OAuth2 Google), upload de arquivos (Cloudinary) e persistência de dados.
-   **Frontend**: Interface de utilizador responsiva para interação com a plataforma.

## Tecnologias Utilizadas

### Backend
-   **Java 21**
-   **Spring Boot 3.5.7** (Web, Security, Data JPA, Mail, Validation, OAuth2 Client)
-   **PostgreSQL** (Banco de dados produção/dev)
-   **H2 Database** (Para testes)
-   **Flyway** (Migração de banco de dados, desativado por padrão)
-   **JJWT** (Autenticação via Token)
-   **Cloudinary** (Armazenamento de mídia)

### Frontend
-   **React 19**
-   **Vite**
-   **TailwindCSS v4** & **daisyUI**
-   **Recharts** (Gráficos)
-   **Jest** (Testes)

---

## Pré-requisitos

Antes de começar, certifique-se de ter instalado na sua máquina:

1.  **Java JDK 21**: [Download JDK 21](https://www.oracle.com/java/technologies/downloads/#java21)
2.  **Node.js** (LTS recomendado): [Download Node.js](https://nodejs.org/)
3.  **PostgreSQL**: [Download PostgreSQL](https://www.postgresql.org/download/)
4.  **Git**: [Download Git](https://git-scm.com/)

---

## Guia de Instalação e Execução (Local)

Siga os passos abaixo para configurar o ambiente de desenvolvimento.

### 1. Clonar o Repositório

```bash
git clone https://github.com/LuisAssis26/teamfoundry-workforce-management
cd teamfoundry-workforce-management
```

### 2. Configuração do Banco de Dados

Crie uma base de dados PostgreSQL chamada `teamfoundry`. Você pode fazer isso via pgAdmin ou linha de comando.

### 3. Configuração do Backend

1.  Navegue até a pasta do backend:
    ```bash
    cd backend
    ```

2.  Configure as variáveis de ambiente. Copie o arquivo de exemplo `.env.example` para um novo arquivo chamado `.env`:
    -   **Windows (PowerShell)**: `Copy-Item .env.example .env`
    -   **Linux/Mac**: `cp .env.example .env`

3.  Abra o arquivo `.env` criado e preencha as variáveis com os seus dados locais. As mais importantes para rodar localmente são:

    ```properties
    # Configurações do Banco de Dados
    SPRING_DATASOURCE_URL_PROD="jdbc:postgresql://localhost:5432/teamfoundry"
    SPRING_DATASOURCE_USERNAME_PROD="seu_usuario_postgres"
    SPRING_DATASOURCE_PASSWORD_PROD="sua_senha_postgres"

    # JWT (Gere uma string segura)
    JWT_SECRET="uma-chave-muito-secreta-com-pelo-menos-32-caracteres"

    # Cloudinary (necessário para uploads)
    CLOUDINARY_URL="cloudinary://api_key:api_secret@cloud_name"

    # Google OAuth (necessário para login com Google)
    GOOGLE_CLIENT_ID="seu-client-id-google"
    GOOGLE_CLIENT_SECRET="seu-client-secret-google"
    ```
    > **Nota**: Para o envio de e-mails funcionar, você precisará configurar as credenciais SMTP (ex: Gmail ou Brevo).

4.  Execute a aplicação:
    -   **Windows**:
        ```bash
        .\gradlew bootRun
        ```
    -   **Linux/Mac**:
        ```bash
        ./gradlew bootRun
        ```

O backend iniciará na porta `8080` (padrão).

### 4. Configuração do Frontend

1.  Abra um novo terminal e navegue até a pasta do frontend:
    ```bash
    cd frontend
    ```

2.  Instale as dependências:
    ```bash
    npm install
    ```

3.  Configure as variáveis de ambiente. Crie um arquivo `.env` na raiz do frontend (ou renomeie `.env.example`) e defina:

    ```properties
    VITE_API_BASE_URL=http://localhost:8080/api
    VITE_CLOUDINARY_BASE_URL=https://res.cloudinary.com/seu-cloud-name/image/upload/
    ```

4.  Execute o servidor de desenvolvimento:
    ```bash
    npm run dev
    ```

O frontend estará disponível em `http://localhost:517x`.

---

## Estrutura do Projeto

```
teamfoundry-workforce-management/
├── backend/               # Código fonte da API Java/Spring Boot
│   ├── src/
│   ├── build.gradle
│   └── Dockerfile
└── frontend/              # Código fonte da Interface React
    ├── src/
    ├── package.json
    └── vite.config.js
```

## Contribuindo

1.  Faça um Fork do projeto.
2.  Crie uma Branch para sua Feature (`git checkout -b feature/MinhaFeature`).
3.  Faça o Commit (`git commit -m 'Adicionando MinhaFeature'`).
4.  Faça o Push (`git push origin feature/MinhaFeature`).
5.  Abra um Pull Request.
