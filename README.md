# TeamFoundry

Welcome to **TeamFoundry**. This application is a platform dedicated to team management and recruitment, allowing the administration of user profiles, skills, resumes, and certificates. The system consists of a robust backend in Spring Boot and a modern frontend in React.

## Overview

-   **Backend**: Manages business logic, authentication (JWT and Google OAuth2), file uploads (Cloudinary), and data persistence.
-   **Frontend**: Responsive user interface for interacting with the platform.

## Technologies Used

### Backend
-   **Java 21**
-   **Spring Boot 3.5.7** (Web, Security, Data JPA, Mail, Validation, OAuth2 Client)
-   **PostgreSQL** (Production/Dev Database)
-   **H2 Database** (For Tests)
-   **Flyway** (Database Migration, disabled by default)
-   **JJWT** (Token-based Authentication)
-   **Cloudinary** (Media Storage)

### Frontend
-   **React 19**
-   **Vite**
-   **TailwindCSS v4** & **daisyUI**
-   **Recharts** (Charts)
-   **Jest** (Testing)

---

## Prerequisites

Before getting started, ensure you have the following installed on your machine:

1.  **Java JDK 21**: [Download JDK 21](https://www.oracle.com/java/technologies/downloads/#java21)
2.  **Node.js** (LTS recommended): [Download Node.js](https://nodejs.org/)
3.  **PostgreSQL**: [Download PostgreSQL](https://www.postgresql.org/download/)
4.  **Git**: [Download Git](https://git-scm.com/)

---

## Installation and Execution Guide (Local)

Follow the steps below to configure the development environment.

### 1. Clone the Repository

```bash
git clone https://github.com/LuisAssis26/teamfoundry-workforce-management
cd teamfoundry-workforce-management
```

### 2. Database Configuration

Create a PostgreSQL database named `teamfoundry`. You can do this via pgAdmin or the command line.

### 3. Backend Configuration

1.  Navigate to the backend folder:
    ```bash
    cd backend
    ```
    
2.  Configure the environment variables. Copy the example file `.env.example` to a new file named `.env`:

3.  Open the created `.env` file and fill in the variables with your local data. The most important ones for running locally are:

    ```properties
    # Database Configuration
    SPRING_DATASOURCE_URL_PROD="jdbc:postgresql://localhost:5432/teamfoundry"
    SPRING_DATASOURCE_USERNAME_PROD="your_postgres_user"
    SPRING_DATASOURCE_PASSWORD_PROD="your_postgres_password"

    # JWT (Generate a secure string)
    JWT_SECRET="a-very-secret-key-with-at-least-32-characters"

    # Cloudinary (required for uploads)
    CLOUDINARY_URL="cloudinary://api_key:api_secret@cloud_name"

    # Google OAuth (required for Google login)
    GOOGLE_CLIENT_ID="your-google-client-id"
    GOOGLE_CLIENT_SECRET="your-google-client-secret"
    ```
    > **Note**: For email sending to work, you will need to configure SMTP credentials (e.g., Gmail or Brevo).

4.  Run the application:
    -   **Windows**:
        ```bash
        .\gradlew bootRun
        ```
    -   **Linux/Mac**:
        ```bash
        ./gradlew bootRun
        ```

The backend will start on port `8080` (default).

### 4. Frontend Configuration

1.  Open a new terminal and navigate to the frontend folder:
    ```bash
    cd frontend
    ```

2.  Install dependencies:
    ```bash
    npm install
    ```

3.  Configure environment variables. Create a `.env` file in the frontend root (or rename `.env.example`) and define:

    ```properties
    VITE_API_BASE_URL=http://localhost:8080/api
    VITE_CLOUDINARY_BASE_URL=https://res.cloudinary.com/your-cloud-name/image/upload/
    ```

4.  Run the development server:
    ```bash
    npm run dev
    ```

The frontend will be available at `http://localhost:517x`.

---

## Project Structure

```
teamfoundry-workforce-management/
├── backend/               # Java/Spring Boot API Source Code
│   ├── src/
│   ├── build.gradle
│   └── Dockerfile
└── frontend/              # React Interface Source Code
    ├── src/
    ├── package.json
    └── vite.config.js
```

## Documentation (Portuguese)

- Consult `Manual_utilização_TeamFoundry.pdf` for a end-user guide, including usage flows.
- `Relatório Final Team Foundry.pdf` captures the academic context, architectural decisions, and evaluation criteria for the project.

## Contributing

1.  Fork the project.
2.  Create a Branch for your Feature (`git checkout -b feature/MyFeature`).
3.  Commit your changes (`git commit -m 'Adding MyFeature'`).
4.  Push to the Branch (`git push origin feature/MyFeature`).
5.  Open a Pull Request.
