import { Link } from "react-router-dom";
import Navbar from "../../../components/sections/Navbar.jsx";
import Footer from "../../../components/sections/Footer.jsx";
import { useAuthContext } from "../../../auth/AuthContext.jsx";

export default function FaqCompanyRequestsStatus() {
  const { userType, logout } = useAuthContext();
  const variant = userType ? "private" : "public";
  const homePath =
    userType === "COMPANY"
      ? "/empresa/informacoes"
      : userType === "EMPLOYEE"
      ? "/candidato/dados-pessoais"
      : "/";

  const companyRequestsLink =
    userType === "COMPANY" ? "/empresa/requisicoes" : "/faq/estado-requisicoes";

  return (
    <div className="min-h-screen bg-base-100 text-base-content">
      <Navbar variant={variant} homePath={homePath} links={[]} onLogout={logout} />

      <main className="max-w-5xl mx-auto px-6 py-12 space-y-10">
        <header className="space-y-2 text-center">
          <p className="text-sm uppercase tracking-widest text-primary/70">Perguntas Frequentes</p>
          <h1 className="text-4xl md:text-5xl font-extrabold text-primary">
            Posso acompanhar o estado das minhas requisições?
          </h1>
          <p className="text-base-content/70">
            Esta funcionalidade é exclusiva para empresas.
          </p>
        </header>

        <section className="rounded-2xl border border-base-200 bg-base-100 shadow-sm p-6 space-y-4">
          <p className="text-base-content/80">
            As empresas podem acompanhar todas as requisições criadas diretamente na aba{" "}
            <Link className="font-bold text-primary underline" to={companyRequestsLink}>
              Requisições
            </Link>
            , onde é apresentada uma visão geral dos pedidos efetuados.
          </p>
          <p className="text-base-content/80">
            Cada requisição exibe, no canto superior direito, o seu estado atual, que pode ser:
          </p>
          <ul className="list-disc pl-5 space-y-1 text-base-content/80">
            <li>
              <span className="font-semibold text-primary">Pendente</span> – a requisição foi
              criada e encontra-se em análise ou em preparação da equipa.
            </li>
            <li>
              <span className="font-semibold text-primary">Ativa</span> – a equipa está montada
              ou em execução.
            </li>
            <li>
              <span className="font-semibold text-primary">Passada</span> – a requisição já foi
              concluída.
            </li>
          </ul>
          <p className="text-base-content/80">
            Para facilitar a gestão, a plataforma permite filtrar as requisições por estado,
            possibilitando que a empresa visualize apenas pedidos pendentes, ativos ou passados,
            conforme a sua necessidade.
          </p>
          <p className="text-base-content/80">
            Esta funcionalidade permite às empresas manter um controlo claro e organizado das
            suas requisições ao longo do tempo.
          </p>
        </section>
      </main>

      <Footer />
    </div>
  );
}
