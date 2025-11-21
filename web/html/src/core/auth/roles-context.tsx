// Note: to use this component you have to make sure the current user roles are injected with withRolesTemplate
// and jade mixin userRoles
import { type ReactNode, createContext } from "react";
export type rolesType = string[];
declare global {
  var global_userRoles: rolesType | undefined;
}

const RolesContext = createContext<rolesType>([]);

const RolesProvider = ({ children }: { children: ReactNode }) => (
  <RolesContext.Provider value={typeof global_userRoles !== "undefined" ? global_userRoles : []}>
    {children}
  </RolesContext.Provider>
);

export { RolesProvider, RolesContext };
