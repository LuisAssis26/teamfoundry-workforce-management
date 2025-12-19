import Navbar from "../../../components/sections/Navbar.jsx";
import Footer from "../../../components/sections/Footer.jsx";
import { useAuthContext } from "../../../auth/AuthContext.jsx";

export default function FaqDataProtection() {
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
            Como proteger os meus dados?
          </h1>
        </header>

        <section className="rounded-2xl border border-base-200 bg-base-100 shadow-sm p-6 space-y-4">
          <p className="text-base-content/80">
            A proteção dos dados na TeamFoundry resulta de uma combinação entre boas práticas do
            utilizador e medidas de segurança da plataforma.
          </p>
          <p className="text-base-content/80">
            Para manter a sua conta segura, recomenda-se que nunca partilhe as suas credenciais de
            acesso e que utilize uma palavra-passe forte, preferencialmente única e difícil de
            adivinhar. Sempre que possível, é aconselhável recorrer a gestores de palavras-passe.
          </p>
          <p className="text-base-content/80">
            A plataforma utiliza mecanismos de segurança como sessões autenticadas, controlo de
            acesso e proteção dos dados armazenados. As opções de conveniência, como lembrar
            sessão, são utilizadas apenas quando explicitamente solicitadas pelo utilizador.
          </p>
          <p className="text-base-content/80">
            Além disso, é importante terminar a sessão em dispositivos partilhados e manter os
            dados do perfil sempre atualizados.
          </p>
          <p className="text-base-content/80">
            Estas medidas ajudam a garantir a confidencialidade, integridade e segurança da
            informação associada à conta.
          </p>
        </section>
      </main>

      <Footer />
    </div>
  );
}
