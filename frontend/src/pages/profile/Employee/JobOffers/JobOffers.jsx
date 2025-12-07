import { useEffect, useMemo, useState } from "react";
import { listEmployeeOffers, acceptEmployeeOffer, listEmployeeJobs } from "../../../../api/profile/profileJobs.js";
import { useEmployeeProfile } from "../EmployeeProfileContext.jsx";
import JobOfferCard from "./JobOfferCard.jsx";

const STATUS_FILTERS = [
  { value: "ALL", label: "Todas" },
  { value: "OPEN", label: "Abertas" },
  { value: "ACCEPTED", label: "Aceites" },
  { value: "CLOSED", label: "Esgotadas" },
];

export default function JobOffers() {
  const [offers, setOffers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [feedback, setFeedback] = useState("");

  const { setJobsData, jobsData, setOffersData, setOffersLoaded } = useEmployeeProfile();

  const [statusFilter, setStatusFilter] = useState("ALL");
  const [companyFilter, setCompanyFilter] = useState("ALL");

  useEffect(() => {
    let isMounted = true;
    async function loadOffers() {
      setLoading(true);
      setError("");
      try {
        const data = await listEmployeeOffers();
        if (!isMounted) return;
        const normalized = Array.isArray(data) ? data : [];
        setOffers(normalized);
        setOffersData(normalized);
        setOffersLoaded(true);
      } catch (err) {
        if (isMounted) setError(err.message || "Não foi possível carregar as ofertas.");
      } finally {
        if (isMounted) setLoading(false);
      }
    }
    loadOffers();
    return () => {
      isMounted = false;
    };
  }, [setOffersData, setOffersLoaded]);

  const companyOptions = useMemo(() => {
    const names = new Set();
    offers.forEach((o) => {
      if (o.companyName) names.add(o.companyName);
    });
    return ["ALL", ...Array.from(names)];
  }, [offers]);

  const filteredOffers = useMemo(() => {
    return offers.filter((offer) => {
      const status = (offer.status || "").toUpperCase();
      const companyName = offer.companyName || "";
      const statusOk = statusFilter === "ALL" ? true : status === statusFilter;
      const companyOk = companyFilter === "ALL" ? true : companyName === companyFilter;
      return statusOk && companyOk;
    });
  }, [offers, statusFilter, companyFilter]);

  const handleAccept = async (id) => {
    setError("");
    setFeedback("");
    try {
      await acceptEmployeeOffer(id);
      const data = await listEmployeeOffers();
      const normalized = Array.isArray(data) ? data : [];
      setOffers(normalized);
      setOffersData(normalized);
      const history = await listEmployeeJobs();
      setJobsData(Array.isArray(history) ? history : jobsData);
      setFeedback("Oferta aceite com sucesso.");
    } catch (err) {
      setError(err.message || "Não foi possível aceitar a oferta.");
    }
  };

  return (
      <section className="w-full">
        <div className="flex items-center gap-4 mb-6">
          <h2 className="text-3xl font-semibold">Ofertas de Trabalho</h2>
        </div>
        
          <div className="space-y-4">
            {/* Filtros */}
            <div className="flex flex-wrap gap-3 justify-start w-full px-0">
              <div className="flex items-center gap-2 px-1">
                <span className="text-sm font-medium">Status:</span>
                <select
                    className="select select-sm select-ghost bg-base-100"
                    value={statusFilter}
                    onChange={(e) => setStatusFilter(e.target.value)}
                >
                  {STATUS_FILTERS.map((opt) => (
                      <option key={opt.value} value={opt.value}>
                        {opt.label}
                      </option>
                  ))}
                </select>
              </div>

              <div className="flex items-center gap-2">
                <span className="text-sm font-medium">Empresa:</span>
                <select
                    className="select select-sm select-ghost bg-base-100"
                    value={companyFilter}
                    onChange={(e) => setCompanyFilter(e.target.value)}
                >
                  {companyOptions.map((opt) => (
                      <option key={opt} value={opt}>
                        {opt === "ALL" ? "Todas" : opt}
                      </option>
                  ))}
                </select>
              </div>
            </div>

            {/* Mensagens */}
            {error && (
                <div className="alert alert-error text-sm" role="alert">
                  {error}
                </div>
            )}
            {feedback && (
                <div className="alert alert-success text-sm" role="status">
                  {feedback}
                </div>
            )}

            {/* Conteúdo */}
            {loading ? (
                <SkeletonList />
            ) : filteredOffers.length === 0 ? (
                <EmptyState />
            ) : (
                <div className="space-y-4 max-w-3xl">
                  {filteredOffers.map((offer) => (
                      <JobOfferCard key={offer.requestId ?? offer.id} offer={offer} onAccept={handleAccept} />
                  ))}
                </div>
            )}
          </div>
      </section>
  );
}

function SkeletonList() {
  return (
      <div className="animate-pulse space-y-3">
        <div className="h-20 bg-base-200 rounded-xl" />
        <div className="h-20 bg-base-200 rounded-xl" />
        <div className="h-20 bg-base-200 rounded-xl" />
      </div>
  );
}

function EmptyState() {
  return (
      <div className="text-center text-base-content/70 py-12 border border-dashed border-base-300 rounded-xl">
        Não existem ofertas neste momento.
      </div>
  );
}
