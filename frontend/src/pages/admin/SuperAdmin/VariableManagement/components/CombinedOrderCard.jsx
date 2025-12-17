import { useCallback, useEffect, useMemo, useState } from "react";
import { useAppHome } from "../../AppHome/AppHomeContext.jsx";
import { useVariableManagement } from "../VariableManagementContext.jsx";
import { updateUnifiedHome } from "../../../../../api/site/siteManagement.js";

function buildCombinedSections({ publicSections = [], appSections = [] }) {
  const heroPublic = publicSections.find((s) => s.type === "HERO");
  const heroAuth = appSections.find((s) => s.type === "HERO");
  const weeklyTip = appSections.find((s) => s.type === "WEEKLY_TIP");
  const industries = publicSections.find((s) => s.type === "INDUSTRIES");
  const partners = publicSections.find((s) => s.type === "PARTNERS");

  const items = [
    heroPublic || heroAuth
      ? {
          key: "HERO",
          title: (heroPublic || heroAuth)?.title || "Hero",
          subtitle: heroPublic?.subtitle || heroAuth?.subtitle || "",
          publicId: heroPublic?.id ?? null,
          appId: heroAuth?.id ?? null,
          active: Boolean(heroPublic?.active ?? heroAuth?.active),
          order: heroPublic?.displayOrder ?? heroAuth?.displayOrder ?? 0,
        }
      : null,
    weeklyTip
      ? {
          key: "WEEKLY_TIP",
          title: weeklyTip.title || "Dica da Semana",
          subtitle: weeklyTip.subtitle || weeklyTip.content || "",
          publicId: null,
          appId: weeklyTip.id,
          active: Boolean(weeklyTip.active),
          order: weeklyTip.displayOrder ?? 1,
        }
      : null,
    industries
      ? {
          key: "INDUSTRIES",
          title: industries.title || "Areas em que atuamos",
          subtitle: industries.subtitle || "",
          publicId: industries.id,
          appId: null,
          active: Boolean(industries.active),
          order: industries.displayOrder ?? 2,
        }
      : null,
    partners
      ? {
          key: "PARTNERS",
          title: partners.title || "Parceiros principais",
          subtitle: partners.subtitle || "",
          publicId: partners.id,
          appId: null,
          active: Boolean(partners.active),
          order: partners.displayOrder ?? 3,
        }
      : null,
  ].filter(Boolean);

  return items.sort((a, b) => (a.order ?? 0) - (b.order ?? 0));
}

export default function CombinedOrderCard() {
  const {
    sections: appSections = [],
    handleSectionToggle: handleAppToggle,
    retryAppHomeConfig,
  } = useAppHome();
  const {
    config,
    setBanner,
    handleSectionToggle: handlePublicToggle,
    retryHomepageConfig,
  } = useVariableManagement();

  const publicSections = useMemo(
    () => (Array.isArray(config?.sections) ? config.sections : []),
    [config?.sections]
  );

  const [combined, setCombined] = useState(() => buildCombinedSections({ publicSections, appSections }));

  useEffect(() => {
    setCombined(buildCombinedSections({ publicSections, appSections }));
  }, [publicSections, appSections]);

  const moveItem = useCallback((id, direction) => {
    setCombined((prev) => {
      const index = prev.findIndex((item) => item.key === id);
      if (index === -1) return prev;
      const targetIndex = direction === "up" ? index - 1 : index + 1;
      if (targetIndex < 0 || targetIndex >= prev.length) return prev;
      const next = [...prev];
      const [moved] = next.splice(index, 1);
      next.splice(targetIndex, 0, moved);
      return next.map((item, idx) => ({ ...item, order: idx }));
    });
  }, []);

  const handleToggle = useCallback(
    async (item) => {
      setBanner?.(null);
      const publicTarget = item.publicId
        ? publicSections.find((section) => section.id === item.publicId)
        : null;
      const appTarget = item.appId ? appSections.find((section) => section.id === item.appId) : null;

      try {
        if (publicTarget) {
          await handlePublicToggle(publicTarget);
        }
        if (appTarget) {
          await handleAppToggle(appTarget);
        }
      } catch (err) {
        setBanner?.({ type: "error", message: err.message || "Nao foi possivel alterar a visibilidade." });
      }
    },
    [appSections, publicSections, handlePublicToggle, handleAppToggle, setBanner]
  );

  const persistUnifiedOrder = useCallback(async () => {
    const publicIds = combined
      .map((item) => item.publicId)
      .filter((id) => id != null);
    const appIds = combined
      .map((item) => item.appId)
      .filter((id) => id != null);

    try {
      await updateUnifiedHome({ publicSectionIds: publicIds, authenticatedSectionIds: appIds });
      setBanner?.({ type: "success", message: "Ordem combinada guardada." });
      retryHomepageConfig?.({ force: true });
      retryAppHomeConfig?.();
    } catch (err) {
      setBanner?.({ type: "error", message: err.message || "Falha ao guardar a ordem combinada." });
    }
  }, [combined, retryAppHomeConfig, retryHomepageConfig, setBanner]);

  return (
    <div className="card bg-base-100 shadow-xl">
      <div className="card-body space-y-6">
        <div className="flex flex-col gap-1">
          <h2 className="card-title text-2xl">Ordem das seções</h2>
          <p className="text-base-content/70">
            Defina a sequência com que as seções aparecem para visitantes e utilizadores autenticados.
          </p>
        </div>

        <ol className="space-y-2">
          {combined.map((item, index) => (
            <li
              key={item.key}
              className="flex items-center justify-between gap-4 rounded-2xl border border-base-300 bg-base-100 px-4 py-3"
            >
              <div className="flex items-center gap-3">
                <span className="font-semibold text-primary cursor-grab select-none">{index + 1}.</span>
                <div>
                  <p className="font-semibold">{item.title || item.key}</p>
                  {item.subtitle && (
                    <p className="text-sm text-base-content/70 line-clamp-1">{item.subtitle}</p>
                  )}
                </div>
              </div>
              <div className="flex flex-wrap items-center gap-3">
                <label className="flex flex-col gap-1 items-start text-sm text-base-content/70">
                  <span>{item.active ? "Visível" : "Oculta"}</span>
                  <input
                    type="checkbox"
                    className="toggle toggle-primary"
                    checked={item.active}
                    onChange={() => handleToggle(item)}
                  />
                </label>
                <button
                  type="button"
                  className="btn btn-sm btn-ghost"
                  onClick={() => moveItem(item.key, "up")}
                  disabled={index === 0}
                >
                  <i className="bi bi-arrow-up" />
                </button>
                <button
                  type="button"
                  className="btn btn-sm btn-ghost"
                  onClick={() => moveItem(item.key, "down")}
                  disabled={index === combined.length - 1}
                >
                  <i className="bi bi-arrow-down" />
                </button>
              </div>
            </li>
          ))}
        </ol>

        <div className="flex justify-end">
          <button type="button" className="btn btn-primary" onClick={persistUnifiedOrder}>
            Guardar ordem combinada
          </button>
        </div>
      </div>
    </div>
  );
}
