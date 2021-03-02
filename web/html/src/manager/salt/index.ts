export default {
  "salt/cmd/remote-commands": () => import("./cmd/remote-commands"),
  "salt/formula-catalog/org-formula-catalog": () => import("./formula-catalog/org-formula-catalog.renderer"),
  "salt/formula-catalog/org-formula-details": () => import("./formula-catalog/org-formula-details"),
  "salt/keys/key-management": () => import("./keys/key-management"),
};
