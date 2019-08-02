// @flow
// Note: to use this component you have to make sure the current user roles are injected with withRolesTemplate
// and jade mixin userRoles
import React from 'react';
import type {Node} from 'react';

export type userLocalizationType = {
  timezone: string,
  localTime: string,
};
declare var global_user_localization: userLocalizationType;

const UserLocalizationContext = React.createContext<userLocalizationType>({});

const UserLocalizationProvider = ({children}: {children: Node}) =>
  <UserLocalizationContext.Provider value={typeof global_user_localization !== 'undefined' ? global_user_localization : {}}>
    {children}
  </UserLocalizationContext.Provider>

export { UserLocalizationProvider, UserLocalizationContext};
