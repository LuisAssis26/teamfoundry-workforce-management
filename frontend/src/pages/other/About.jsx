import Navbar from "../../components/sections/Navbar.jsx";
import Footer from "../../components/sections/Footer.jsx";
import { useAuthContext } from "../../auth/AuthContext.jsx";
import joaoGabriel from "../../assets/images/logo/devTeam/joao_gabriel.jpg";
import luisAssis from "../../assets/images/logo/devTeam/luis_assis.jpg";
import luisNantes from "../../assets/images/logo/devTeam/luis_nantes.jpg";
import pedroSampaio from "../../assets/images/logo/devTeam/pedro_sampaio.jpg";
import gabrielOliveira from "../../assets/images/logo/devTeam/gabriel_oliveira.jpg";
import rodrigoFerreira from "../../assets/images/logo/devTeam/rodrigo_ferreira.jpg";

const team = [
  {
    name: "Luís Assis",
    role: "Membro da Equipa de Desenvolvimento · Gestor do Projeto",
    initials: "LA",
    photo: luisAssis,
  },
  {
    name: "Luís Nantes",
    role: "Membro da Equipa de Desenvolvimento",
    initials: "LN",
    photo: luisNantes,
  },
  {
    name: "João Nunes",
    role: "Membro da Equipa de Desenvolvimento",
    initials: "JN",
    photo: joaoGabriel,
  },
  {
    name: "Gabriel Oliveira",
    role: "Membro da Equipa de Desenvolvimento",
    initials: "GO",
    photo: gabrielOliveira,
  },
  {
    name: "Pedro Sampaio",
    role: "Membro da Equipa de Desenvolvimento",
    initials: "PS",
    photo: pedroSampaio,
  },
  {
    name: "Rodrigo Ferreira",
    role: "Membro da Equipa de Desenvolvimento",
    initials: "RF",
    photo: rodrigoFerreira,
  },
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
        <section className="relative overflow-hidden rounded-3xl border border-base-200 bg-base-100 px-8 py-12 text-center shadow-sm">
          <div className="absolute -top-24 -right-28 h-56 w-56 rounded-full bg-primary/10 blur-3xl" />
          <div className="absolute -bottom-24 -left-28 h-56 w-56 rounded-full bg-secondary/10 blur-3xl" />
          <div className="relative space-y-4">
            <p className="text-sm font-semibold uppercase tracking-[0.2em] text-secondary">
              TeamFoundry
            </p>
            <h1 className="text-4xl md:text-5xl font-extrabold text-primary">Sobre nós</h1>
            <p className="text-base-content/70 max-w-3xl mx-auto leading-relaxed">
              A TeamFoundry nasceu para simplificar a ligação entre indústria e talento. Combinamos
              validação de credenciais, gestão de requisições e acompanhamento de equipas num único
              fluxo, garantindo transparência e segurança para empresas e profissionais.
            </p>
            <div className="flex flex-wrap justify-center gap-3 pt-2">
              <span className="badge badge-outline border-primary/30 text-primary">
                Credenciais seguras
              </span>
              <span className="badge badge-outline border-secondary/30 text-secondary">
                Fluxos rastreáveis
              </span>
              <span className="badge badge-outline border-accent/30 text-accent">Dados fiáveis</span>
            </div>
          </div>
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
              Continuamos a iterar com foco em experiência, segurança e dados fiáveis para que cada
              ligação entre empresa e profissional seja rápida, segura e rastreável.
            </p>
          </div>
          <div className="card bg-base-100 border border-base-200 shadow-sm">
            <div className="card-body space-y-3">
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
            Conhece as pessoas que deram forma ao projeto e que mantêm a plataforma a evoluir.
          </p>
          <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {team.map((member) => (
              <div
                key={member.name}
                className="card bg-base-100 border border-base-200 shadow-sm hover:shadow-md transition-shadow"
              >
                <div className="card-body items-center text-center space-y-3">
                  <div className="h-24 w-24 rounded-full bg-primary/10 text-primary flex items-center justify-center text-xl font-semibold ring-1 ring-primary/20 overflow-hidden">
                    {member.photo ? (
                      <img
                        src={member.photo}
                        alt={`Foto de ${member.name}`}
                        className="h-full w-full object-cover"
                      />
                    ) : (
                      <span>{member.initials}</span>
                    )}
                  </div>
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
