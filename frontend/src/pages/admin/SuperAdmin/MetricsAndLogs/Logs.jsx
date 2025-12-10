import { useMemo, useState } from "react";
import InputField from "../../../../components/ui/Input/InputField.jsx";
import Button from "../../../../components/ui/Button/Button.jsx";
import { apiFetch } from "../../../../api/auth/client.js";

export default function Logs() {
  const [logs, setLogs] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [filters, setFilters] = useState(() => {
    const now = new Date();
    return {
      type: "ALL",
      month: now.getMonth() + 1,
      year: now.getFullYear(),
      query: "",
    };
  });

  const monthOptions = useMemo(() => Array.from({ length: 12 }, (_, i) => i + 1), []);
  const yearOptions = useMemo(
    () => Array.from({ length: 6 }, (_, i) => new Date().getFullYear() - i),
    []
  );

  const handleSearch = async () => {
    setLoading(true);
    setError(null);
    try {
      const params = new URLSearchParams();
      if (filters.type && filters.type !== "ALL") params.append("type", filters.type);
      if (filters.year) params.append("year", String(filters.year));
      if (filters.month) params.append("month", String(filters.month));
      if (filters.query.trim()) params.append("q", filters.query.trim());

      const resp = await apiFetch(`/api/super-admin/logs?${params.toString()}`);
      if (!resp.ok) throw new Error("Falha ao carregar logs.");
      const payload = await resp.json();
      setLogs(Array.isArray(payload) ? payload : []);
    } catch (err) {
      setError(err.message || "Erro ao pesquisar logs.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="space-y-6">
      <header className="space-y-1">
        <h1 className="text-3xl md:text-4xl font-extrabold text-primary">Logs</h1>
      </header>

      <div className="card bg-base-100 border border-base-200 shadow-sm">
        <div className="card-body space-y-4">
          <div className="flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
            <div className="flex flex-col md:flex-row gap-3 md:items-end w-full">
              <div className="flex flex-col gap-1 w-full md:w-48">
                <label className="text-sm font-medium">Tipo</label>
                <select
                  className="select select-bordered h-10"
                  value={filters.type}
                  onChange={(e) => setFilters((prev) => ({ ...prev, type: e.target.value }))}
                >
                  <option value="ALL">Admins e Users</option>
                  <option value="ADMIN">Admins</option>
                  <option value="USER">Users</option>
                </select>
              </div>
              <div className="flex flex-col gap-1 w-full md:w-32">
                <label className="text-sm font-medium">Mês</label>
                <select
                  className="select select-bordered h-10"
                  value={filters.month}
                  onChange={(e) => setFilters((prev) => ({ ...prev, month: Number(e.target.value) }))}
                >
                  {monthOptions.map((m) => (
                    <option key={m} value={m}>
                      {m.toString().padStart(2, "0")}
                    </option>
                  ))}
                </select>
              </div>
              <div className="flex flex-col gap-1 w-full md:w-32">
                <label className="text-sm font-medium">Ano</label>
                <select
                  className="select select-bordered h-10"
                  value={filters.year}
                  onChange={(e) => setFilters((prev) => ({ ...prev, year: Number(e.target.value) }))}
                >
                  {yearOptions.map((y) => (
                    <option key={y} value={y}>
                      {y}
                    </option>
                  ))}
                </select>
              </div>
              <div className="flex flex-col gap-1 w-full md:w-64">
                <InputField
                  label="Pesquisa (credencial/email)"
                  value={filters.query}
                  onChange={(e) => setFilters((prev) => ({ ...prev, query: e.target.value }))}
                  placeholder="ex: admin1 ou user@dominio"
                  inputClassName="h-10"
                />
              </div>
            </div>
            <div className="flex gap-2">
              <Button
                label={loading ? "A pesquisar..." : "Pesquisar logs"}
                variant="primary"
                className="min-w-[150px]"
                disabled={loading}
                onClick={handleSearch}
              />
            </div>
          </div>

          {error && (
            <div className="alert alert-error">
              <span>{error}</span>
            </div>
          )}

          <div className="max-h-96 overflow-auto border border-base-200 rounded-xl">
            {loading ? (
              <div className="p-4 text-base-content/70">Carregando logs...</div>
            ) : logs === null ? (
              <div className="p-4 text-base-content/60">Pesquise para carregar logs.</div>
            ) : logs.length === 0 ? (
              <div className="p-4 text-base-content/70">Nenhum log encontrado.</div>
            ) : (
              <table className="table table-zebra w-full">
                <thead>
                  <tr>
                    <th>Data</th>
                    <th>Tipo</th>
                    <th>Ator</th>
                    <th>Ação</th>
                  </tr>
                </thead>
                <tbody>
                  {logs.map((log, idx) => (
                    <tr key={`${log.type}-${log.actor}-${log.timestamp}-${idx}`}>
                      <td className="whitespace-nowrap">
                        {new Date(log.timestamp).toLocaleString("pt-PT")}
                      </td>
                      <td className="uppercase text-xs font-semibold">{log.type}</td>
                      <td className="whitespace-nowrap">{log.actor}</td>
                      <td>{log.action}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      </div>
    </section>
  );
}
