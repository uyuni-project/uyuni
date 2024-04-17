export default {
  "audit/coco": () => import("./coco/coco-global-scans-list.renderer"),
  "audit/cveaudit": () => import("./cveaudit/cveaudit"),
  "audit/subscription-matching": () => import("./subscription-matching/subscription-matching"),
};
