export default {
  "reports/inventory": () => import("./inventory.renderer"),
  "reports/cveSearch": () => import("./cveSearch.renderer"),
};
