import Navbar from "../../../components/sections/Navbar.jsx";
import Footer from "../../../components/sections/Footer.jsx";
import { useAuthContext } from "../../../auth/AuthContext.jsx";

export default function FaqCompanyRequestCancellation() {
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
            Posso cancelar ou apagar uma requisição?
          </h1>
          <p className="text-base-content/70">
            Entenda se é possível cancelar ou apagar uma requisição após a sua criação.
          </p>
        </header>

        <section className="rounded-2xl border border-base-200 bg-base-100 shadow-sm p-6 space-y-4">
          <p className="text-base-content/80">
            Não. Por motivos de profissionalidade, uma requisição não pode ser cancelada nem
            apagada após ser criada.
          </p>
          <p className="text-base-content/80">
            Depois de criada, a requisição é analisada por administradores responsáveis pela
            montagem das equipas e podem já existir candidatos convidados para integrar essa
            equipa. Cancelar ou eliminar a requisição poderia comprometer estes processos.
          </p>
          <p className="text-base-content/80">
            Por esse motivo, antes da submissão final, o sistema solicita à empresa uma confirmação
            explícita para garantir que pretende mesmo criar a requisição com os dados fornecidos.
          </p>
          <p className="text-base-content/80">
            Esta abordagem assegura organização, transparência e respeito por todos os
            intervenientes.
          </p>
        </section>
      </main>

      <Footer />
    </div>
  );
}
