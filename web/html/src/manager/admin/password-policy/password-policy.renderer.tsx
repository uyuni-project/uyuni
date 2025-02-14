import SpaRenderer from "core/spa/spa-renderer";

import { PasswordPolicyData } from "./password_policy_type";
import PasswordPolicy from "./password-policy";

export const renderer = (id: string, policy: PasswordPolicyData, defaults: PasswordPolicyData) => {
  SpaRenderer.renderNavigationReact(
    <PasswordPolicy policy={policy} defaults={defaults} />,
    document.getElementById(id)
  );
};
