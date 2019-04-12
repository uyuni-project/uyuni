import type {rolesType} from "./roles-context";

export function isOrgAdmin(roles: rolesType) {
  return roles.includes("org_admin");
}
