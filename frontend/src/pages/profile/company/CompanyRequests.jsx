import { useEffect, useMemo, useState } from "react";
import Button from "../../../components/ui/Button/Button.jsx";
import { useCompanyProfile } from "./CompanyProfileContext.jsx";
import CompanyRequestCard from "./components/CompanyRequestCard.jsx";
import { createCompanyRequest } from "../../../api/profile/companyRequests.js";
import CompanyRequestModal from "./components/CompanyRequestModal.jsx";
import FilterDropdown from "../../../components/ui/Dropdown/FilterDropdown.jsx";

/**
 * Página de requisições da empresa: filtros + botão para criar requisição.
 */
export default function CompanyRequests() {
  const {
    requestsData,
    requestsLoaded,
    refreshRequests,
    setRequestsData,
    setRequestsLoaded,
  } = useCompanyProfile();

  const [statusFilter, setStatusFilter] = useState("ALL");
  const [orderBy, setOrderBy] = useState("START_DESC");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [feedback, setFeedback] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    let mounted = true;
    async function load() {
      setLoading(true);
      setError("");
      try {
        const data = await refreshRequests();
        if (!mounted) return;
        setRequestsData(Array.isArray(data) ? data : []);
      } catch (err) {
        if (mounted) setError(err.message || "Não foi possível carregar as requisições.");
      } finally {
        if (mounted) setLoading(false);
      }
    }
    if (!requestsLoaded) {
      load();
    } else {
      setLoading(false);
    }
    return () => {
      mounted = false;
    };
  }, [refreshRequests, requestsLoaded, setRequestsData]);

  const filteredRequests = useMemo(() => {
    const list = Array.isArray(requestsData) ? [...requestsData] : [];
    const filtered =
      statusFilter === "ALL"
        ? list
        : list.filter((req) => (req.computedStatus || "").toUpperCase() === statusFilter);

    const comparator = (a, b) => {
      const aDate = a.startDate ? new Date(a.startDate) : a.createdAt ? new Date(a.createdAt) : null;
      const bDate = b.startDate ? new Date(b.startDate) : b.createdAt ? new Date(b.createdAt) : null;
      const aTime = aDate ? aDate.getTime() : 0;
      const bTime = bDate ? bDate.getTime() : 0;
      if (orderBy === "START_ASC") return aTime - bTime;
      return bTime - aTime;
    };

    return filtered.sort(comparator);
  }, [requestsData, statusFilter, orderBy]);

  const handleCreate = async (payload) => {
    setSaving(true);
    setError("");
    setFeedback("");
    try {
      const created = await createCompanyRequest(payload);
      const updated = [created, ...(requestsData || [])];
      setRequestsData(updated);
      setRequestsLoaded(true);
      setFeedback("Requisição criada com sucesso.");
      setModalOpen(false);
      if (created?.computedStatus) {
        setStatusFilter(created.computedStatus);
      }
    } catch (err) {
      setError(err.message || "Não foi possível criar a requisição.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="w-full">
      <header className="flex items-center gap-4 mb-6 justify-between">
        <div>
          <h1 className="text-3xl font-semibold text-center sm:text-center md:text-left w-full">Requisições</h1>
        </div>
        <Button
          label="Fazer requisição"
          variant="primary"
          fullWidth={false}
          onClick={() => setModalOpen(true)}
        />
      </header>

      <div className="space-y-4">
        <div className="flex flex-wrap gap-3 justify-center md:justify-start w-full px-0">
          <FilterDropdown
            label="Estado:"
            value={statusFilter}
            onChange={setStatusFilter}
            options={[
              { value: "ALL", label: "Todos" },
              { value: "ACTIVE", label: "Ativas" },
              { value: "PENDING", label: "Pendentes" },
              { value: "PAST", label: "Passadas" },
            ]}
            selectClassName="w-30 bg-base-200 h-10"
          />
          <FilterDropdown
            label="Data:"
            value={orderBy}
            onChange={setOrderBy}
            options={[
              { value: "START_ASC", label: "Mais antiga" },
              { value: "START_DESC", label: "Mais recente" },
            ]}
            selectClassName="w-30 bg-base-200 h-10"
          />
        </div>
      

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

        {loading ? (
          <SkeletonList />
        ) : filteredRequests.length === 0 ? (
          <EmptyState />
        ) : (
          <div className="space-y-4">
            {filteredRequests.map((req) => (
              <CompanyRequestCard key={req.id} request={req} />
            ))}
          </div>
        )}
      </div>
      <CompanyRequestModal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        onSubmit={handleCreate}
        loading={saving}
      />
    </div>
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
      Ainda não existem requisições nesta categoria.
    </div>
  );
}
