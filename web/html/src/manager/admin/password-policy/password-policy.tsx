import { hot } from "react-hot-loader/root";

import { useState } from "react";

import { AsyncButton } from "components/buttons";
import withPageWrapper from "components/general/with-page-wrapper";
import { Check, Form, Text } from "components/input";
import { Panel } from "components/panels/Panel";
import { TopPanel } from "components/panels/TopPanel";
import { MessagesContainer, showErrorToastr, showSuccessToastr } from "components/toastr/toastr";

import Network from "utils/network";

import { PasswordPolicyData, PasswordPolicyProps } from "./password_policy_type";

const PasswordPolicy = (prop: PasswordPolicyProps) => {
  const policy_endpoint = "/rhn/manager/api/admin/config/password-policy";
  const [defaults, setDefaults] = useState(prop.defaults);
  const [policy, setPolicy] = useState(prop.policy);

  return (
    <TopPanel title={t("Server Configuration - Password Policy")} icon="fa-info-circle">
      <div className="page-summary">
        <MessagesContainer />
        <p>{t("Set up your server local users password policy.")}</p>
      </div>
      <Form model={policy}>
        <Panel headingLevel="h2" title={t("Password Policy Settings")}>
          <div className="col-md-8">
            {/* Minimum Length */}
            <div className="row">
              <div className="col-md-4 text-left">
                <label htmlFor="minLength">{t("Min Password Length")}</label>
              </div>
              <Text required name="minLength" divClass="col-md-2" type="number" />
            </div>
            {/* Maximum Length */}
            <div className="row">
              <div className="col-md-4 text-left">
                <label htmlFor="maxLength">{t("Max Password Length")}</label>
              </div>
              <Text required name="maxLength" divClass="col-md-2" type="number" />
            </div>
            {/* Require Digits */}
            <div className="row">
              <div className="col-md-4 text-left">
                <label htmlFor="digitFlag">{t("Require Digits")}</label>
              </div>
              <Check name="digitFlag" key="digitFlag" divClass="col-md-2" />
            </div>
            {/* Require Lowercase Characters */}
            <div className="row">
              <div className="col-md-4 text-left">
                <label htmlFor="lowerCharFlag">{t("Require Lowercase Characters")}</label>
              </div>
              <Check name="lowerCharFlag" key="lowerCharFlag" divClass="col-md-2" />
            </div>
            {/* Require Uppercase Characters */}
            <div className="row">
              <div className="col-md-4 text-left">
                <label htmlFor="upperCharFlag">{t("Require Uppercase Characters")}</label>
              </div>
              <Check name="upperCharFlag" key="upperCharFlag" divClass="col-md-2" />
            </div>
            {/* Restrict Consecutive Characters */}
            <div className="row">
              <div className="col-md-4 text-left">
                <label htmlFor="consecutiveCharsFlag">{t("Restrict Consecutive Characters")}</label>
              </div>
              <Check name="consecutiveCharsFlag" key="consecutiveCharsFlag" divClass="col-md-2" />
            </div>
            {/* Require Special Characters */}
            <div className="row">
              <div className="col-md-4 text-left">
                <label htmlFor="specialCharFlag">{t("Require Special Characters")}</label>
              </div>
              <Check name="specialCharFlag" key="specialCharFlag" divClass="col-md-2" />
            </div>
            {/* Allowed Special Characters */}
            <div className="row form-group">
              <div className="col-md-4 text-left">
                <label htmlFor="specialChars">{t("Allowed Special Characters")}</label>
              </div>
              <Text
                disabled={!policy.specialCharFlag}
                name="specialChars"
                divClass="col-md-4"
                defaultValue={defaults.specialChars?.toLocaleString()}
              />
            </div>
            {/* Restrict Character Occurrence */}
            <div className="row">
              <div className="col-md-4 text-left">
                <label htmlFor="restrictedOccurrenceFlag">{t("Restrict Characters Occurrences")}</label>
              </div>
              <Check key="restrictedOccurrenceFlag" name="restrictedOccurrenceFlag" divClass="col-md-2" />
            </div>
            {/* Maximum Character Occurrence */}
            <div className="row form-group">
              <div className="col-md-4 text-left">
                <label htmlFor="maxCharacterOccurrence">{t("Max Characters Occurrences")}</label>
              </div>
              <Text
                disabled={!policy.restrictedOccurrenceFlag}
                name="maxCharacterOccurrence"
                divClass="col-md-2"
                type="number"
                defaultValue={defaults.maxCharacterOccurrence.toLocaleString()}
              />
            </div>
            <div className="row">
              <div className="col-md-4 text-left"></div>
              <div className="col-md-4 text-left">
                <div className="btn-group">
                  <AsyncButton
                    id="saveButton"
                    className="btn-primary btn-large"
                    title={t("Save Password Policy")}
                    text={t("Save")}
                    icon="fa-save"
                    action={() => {
                      Network.post(policy_endpoint, policy)
                        .then(() => {
                          showSuccessToastr(t("Password Policy Changed"));
                        })
                        .catch((error) => {
                          showErrorToastr(error);
                        });
                    }}
                  />
                  <AsyncButton
                    id="resetButton"
                    className="btn-secondary btn-large"
                    title={t("Reset")}
                    text={t("Reset")}
                    icon="fa-refresh"
                    action={() => {
                      const default_policy_endpoint = "/rhn/manager/api/admin/config/password-policy/default";
                      Network.get(default_policy_endpoint)
                        .then((resp) => {
                          const defaults: PasswordPolicyData = JSON.parse(resp.data);
                          Network.post(policy_endpoint, defaults)
                            .then(() => showSuccessToastr(t("Password Policy Reset to Default")))
                            .catch((error) => showErrorToastr(error));
                          setDefaults(defaults);
                          setPolicy(defaults);
                        })
                        .catch((error) => {
                          showErrorToastr(error);
                        });
                    }}
                  />
                </div>
              </div>
            </div>
          </div>
        </Panel>
      </Form>
    </TopPanel>
  );
};

export default hot(withPageWrapper(PasswordPolicy));
