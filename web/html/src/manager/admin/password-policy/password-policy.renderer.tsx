import SpaRenderer from "core/spa/spa-renderer";
import PasswordPolicy from "./password-policy";
import { PasswordPolicyData } from "./password_policy_type";

export const renderer = (
  id: string, policy: PasswordPolicyData, defaults: PasswordPolicyData
) => {
  SpaRenderer.renderNavigationReact(
    <PasswordPolicy 
    policy={policy} 
    defaults={defaults}
    />,
    document.getElementById(id)
  );
};
