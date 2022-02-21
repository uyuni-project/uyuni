// Note: To use this component make sure it's used in a page wrapped by the UserLocalizationProvider
// (user-localization-context.js)
import { useContext } from "react";

import { UserLocalizationContext } from "core/user-localization/user-localization-context";

const useUserLocalization = () => useContext(UserLocalizationContext);

export default useUserLocalization;
