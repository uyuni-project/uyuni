// @flow
// Note: To use this component make sure it's used in a placed wrapped by the roles-context-provider
import React, {useContext} from 'react';
import {RolesContext} from './roles-context';

const useRoles = () => useContext(RolesContext);

export default useRoles;
