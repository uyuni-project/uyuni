export default {
  "audit/coco": () => import("./coco/coco-global-scans-list.renderer"),
  "audit/cveaudit": () => import("./cveaudit/cveaudit"),
  "audit/subscription-matching": () => import("./subscription-matching/subscription-matching"),
  "audit/list-tailoring-files":() => import("./scap/list-tailoring-files"),
  "audit/create-tailoring-file":() => import("./scap/create-tailoring-file"),
};
