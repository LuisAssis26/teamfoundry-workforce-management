import { useEffect, useMemo, useState } from "react";
import {
  Bar,
  BarChart,
  CartesianGrid,
  Legend,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { apiFetch } from "../../../../api/auth/client.js";

export default function Metrics() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;
    async function load() {
      setLoading(true);
      setError(null);
      try {
        const resp = await apiFetch("/api/super-admin/metrics/overview");
        if (!resp.ok) throw new Error("Falha ao carregar métricas.");
        const payload = await resp.json();
        if (!cancelled) setData(payload);
      } catch (err) {
        if (!cancelled) setError(err.message || "Erro inesperado ao obter métricas.");
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    load();
    return () => {
      cancelled = true;
    };
  }, []);

  const kpis = useMemo(() => {
    const k = data?.kpis ?? {};
    return [
      { label: "Empresas ativas", value: k.activeCompanies ?? 0 },
      { label: "Funcionários ativos", value: k.activeEmployees ?? 0 },
      { label: "Credenciais pendentes", value: k.pendingCompanies ?? 0 },
      { label: "Requisições abertas", value: k.openRequests ?? 0 },
      { label: "Requisições concluídas", value: k.closedRequests ?? 0 },
    ];
  }, [data]);

  const requestsByState = useMemo(
      () => (data?.requestsByState ?? []).map((item) => ({
        ...item,
        label: formatState(item.state),
      })),
      [data]
  );

  const workloads = useMemo(
      () => (data?.workloadByAdmin ?? []).map((w) => ({
        ...w,
        adminLabel: w.adminName || `Admin ${w.adminId ?? ""}`,
      })),
      [data]
  );

  return (
    <section className="space-y-8">
      <header>
        <h1 className="text-3xl md:text-4xl font-extrabold text-primary">Métricas</h1>
      </header>

      {error && (
        <div className="alert alert-error shadow-md">
          <span>{error}</span>
        </div>
      )}

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-5">
        {kpis.map((kpi) => (
          <div key={kpi.label} className="card bg-base-100 border border-base-200 shadow-sm">
            <div className="card-body p-4">
              <p className="text-sm text-base-content/70">{kpi.label}</p>
              <p className="text-3xl font-extrabold text-primary mt-1">{kpi.value}</p>
            </div>
          </div>
        ))}
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <ChartPanel title="Requisições por estado" loading={loading}>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={requestsByState}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="label" />
              <YAxis allowDecimals={false} />
              <Tooltip contentStyle={{ boxShadow: "none", border: "1px solid #e5e7eb" }} cursor={false} />
              <Legend />
              <Bar dataKey="count" name="Requisições" fill="#4f46e5" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </ChartPanel>

        <ChartPanel title="Requisições pendentes por administrador" loading={loading}>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart layout="vertical" data={workloads.slice(0, 6)} margin={{ left: 32 }}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis type="number" allowDecimals={false} />
              <YAxis type="category" dataKey="adminLabel" width={120} />
              <Tooltip contentStyle={{ boxShadow: "none", border: "1px solid #e5e7eb" }} cursor={false} />
              <Legend />
              <Bar dataKey="pendingRequests" name="Pendentes" fill="#0ea5e9" radius={[0, 4, 4, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </ChartPanel>
      </div>

    </section>
  );
}

function ChartPanel({ title, loading, children }) {
  return (
    <div className="card bg-base-100 border border-base-200 shadow-sm">
      <div className="card-body">
        <div className="flex items-center justify-between mb-2">
          <h2 className="text-lg font-semibold">{title}</h2>
          {loading && <span className="loading loading-spinner loading-sm text-primary" />}
        </div>
        <div className="h-[320px] w-full">{children}</div>
      </div>
    </div>
  );
}

function formatState(state) {
  const s = String(state || "").toUpperCase();
  if (s === "COMPLETE") return "Concluídas";
  if (s === "INCOMPLETE") return "Incompletas";
  return s || "N/D";
}
