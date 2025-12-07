import { useEffect, useState } from "react";
import { Navigate } from "react-router-dom";
import Navbar from "../../components/sections/Navbar.jsx";
import Footer from "../../components/sections/Footer.jsx";
import { useAuthContext } from "../../auth/AuthContext.jsx";
import { fetchWeeklyTipsPage } from "../../api/site/siteManagement.js";

const FALLBACK_TIP_OF_WEEK = {
  category: "Seguranca",
  title: "Seguranca em primeiro lugar!",
  description:
    "Antes de comecares o turno, confirma se todos os equipamentos estao em boas condicoes. Pequenos cuidados evitam grandes acidentes.",
  publishedAt: new Date().toISOString().slice(0, 10),
};

export default function WeeklyTipsPage() {
  const { isAuthenticated, logout } = useAuthContext();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let active = true;
    setLoading(true);
    fetchWeeklyTipsPage()
      .then((payload) => {
        if (!active) return;
        setData(payload);
        setError(null);
      })
      .catch((err) => {
        if (!active) return;
        setError(err.message || "Nao foi possivel carregar as dicas.");
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, []);

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  const tipOfWeek = data?.tipOfWeek ?? FALLBACK_TIP_OF_WEEK;
  const allTips = Array.isArray(data?.tips) ? data.tips : [];
  const otherTips =
    tipOfWeek && tipOfWeek.id
      ? allTips.filter((tip) => tip.id !== tipOfWeek.id)
      : allTips;

  return (
    <div className="min-h-screen flex flex-col bg-base-100 text-base-content">
      <Navbar variant="private" homePath="/" links={[]} onLogout={logout} />
      <main className="flex-1">
        <TipsHero />
        <section className="max-w-6xl mx-auto px-6 py-10 space-y-10">
          {error && (
            <div className="alert alert-warning shadow">
              <span>{error}</span>
            </div>
          )}
          <TipOfWeekCard tip={tipOfWeek} loading={loading} />
          <AllTipsGrid tips={otherTips} loading={loading} />
        </section>
      </main>
      <Footer />
    </div>
  );
}

function TipsHero() {
  return (
    <section className="bg-base-200/60 border-b border-base-200">
      <div className="max-w-6xl mx-auto px-6 py-12 space-y-4">
        <p className="text-xs uppercase tracking-[0.35em] text-primary/80">
          Formacao continua
        </p>
        <h1 className="text-3xl md:text-4xl font-bold text-base-content">
          Dicas e conselhos
        </h1>
        <p className="max-w-2xl text-sm md:text-base text-base-content/70">
          Aprende com a experiencia de profissionais do setor. Aqui
          encontras dicas praticas sobre seguranca, produtividade e
          desenvolvimento profissional para te ajudar a ter sucesso na
          industria.
        </p>
      </div>
    </section>
  );
}

function TipOfWeekCard({ tip, loading }) {
  const publishedDate = formatDate(tip?.publishedAt);

  return (
    <section className="rounded-3xl bg-primary text-primary-content p-8 md:p-10 shadow-lg space-y-4">
      <p className="text-xs uppercase tracking-[0.35em] opacity-80 flex items-center gap-2">
        <span className="inline-flex h-7 w-7 items-center justify-center rounded-full border border-primary-content/40">
          <i className="bi bi-lightbulb" aria-hidden="true" />
        </span>
        Dica da semana
      </p>
      <div className="space-y-2">
        <div className="flex flex-wrap items-center gap-3 text-xs md:text-sm opacity-90">
          {tip?.category && (
            <span className="uppercase tracking-wide px-3 py-1 rounded-full bg-primary-content/10">
              {tip.category}
            </span>
          )}
          {publishedDate && (
            <span className="opacity-80">Atualizada em {publishedDate}</span>
          )}
        </div>
        <h2 className="text-2xl md:text-3xl font-semibold">
          {tip?.title ?? "Em breve novas dicas"}
        </h2>
        <p className="text-sm md:text-base leading-relaxed opacity-90 whitespace-pre-line">
          {tip?.description ??
            "Mantem-te atento: em breve vamos partilhar novas dicas para apoiares o teu desenvolvimento profissional."}
        </p>
      </div>
      {loading && (
        <div className="flex items-center gap-2 text-xs opacity-90">
          <span className="loading loading-spinner loading-xs" />
          <span>A carregar conteudo...</span>
        </div>
      )}
    </section>
  );
}

function AllTipsGrid({ tips, loading }) {
  const hasTips = tips && tips.length > 0;
  const items = hasTips ? tips.slice(0, 10) : [];

  return (
    <section className="space-y-6">
      <div className="space-y-1">
        <h2 className="text-xl md:text-2xl font-semibold text-base-content">
          Todas as dicas
        </h2>
        <p className="text-sm text-base-content/70">
          Consulta o historico de boas praticas que fomos reunindo ao longo do
          tempo.
        </p>
      </div>

      {loading && !hasTips && (
        <div className="flex items-center gap-2 text-sm text-base-content/70">
          <span className="loading loading-spinner loading-sm" />
          <span>A carregar dicas anteriores...</span>
        </div>
      )}

      {!loading && !hasTips && (
        <p className="text-sm text-base-content/70">
          Ainda nao tem dicas guardadas. Assim que o administrador adicionar
          conteudo, vais encontra-lo aqui.
        </p>
      )}

      {hasTips && (
        <div className="grid gap-6 md:grid-cols-2">
          {items.map((tip) => (
            <article
              key={tip.id}
              className="rounded-2xl border border-base-200 bg-base-100 shadow-sm p-5 flex flex-col gap-3"
            >
              <div className="flex items-center justify-between text-xs text-base-content/60">
                <span className="uppercase tracking-wide text-primary/80 font-semibold">
                  {tip.category}
                </span>
                <span>{formatDate(tip.publishedAt)}</span>
              </div>
              <h3 className="text-base font-semibold text-base-content">
                {tip.title}
              </h3>
              <p className="text-sm text-base-content/70 line-clamp-4">
                {tip.description}
              </p>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

function formatDate(value) {
  if (!value) return null;
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return null;
  return date.toLocaleDateString("pt-PT", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });
}

