import { Link } from "react-router-dom";
import Navbar from "../../../components/sections/Navbar.jsx";
import Footer from "../../../components/sections/Footer.jsx";
import { useAuthContext } from "../../../auth/AuthContext.jsx";

export default function FaqMatchingProcess() {
  const { userType, logout } = useAuthContext();
  const variant = userType ? "private" : "public";
  const homePath =
    userType === "COMPANY"
      ? "/empresa/informacoes"
      : userType === "EMPLOYEE"
      ? "/candidato/dados-pessoais"
      : "/";

  const companyRequestsLink = userType === "COMPANY" ? "/empresa/requisicoes" : "/faq/processo-matching";
  const employeeOffersLink = userType === "EMPLOYEE" ? "/candidato/ofertas" : "/faq/processo-matching";

  return (
    <div className="min-h-screen bg-base-100 text-base-content">
      <Navbar variant={variant} homePath={homePath} links={[]} onLogout={logout} />

      <main className="max-w-5xl mx-auto px-6 py-12 space-y-10">
        <header className="space-y-2 text-center">
          <p className="text-sm uppercase tracking-widest text-primary/70">Perguntas Frequentes</p>
          <h1 className="text-4xl md:text-5xl font-extrabold text-primary">
            Como funciona o processo de matching?
          </h1>
        </header>

        <section className="rounded-2xl border border-base-200 bg-base-100 shadow-sm p-6 space-y-4">
          <h2 className="text-2xl font-bold text-primary">Se és empresa</h2>
          <p className="text-base-content/80">
            Acede à aba{" "}
            <Link className="font-bold text-primary underline" to={companyRequestsLink}>
              Requisições
            </Link>{" "}
            e clica em Fazer requisição para iniciar um novo pedido.
          </p>
          <p className="text-base-content/80">
            Preenche o formulário com as informações essenciais da equipa:
          </p>
          <ul className="list-disc pl-5 space-y-1 text-base-content/80">
            <li>Nome da equipa</li>
            <li>Descrição da requisição</li>
            <li>Local de atuação</li>
            <li>Data de início e data de fim do trabalho</li>
          </ul>
          <p className="text-base-content/80">
            De seguida, adiciona as funções necessárias, indicando para cada uma:
          </p>
          <ul className="list-disc pl-5 space-y-1 text-base-content/80">
            <li>Nome da função</li>
            <li>Número de profissionais pretendidos</li>
            <li>Salário associado</li>
          </ul>
          <p className="text-base-content/80">
            Após preencher todas as funções, clica em Criar requisição. A requisição ficará
            registada e passará para a fase de análise.
          </p>
          <p className="text-base-content/80">
            Um administrador será automaticamente atribuído à tua requisição e ficará
            responsável por montar a equipa, garantindo que o processo decorre de forma
            organizada e segura.
          </p>
        </section>

        <section className="rounded-2xl border border-base-200 bg-base-100 shadow-sm p-6 space-y-4">
          <h2 className="text-2xl font-bold text-primary">O que acontece a seguir?</h2>
          <ul className="list-disc pl-5 space-y-2 text-base-content/80">
            <li>
              O sistema recomenda automaticamente candidatos adequados com base nas funções,
              competências e localização.
            </li>
            <li>
              O administrador pode ajustar a seleção utilizando filtros como função, área
              geográfica e competências, assegurando a melhor correspondência possível.
            </li>
            <li>Após a seleção, são enviados convites aos candidatos.</li>
          </ul>
        </section>

        <section className="rounded-2xl border border-base-200 bg-base-100 shadow-sm p-6 space-y-4">
          <h2 className="text-2xl font-bold text-primary">Se és candidato</h2>
          <p className="text-base-content/80">
            Quando recebes um convite, este fica disponível na aba{" "}
            <Link className="font-bold text-primary underline" to={employeeOffersLink}>
              Ofertas
            </Link>
            .
          </p>
          <p className="text-base-content/80">Em cada oferta, podes consultar:</p>
          <ul className="list-disc pl-5 space-y-1 text-base-content/80">
            <li>Detalhes da função</li>
            <li>Condições propostas</li>
            <li>Informações relevantes sobre o trabalho</li>
          </ul>
          <p className="text-base-content/80">
            Após analisar a proposta, podes aceitar ou rejeitar o convite diretamente na
            plataforma.
          </p>
        </section>
      </main>

      <Footer />
    </div>
  );
}
