import { Link } from "react-router-dom";
import Navbar from "../../../components/sections/Navbar.jsx";
import Footer from "../../../components/sections/Footer.jsx";
import { useAuthContext } from "../../../auth/AuthContext.jsx";

export default function FaqDeactivateAccount() {
  const { userType, logout } = useAuthContext();
  const variant = userType ? "private" : "public";
  const homePath =
    userType === "COMPANY"
      ? "/empresa/informacoes"
      : userType === "EMPLOYEE"
      ? "/candidato/dados-pessoais"
      : "/";

  const baseLink = "/faq/desativar-conta";
  const settingsLink =
    userType === "COMPANY"
      ? "/empresa/definicoes"
      : userType === "EMPLOYEE"
      ? "/candidato/definicoes"
      : baseLink;

  return (
    <div className="min-h-screen bg-base-100 text-base-content">
      <Navbar variant={variant} homePath={homePath} links={[]} onLogout={logout} />

      <main className="max-w-5xl mx-auto px-6 py-12 space-y-10">
        <header className="space-y-2 text-center">
          <p className="text-sm uppercase tracking-widest text-primary/70">Perguntas Frequentes</p>
          <h1 className="text-4xl md:text-5xl font-extrabold text-primary">
            É possível desativar a conta?
          </h1>
        </header>

        <section className="rounded-2xl border border-base-200 bg-base-100 shadow-sm p-6 space-y-4">
          <p className="text-base-content/80">
            Sim, é possível desativar a conta diretamente através das definições, de forma simples
            e segura.
          </p>
          <p className="text-base-content/80">
            A opção encontra-se na aba{" "}
            <Link className="font-bold text-primary underline" to={settingsLink}>
              Definições
            </Link>
            , onde o utilizador pode solicitar a desativação da conta após confirmar a sua
            palavra-passe.
          </p>
        </section>

        <section className="rounded-2xl border border-base-200 bg-base-100 shadow-sm p-6 space-y-4">
          <h2 className="text-2xl font-bold text-primary">Importante</h2>
          <p className="text-base-content/80">
            A plataforma não permite desativar a conta caso existam serviços ativos ou propostas
            aceites em curso. Esta regra garante a integridade dos processos e evita interrupções
            em compromissos já assumidos.
          </p>
          <p className="text-base-content/80">
            Caso existam serviços ativos, será necessário concluir ou cancelar esses processos
            antes de proceder à desativação da conta.
          </p>
        </section>

        <section className="rounded-2xl border border-base-200 bg-base-100 shadow-sm p-6 space-y-4">
          <p className="text-base-content/80">A desativação requer sempre:</p>
          <ul className="list-disc pl-5 space-y-1 text-base-content/80">
            <li>Confirmação da palavra-passe</li>
            <li>Confirmação explícita da intenção de desativar a conta</li>
          </ul>
          <p className="text-base-content/80">
            Caso o utilizador pretenda voltar a utilizar a plataforma no futuro, a reativação da
            conta pode ser solicitada através do suporte.
          </p>
        </section>
      </main>

      <Footer />
    </div>
  );
}
