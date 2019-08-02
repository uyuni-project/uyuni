// @flow
// Note: To use this component make sure it's used in a placed wrapped by the roles-context-provider
import {useContext} from 'react';
import {UserLocalizationContext} from "core/user-localization/user-localization-context";

const useUserLocalization = () => useContext(UserLocalizationContext);

export default useUserLocalization;
