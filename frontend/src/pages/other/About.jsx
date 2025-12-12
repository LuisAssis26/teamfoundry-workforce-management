import Navbar from "../../components/sections/Navbar.jsx";
import Footer from "../../components/sections/Footer.jsx";
import { useAuthContext } from "../../auth/AuthContext.jsx";

const team = [
  { name: "Nome do Membro 1", role: "Cargo / Função" },
  { name: "Nome do Membro 2", role: "Cargo / Função" },
  { name: "Nome do Membro 3", role: "Cargo / Função" },
  { name: "Nome do Membro 4", role: "Cargo / Função" },
  { name: "Nome do Membro 5", role: "Cargo / Função" },
  { name: "Nome do Membro 6", role: "Cargo / Função" },
];

export default function About() {
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

      <main className="max-w-6xl mx-auto px-6 py-12 space-y-12">
        <section className="space-y-4 text-center">
          <h1 className="text-4xl md:text-5xl font-extrabold text-primary">Sobre nós</h1>
          <p className="text-base-content/70 max-w-3xl mx-auto leading-relaxed">
            A TeamFoundry nasceu para simplificar a ligação entre indústria e talento. Combinamos
            validação de credenciais, gestão de requisições e acompanhamento de equipas num único
            fluxo, garantindo transparência e segurança para empresas e profissionais.
          </p>
        </section>

        <section className="grid md:grid-cols-2 gap-10 items-center">
          <div className="space-y-3">
            <h2 className="text-2xl font-bold text-primary">Origem e visão</h2>
            <p className="text-base-content/80 leading-relaxed">
              Criada por uma equipa multidisciplinar, a plataforma evoluiu a partir de necessidades
              reais de operações: aprovar contas com rigor, gerir uploads críticos (CVs e
              certificações), acompanhar requisições e manter logs completos de ações.
            </p>
            <p className="text-base-content/80 leading-relaxed">
              Continuamos a iterar com foco em experiência, segurança e dados fiáveis — para que
              cada ligação entre empresa e profissional seja rápida, segura e rastreável.
            </p>
          </div>
          <div className="card bg-base-100 border border-base-200 shadow-sm">
            <div className="card-body space-y-2">
              <h3 className="text-lg font-semibold text-primary">O que estamos a construir</h3>
              <ul className="list-disc list-inside text-base-content/80 space-y-1">
                <li>Validação e aprovação de credenciais de empresas e admins.</li>
                <li>Gestão de requisições com filtros e logs de ações.</li>
                <li>Uploads seguros para CV, certificações e foto de perfil.</li>
                <li>Dashboard de métricas e histórico de logs por perfil.</li>
              </ul>
            </div>
          </div>
        </section>

        <section className="space-y-4">
          <h2 className="text-2xl font-bold text-primary text-center">A equipa</h2>
          <p className="text-base-content/70 text-center max-w-3xl mx-auto">
            Placeholders para os membros do projeto. Substitua pelas fotos e bios finais assim que
            estiverem disponíveis.
          </p>
          <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {team.map((member) => (
              <div
                key={member.name}
                className="card bg-base-100 border border-base-200 shadow-sm hover:shadow-md transition-shadow"
              >
                <div className="card-body items-center text-center space-y-3">
                  <div className="h-24 w-24 rounded-full bg-base-200" />
                  <div>
                    <p className="text-lg font-semibold text-primary">{member.name}</p>
                    <p className="text-base-content/70">{member.role}</p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </section>
      </main>

      <Footer />
    </div>
  );
}
