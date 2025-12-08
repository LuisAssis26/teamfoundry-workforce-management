import { useEffect, useMemo, useState } from "react";
import { listEmployeeOffers, acceptEmployeeOffer, listEmployeeJobs } from "../../../../api/profile/profileJobs.js";
import { useEmployeeProfile } from "../EmployeeProfileContext.jsx";
import JobOfferCard from "./JobOfferCard.jsx";
import FilterDropdown from "../../../../components/ui/Dropdown/FilterDropdown.jsx";

const STATUS_FILTERS = [
  { value: "ALL", label: "Todas" },
  { value: "OPEN", label: "Abertas" },
  { value: "ACCEPTED", label: "Aceites" },
  { value: "CLOSED", label: "Esgotadas" },
];

const DATE_ORDER_OPTIONS = [
  { value: "DESC", label: "Mais recentes" },
  { value: "ASC", label: "Mais antigas" },
];

export default function JobOffers() {
  const [offers, setOffers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [feedback, setFeedback] = useState("");

  const { setJobsData, jobsData, offersData, offersLoaded, setOffersData, setOffersLoaded } = useEmployeeProfile();

  const [statusFilter, setStatusFilter] = useState("ALL");
  const [companyFilter, setCompanyFilter] = useState("ALL");
  const [startDateOrder, setStartDateOrder] = useState("DESC");

  useEffect(() => {
    let isMounted = true;
    async function loadOffers() {
      if (offersLoaded && Array.isArray(offersData)) {
        setOffers(offersData);
        setLoading(false);
        return;
      }
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
    }, [offersData, offersLoaded, setOffersData, setOffersLoaded]);

  const companyOptions = useMemo(() => {
    const names = new Set();
    offers.forEach((o) => {
      if (o.companyName) names.add(o.companyName);
    });
    return ["ALL", ...Array.from(names)];
  }, [offers]);

  const filteredOffers = useMemo(() => {
    const list = offers.filter((offer) => {
      const status = (offer.status || "").toUpperCase();
      const companyName = offer.companyName || "";
      const statusOk = statusFilter === "ALL" ? true : status === statusFilter;
      const companyOk = companyFilter === "ALL" ? true : companyName === companyFilter;
      return statusOk && companyOk;
    });

    return [...list].sort((a, b) => {
      const aDate = a.startDate ? new Date(a.startDate).getTime() : 0;
      const bDate = b.startDate ? new Date(b.startDate).getTime() : 0;
      if (aDate === bDate) return 0;
      return startDateOrder === "ASC" ? aDate - bDate : bDate - aDate;
    });
  }, [offers, statusFilter, companyFilter, startDateOrder]);

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
        <div className="flex items-center gap-4 mb-6 justify-start sm:justify-start md:justify-start">
          <h2 className="text-3xl font-semibold text-center sm:text-center md:text-left w-full">Ofertas de Trabalho</h2>
        </div>
        
          <div className="space-y-4">
            {/* Filtros */}
            <div className="flex flex-wrap gap-3 justify-center md:justify-start w-full px-0">
              <FilterDropdown
                  label="Status:"
                  value={statusFilter}
                  onChange={setStatusFilter}
                  options={STATUS_FILTERS}
                  className="px-1"
                  selectClassName="w-30 bg-base-200 h-10"
              />

              <FilterDropdown
                  label="Data:"
                  value={startDateOrder}
                  onChange={setStartDateOrder}
                  options={DATE_ORDER_OPTIONS}
                  className="px-1"
                  selectClassName="w-30 bg-base-200 h-10"
              />

              <FilterDropdown
                  label="Empresa:"
                  value={companyFilter}
                  onChange={setCompanyFilter}
                  options={companyOptions.map((opt) => ({
                      value: opt,
                      label: opt === "ALL" ? "Todas" : opt,
                  }))}
                  className="hidden md:block px-1 "
                  selectClassName="w-30 bg-base-200 h-10 ml-2"
              />
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
                <div className="space-y-4">
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
