import { Link } from "react-router-dom";
import Navbar from "../../../components/sections/Navbar.jsx";
import Footer from "../../../components/sections/Footer.jsx";
import { useAuthContext } from "../../../auth/AuthContext.jsx";

export default function FaqCandidateDocuments() {
  const { userType, logout } = useAuthContext();
  const variant = userType ? "private" : "public";
  const homePath =
    userType === "COMPANY"
      ? "/empresa/informacoes"
      : userType === "EMPLOYEE"
      ? "/candidato/dados-pessoais"
      : "/";

  const baseLink = "/faq/documentos-candidato";
  const profileLink = userType === "EMPLOYEE" ? "/candidato/dados-pessoais" : baseLink;
  const documentsLink = userType === "EMPLOYEE" ? "/candidato/documentos" : baseLink;
  const certificationsLink = userType === "EMPLOYEE" ? "/candidato/certificacoes" : baseLink;

  return (
    <div className="min-h-screen bg-base-100 text-base-content">
      <Navbar variant={variant} homePath={homePath} links={[]} onLogout={logout} />

      <main className="max-w-5xl mx-auto px-6 py-12 space-y-10">
        <header className="space-y-2 text-center">
          <p className="text-sm uppercase tracking-widest text-primary/70">Perguntas Frequentes</p>
          <h1 className="text-4xl md:text-5xl font-extrabold text-primary">
            Quais documentos posso enviar?
          </h1>
          <p className="text-base-content/70">Esta funcionalidade é destinada a candidatos.</p>
        </header>

        <section className="rounded-2xl border border-base-200 bg-base-100 shadow-sm p-6 space-y-4">
          <p className="text-base-content/80">
            Na TeamFoundry, os candidatos podem completar o seu perfil através do upload de foto
            de perfil, currículo e documento de identificação. Estes elementos permitem validar a
            identidade, analisar o percurso profissional e tornar o perfil mais completo para
            futuras propostas.
          </p>
          <p className="text-base-content/80">
            Todos os ficheiros podem ser adicionados, atualizados ou substituídos sempre que
            necessário.
          </p>
        </section>

        <section className="rounded-2xl border border-base-200 bg-base-100 shadow-sm p-6 space-y-4">
          <h2 className="text-2xl font-bold text-primary">O que podes enviar</h2>
          <ul className="list-disc pl-5 space-y-1 text-base-content/80">
            <li>Foto de perfil – para identificação do candidato.</li>
            <li>Currículo (CV) – para apresentar a experiência profissional e competências.</li>
            <li>
              Documento de identificação – upload do documento frente e verso, para validação de
              identidade.
            </li>
          </ul>
        </section>

        <section className="rounded-2xl border border-base-200 bg-base-100 shadow-sm p-6 space-y-6">
          <div className="space-y-3">
            <h3 className="text-xl font-semibold text-primary">Foto</h3>
            <ol className="list-decimal pl-5 space-y-1 text-base-content/80">
              <li>
                Acede à aba{" "}
                <Link className="font-bold text-primary underline" to={profileLink}>
                  Perfil
                </Link>
                .
              </li>
              <li>Clica em Adicionar foto.</li>
              <li>Seleciona a imagem e guarda as alterações.</li>
            </ol>
          </div>

          <div className="space-y-3">
            <h3 className="text-xl font-semibold text-primary">Documentos</h3>
            <ol className="list-decimal pl-5 space-y-1 text-base-content/80">
              <li>
                Acede à aba{" "}
                <Link className="font-bold text-primary underline" to={documentsLink}>
                  Documentos
                </Link>
                .
              </li>
              <li>Faz upload do currículo (CV).</li>
              <li>
                Adiciona o documento de identificação, enviando o ficheiro da frente e o do verso.
              </li>
            </ol>
          </div>

          <div className="space-y-3">
            <h3 className="text-xl font-semibold text-primary">Certificação</h3>
            <ol className="list-decimal pl-5 space-y-1 text-base-content/80">
              <li>
                No Perfil, acede ao separador{" "}
                <Link className="font-bold text-primary underline" to={certificationsLink}>
                  Certificações
                </Link>
                .
              </li>
              <li>Clica em Adicionar para criar uma nova certificação.</li>
              <li>Preenche os dados da certificação e anexa o ficheiro comprovativo antes de confirmar.</li>
            </ol>
          </div>
        </section>
      </main>

      <Footer />
    </div>
  );
}
