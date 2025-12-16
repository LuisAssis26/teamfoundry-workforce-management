export const SECTION_LABELS = {
  HERO: "Hero (topo)",
  INDUSTRIES: "Indústrias",
  PARTNERS: "Parceiros",
};

export const VIEW_TABS = [
  {
    id: "publicHome",
    label: "Home pública",
    description: "Configura a landing page visível antes do login.",
  },
  {
    id: "appHome",
    label: "Home autenticada",
    description: "Conteúdo mostrado quando o utilizador já fez login.",
  },
  {
    id: "weeklyTips",
    label: "Dicas da semana",
    description: "Sugestões rápidas para destacar na plataforma.",
  },
  {
    id: "globalVars",
    label: "Variáveis globais",
    description: "Texto e links reutilizados em várias páginas.",
  },
];

export const DEFAULT_GLOBAL_OPTIONS = {
  functions: [],
  competences: [],
  geoAreas: [],
  activitySectors: [],
};

export const OPTION_LABELS = {
  functions: "função",
  competences: "competência",
  geoAreas: "área geográfica",
  activitySectors: "setor de atividade",
};

export const EMPTY_FORMS = {
  industry: {
    name: "",
    description: "",
    imageUrl: "",
    linkUrl: "",
    active: true,
  },
  partner: {
    name: "",
    description: "",
    imageUrl: "",
    websiteUrl: "",
    active: true,
  },
};
