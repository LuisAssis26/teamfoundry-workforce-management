import Navbar from "../../../components/sections/Navbar.jsx";
import Footer from "../../../components/sections/Footer.jsx";
import { useAuthContext } from "../../../auth/AuthContext.jsx";

export default function FaqCompanyCredentials() {
  const { userType, logout } = useAuthContext();
  const variant = userType ? "private" : "public";
  const homePath =
    userType === "COMPANY"
      ? "/empresa/informacoes"
      : userType === "EMPLOYEE"
      ? "/candidato/dados-pessoais"
      : "/";

  return (
    <div className="min-h-screen bg-base-100 text-base-content">
      <Navbar variant={variant} homePath={homePath} links={[]} onLogout={logout} />

      <main className="max-w-5xl mx-auto px-6 py-12 space-y-10">
        <header className="space-y-2 text-center">
          <p className="text-sm uppercase tracking-widest text-primary/70">Perguntas Frequentes</p>
          <h1 className="text-4xl md:text-5xl font-extrabold text-primary">
            Como funcionam as credenciais de empresa?
          </h1>
          <p className="text-base-content/70">Esta funcionalidade é destinada a empresas.</p>
        </header>

        <section className="rounded-2xl border border-base-200 bg-base-100 shadow-sm p-6 space-y-4">
          <p className="text-base-content/80">
            Para utilizar a TeamFoundry como empresa, é necessário criar uma conta e submeter as
            credenciais empresariais através do processo de registo.
          </p>
          <p className="text-base-content/80">
            Após o registo, as credenciais da empresa ficam pendentes de validação. Um administrador
            da plataforma irá analisar os dados fornecidos, como:
          </p>
          <ul className="list-disc pl-5 space-y-1 text-base-content/80">
            <li>Nome da empresa</li>
            <li>Email de contacto</li>
            <li>NIF</li>
            <li>País e informações associadas</li>
          </ul>
          <p className="text-base-content/80">
            Enquanto as credenciais não forem aprovadas, a conta não poderá aceder às
            funcionalidades empresariais.
          </p>
          <p className="text-base-content/80">
            A empresa passa a ter acesso completo à plataforma apenas após a aceitação das
            credenciais por um administrador, garantindo um ambiente seguro, fiável e com entidades
            devidamente verificadas.
          </p>
        </section>
      </main>

      <Footer />
    </div>
  );
}
