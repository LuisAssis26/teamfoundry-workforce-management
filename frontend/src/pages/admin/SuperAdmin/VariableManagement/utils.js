export function sortByName(list = []) {
  return [...list].sort((a, b) => (a?.name || "").localeCompare(b?.name || ""));
}

export function moveItemInList(list, id, direction) {
  const index = list.findIndex((item) => item.id === id);
  if (index < 0) return null;
  const target = direction === "up" ? index - 1 : index + 1;
  if (target < 0 || target >= list.length) return null;
  const next = [...list];
  const [removed] = next.splice(index, 1);
  next.splice(target, 0, removed);
  return next;
}

export function sortByOrder(items = []) {
  return [...items].sort((a, b) => (a.displayOrder ?? 0) - (b.displayOrder ?? 0));
}

export function normalizeHomepageConfig(payload) {
  return {
    sections: sortByOrder(payload?.sections ?? []),
    industries: sortByOrder(payload?.industries ?? []),
    partners: sortByOrder(payload?.partners ?? []),
  };
}

export function normalizeAppHomeConfig(payload) {
  return {
    sections: sortByOrder(payload?.sections ?? []),
  };
}

export function sortWeeklyTips(list = []) {
  return sortByOrder(list).map((item, index) => ({
    ...item,
    displayOrder: item.displayOrder ?? index,
  }));
}
