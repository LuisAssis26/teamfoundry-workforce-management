import Navbar from "../../../components/sections/Navbar.jsx";
import Footer from "../../../components/sections/Footer.jsx";
import { useAuthContext } from "../../../auth/AuthContext.jsx";

export default function FaqCandidateOfferAcceptance() {
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
            Posso recusar uma oferta depois de aceitá-la?
          </h1>
          <p className="text-base-content/70">
            Saiba o que acontece após aceitar uma proposta e quais são as opções disponíveis.
          </p>
        </header>

        <section className="rounded-2xl border border-base-200 bg-base-100 shadow-sm p-6 space-y-4">
          <p className="text-base-content/80">
            Não. Por motivos de profissionalidade, após a aceitação de uma proposta, não é possível
            recusar ou cancelar a oferta através da plataforma.
          </p>
          <p className="text-base-content/80">
            A aceitação de uma proposta representa um compromisso entre o candidato e a empresa.
            Caso seja necessária alguma alteração às condições acordadas ou exista qualquer
            imprevisto, o candidato deverá entrar em contacto diretamente com a empresa para
            analisar a situação.
          </p>
          <p className="text-base-content/80">
            Esta regra garante clareza, compromisso e confiança entre todas as partes envolvidas.
          </p>
        </section>
      </main>

      <Footer />
    </div>
  );
}
