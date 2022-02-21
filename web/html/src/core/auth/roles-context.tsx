// Note: to use this component you have to make sure the current user roles are injected with withRolesTemplate
// and jade mixin userRoles
import * as React from "react";

export type rolesType = Array<string>;
declare var global_userRoles: rolesType | undefined;

const RolesContext = React.createContext<rolesType>([]);

const RolesProvider = ({ children }: { children: React.ReactNode }) => (
  <RolesContext.Provider value={typeof global_userRoles !== "undefined" ? global_userRoles : []}>
    {children}
  </RolesContext.Provider>
);

export { RolesProvider, RolesContext };
