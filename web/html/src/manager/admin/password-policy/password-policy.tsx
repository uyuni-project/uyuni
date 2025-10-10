import { useState } from "react";

import { AsyncButton } from "components/buttons";
import withPageWrapper from "components/general/with-page-wrapper";
import { DEPRECATED_Check, Form, Text } from "components/input";
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
      <Form model={policy} onChange={(model) => setPolicy({ ...model })}>
        <Panel headingLevel="h2" title={t("Password Policy Settings")}>
          {/* Minimum Length */}
          <Text
            label={t("Min Password Length")}
            required
            name="minLength"
            labelClass="col-md-3"
            divClass="col-md-2"
            type="number"
          />
          {/* Maximum Length */}
          <Text
            label={t("Max Password Length")}
            required
            name="maxLength"
            labelClass="col-md-3"
            divClass="col-md-2"
            type="number"
          />
          <div className="row">
            <div className="col-md-3 text-right">
              <label className="control-label">Password Complexity:</label>
            </div>
            <div className="col-md-8">
              {/* Require Digits */}
              <DEPRECATED_Check label={t("Require Digits")} name="digitFlag" key="digitFlag" divClass="col-md-6" />
              {/* Require Lowercase Characters */}
              <DEPRECATED_Check
                label={t("Require Lowercase Characters")}
                name="lowerCharFlag"
                key="lowerCharFlag"
                divClass="col-md-6"
              />
              {/* Require Uppercase Characters */}
              <DEPRECATED_Check
                label={t("Require Uppercase Characters")}
                name="upperCharFlag"
                key="upperCharFlag"
                divClass="col-md-6"
              />
              {/* Restrict Consecutive Characters */}
              <DEPRECATED_Check
                label={t("Restrict Consecutive Characters")}
                name="consecutiveCharsFlag"
                key="consecutiveCharsFlag"
                divClass="col-md-6"
              />
              {/* Require Special Characters */}
              <DEPRECATED_Check
                label={t("Require Special Characters")}
                name="specialCharFlag"
                key="specialCharFlag"
                divClass="col-md-6"
              />
            </div>
          </div>

          {/* Allowed Special Characters */}
          <Text
            label={t("Allowed Special Characters")}
            disabled={!policy.specialCharFlag}
            name="specialChars"
            labelClass="col-md-3 text-left"
            divClass="col-md-3"
            defaultValue={defaults.specialChars?.toLocaleString()}
          />
          {/* Restrict Character Occurrence */}
          <DEPRECATED_Check
            label={t("Restrict Characters Occurrences")}
            key="restrictedOccurrenceFlag"
            name="restrictedOccurrenceFlag"
            divClass="col-md-6 col-md-offset-3 offset-md-3"
          />
          {/* Maximum Character Occurrence */}
          <Text
            label={t("Max Characters Occurrences")}
            disabled={!policy.restrictedOccurrenceFlag}
            name="maxCharacterOccurrence"
            labelClass="col-md-3 text-left"
            divClass="col-md-2"
            type="number"
            defaultValue={defaults.maxCharacterOccurrence.toLocaleString()}
          />
          <div className="row">
            <div className="col-md-4 text-left col-md-offset-3 offset-md-3">
              <div className="btn-group">
                <AsyncButton
                  id="saveButton"
                  className="btn-primary btn-large"
                  text={t("Save")}
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
                  text={t("Reset")}
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
        </Panel>
      </Form>
    </TopPanel>
  );
};

export default withPageWrapper(PasswordPolicy);
