import Navbar from "../../components/sections/Navbar.jsx";
import Footer from "../../components/sections/Footer.jsx";
import { useAuthContext } from "../../auth/AuthContext.jsx";

const faqs = [
  {
    question: "Como a TeamFoundry liga empresas a profissionais?",
    answer:
      "Empresas publicam requisições e a nossa plataforma recomenda perfis e equipas com base em competências, disponibilidade e geografia. Os administradores validam credenciais antes de qualquer atribuição.",
  },
  {
    question: "Posso acompanhar o estado das requisições?",
    answer:
      "Sim. Cada requisição mostra estados como Incompleta, Em Progresso ou Concluída, datas previstas e responsável designado.",
  },
  {
    question: "Quais documentos posso enviar?",
    answer:
      "Currículo, certificações e foto de perfil. Os uploads são validados e podem ser substituídos a qualquer momento.",
  },
  {
    question: "Como funcionam as credenciais de empresa?",
    answer:
      "Super administradores revêm NIF, dados de contacto e país. Uma conta só fica ativa após aprovação.",
  },
  {
    question: "É possível desativar a conta?",
    answer:
      "Sim. Empresas e colaboradores podem desativar a conta, preservando dados e histórico. A reativação pode ser solicitada via suporte.",
  },
  {
    question: "Como proteger meus dados?",
    answer:
      "Nunca partilhe credenciais. Use passwords fortes (ou password manager) e, se possível, ative 2FA. Mantemos tokens por sessão e opções de lembrete só quando solicitado.",
  },
];

export default function FAQs() {
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

      <main className="max-w-6xl mx-auto px-6 py-12 space-y-10">
        <header className="space-y-2 text-center">
          <h1 className="text-4xl md:text-5xl font-extrabold text-primary">FAQ&apos;s</h1>
          <p className="text-base-content/70">
            Perguntas frequentes sobre credenciais, requisições e gestão de contas na TeamFoundry.
          </p>
        </header>

        <div className="grid gap-4">
          {faqs.map((item) => (
            <article
              key={item.question}
              className="card bg-base-100 border border-base-200 shadow-sm hover:shadow-md transition-shadow"
            >
              <div className="card-body space-y-2">
                <h2 className="text-lg font-semibold text-primary">{item.question}</h2>
                <p className="text-base-content/80 leading-relaxed">{item.answer}</p>
              </div>
            </article>
          ))}
        </div>
      </main>

      <Footer />
    </div>
  );
}
