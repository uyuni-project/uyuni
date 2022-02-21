// Note: to use this component you have to make sure the current user localization are injected with
// the jade mixin userLocalization
import * as React from "react";

export type userLocalizationType = {
  timezone: string;
  localTime: string;
};
declare var global_user_localization: userLocalizationType | undefined;

const UserLocalizationContext = React.createContext<Partial<userLocalizationType>>({});

const UserLocalizationProvider = ({ children }: { children: React.ReactNode }) => (
  <UserLocalizationContext.Provider
    value={typeof global_user_localization !== "undefined" ? global_user_localization : {}}
  >
    {children}
  </UserLocalizationContext.Provider>
);

export { UserLocalizationProvider, UserLocalizationContext };
