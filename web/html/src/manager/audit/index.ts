export default {
  "audit/cveaudit": () => import("./cveaudit/cveaudit"),
  "audit/subscription-matching": () => import("./subscription-matching/subscription-matching"),
};
