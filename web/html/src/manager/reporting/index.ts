export default {
  "reporting/inventory": () => import("./inventory.renderer"),
  "reporting/cveSearch": () => import("./cveSearch.renderer"),
};
