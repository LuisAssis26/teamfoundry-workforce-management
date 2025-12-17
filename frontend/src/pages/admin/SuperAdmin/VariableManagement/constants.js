export const SECTION_LABELS = {
  HERO: "Hero (topo)",
  INDUSTRIES: "Areas em que atuamos",
  PARTNERS: "Parceiros",
};

export const VIEW_TABS = [
  {
    id: "home",
    label: "Home",
    description: "Hero autenticado + hero publico, areas e parceiros.",
  },
  {
    id: "weeklyTips",
    label: "Dicas da semana",
    description: "Sugestoes rapidas para destacar na plataforma.",
  },
  {
    id: "globalVars",
    label: "Variaveis globais",
    description: "Texto e links reutilizados em varias paginas.",
  },
];

export const DEFAULT_GLOBAL_OPTIONS = {
  functions: [],
  competences: [],
  geoAreas: [],
  activitySectors: [],
};

export const OPTION_LABELS = {
  functions: "funcao",
  competences: "competencia",
  geoAreas: "area geografica",
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
