import { Check, Form, Text } from "components/input";
import { MessagesContainer, showErrorToastr, showSuccessToastr } from "components/toastr/toastr";

import { AsyncButton } from "components/buttons";
import { Panel } from "components/panels/Panel";
import { PasswordPolicyProps } from "./password_policy_type";
import { TopPanel } from "components/panels/TopPanel";
import { hot } from "react-hot-loader/root";
import withPageWrapper from "components/general/with-page-wrapper";

const updatePolicy = (policyData) => {
  return fetch("/rhn/manager/api/admin/config/password-policy", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(policyData),
  }).then((response) => {
    if (!response.ok) {
      return response.json().then((errorData) => {
        throw errorData;
      });
    }
    return response.json();
  });
};

const defaultPolicy = () => {
  return fetch("/rhn/manager/api/admin/config/password-policy/default", {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
    },
  }).then((response) => {
    if (!response.ok) {
      return response.json().then((errorData) => {
        throw errorData;
      });
    }
    return response.json();
  });
};

const PasswordPolicy = (prop: PasswordPolicyProps) => {
  return (
    <TopPanel title={t("Server Configuration - Password Policy")} icon="fa-info-circle">
      <div className="page-summary">
        <MessagesContainer />
        <p>{t("Set up your server local users password policy.")}</p>
      </div>
      <Form model={prop.policy}>
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
              <Text disabled={!prop.policy.specialCharFlag}
                name="specialChars"
                divClass="col-md-4"
                defaultValue={prop.defaults.specialChars?.toLocaleString()}
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
                disabled={!prop.policy.restrictedOccurrenceFlag}
                name="maxCharacterOccurrence"
                divClass="col-md-2"
                type="number"
                defaultValue={prop.defaults.maxCharacterOccurrence.toLocaleString()}
              />
            </div>
          </div>
          <div className="row">
            <div className="col-md-4 text-center">
              <div className="btn-group">
                <AsyncButton
                  id="saveButton"
                  className="btn-primary btn-large"
                  title={t("Save Password Policy")}
                  text={t("Save")}
                  icon="fa-save"
                  action={() =>
                    updatePolicy(prop.policy)
                      .then(() => {
                        showSuccessToastr(t("Password Policy Changed"));
                      })
                      .catch((error) => {
                        showErrorToastr(error, { autoHide: false });
                      })
                  }
                />
              </div>
            </div>
            <div className="col-md-4 text-center">
              <div className="btn-group">
                <AsyncButton
                  id="resetButton"
                  className="btn-primary btn-large"
                  title={t("Reset")}
                  text={t("Reset")}
                  icon="fa-refresh"
                  action={() =>
                    defaultPolicy()
                      .then((policy) => {
                        prop.policy = policy;
                        showSuccessToastr(t("Password Policy reset to defaults"));
                      })
                      .catch((error) => {
                        showErrorToastr(error, { autoHide: false });
                      })
                  }
                />
              </div>
            </div>
          </div>
        </Panel>
      </Form>
    </TopPanel>
  );
};

export default hot(withPageWrapper(PasswordPolicy));
