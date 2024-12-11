import PasswordPolicy from "./password-policy";
import { PasswordPolicyData } from "./password_policy_type";
import SpaRenderer from "core/spa/spa-renderer";

export const renderer = (id: string, policy: PasswordPolicyData, defaults: PasswordPolicyData) => {
  SpaRenderer.renderNavigationReact(
    <PasswordPolicy policy={policy} defaults={defaults} />,
    document.getElementById(id)
  );
};
