import { hot } from "react-hot-loader/root";
import * as React from "react";
import { useState } from "react";

import withPageWrapper from "components/general/with-page-wrapper";
import { Form, Text } from "components/input";
import { Panel } from "components/panels/Panel";
import { TopPanel } from "components/panels/TopPanel";
import { AsyncButton } from "components/buttons";
import { showErrorToastr } from "components/toastr/toastr";

// Assume you have an API hook similar to useLifecyclePaygActionsApi
import usePasswordPolicyApi from "manager/admin/password-policy/use-password-policy-api";

type PasswordPolicyProps = {
  minLength: bigint;
  maxLength: bigint;
  digitsFlag: boolean;
  lowerCharFlag: boolean;
  upperCharFlag: boolean;
  consecutiveCharFlag: boolean;
  specialCharFlag: boolean;
  specialCharList: string | null;
  restrictedOccurrenceFlag: boolean;
  maxCharOccurrence: bigint;
};

const PasswordPolicy = (props: PasswordPolicyProps) => {
  const [policy, setPolicy] = useState({
    properties: {
      minLength: props.minLength.toString(),
      maxLength: props.maxLength.toString(),
      digitsFlag: props.digitsFlag,
      lowerCharFlag: props.lowerCharFlag,
      upperCharFlag: props.upperCharFlag,
      consecutiveCharFlag: props.consecutiveCharFlag,
      specialCharFlag: props.specialCharFlag,
      specialCharList: props.specialCharList || "",
      restrictedOccurrenceFlag: props.restrictedOccurrenceFlag,
      maxCharOccurrence: props.maxCharOccurrence.toString(),
    },
    errors: {},
  });

  const { updatePolicy } = usePasswordPolicyApi();

  return (
    <TopPanel
      title={t("SUSE Manager Configuration - Password Policy")}
      icon="fa-info-circle"
      button={
        <div className="pull-right btn-group">
          <AsyncButton
            id="savebutton"
            className="btn-primary"
            title={t("Save Password Policy")}
            text={t("Save")}
            icon="fa-save"
            action={() =>
              updatePolicy(policy.properties)
                .then(() => {
                  // Handle success, maybe show a success message
                })
                .catch((error) => {
                  setPolicy({ ...policy, errors: error.errors });
                  showErrorToastr(error.messages, { autoHide: false });
                })
            }
          />
        </div>
      }
    >
      <div className="page-summary">
        <p>{t("Set up your SUSE Manager server local users password policy.")}</p>
      </div>
      <Form
        model={policy.properties}
        errors={policy.errors}
        onChange={(newProperties) => setPolicy({ ...policy, properties: newProperties })}
      >
        <Panel headingLevel="h2" title={t("Password Policy Settings")}>
          <div className="col-md-10">
            {/* Minimum Length */}
            <div className="row">
              <Text
                required
                name="minLength"
                label={t("Minimum Length")}
                labelClass="col-md-4 text-left"
                divClass="col-md-8"
                type="number"
              />
            </div>
            {/* Maximum Length */}
            <div className="row">
              <Text
                required
                name="maxLength"
                label={t("Maximum Length")}
                labelClass="col-md-4 text-left"
                divClass="col-md-8"
                type="number"
              />
            </div>
            {/* Require Digits */}
            <div className="row form-group">
              <label className="col-md-4 text-left">{t("Require Digits")}</label>
              <div className="col-md-8">
                <input
                  type="checkbox"
                  name="digitsFlag"
                  checked={policy.properties.digitsFlag}
                  onChange={(e) =>
                    setPolicy({
                      ...policy,
                      properties: { ...policy.properties, digitsFlag: e.target.checked },
                    })
                  }
                />
              </div>
            </div>
            {/* Require Lowercase Characters */}
            <div className="row form-group">
              <label className="col-md-4 text-left">{t("Require Lowercase Characters")}</label>
              <div className="col-md-8">
                <input
                  type="checkbox"
                  name="lowerCharFlag"
                  checked={policy.properties.lowerCharFlag}
                  onChange={(e) =>
                    setPolicy({
                      ...policy,
                      properties: { ...policy.properties, lowerCharFlag: e.target.checked },
                    })
                  }
                />
              </div>
            </div>
            {/* Require Uppercase Characters */}
            <div className="row form-group">
              <label className="col-md-4 text-left">{t("Require Uppercase Characters")}</label>
              <div className="col-md-8">
                <input
                  type="checkbox"
                  name="upperCharFlag"
                  checked={policy.properties.upperCharFlag}
                  onChange={(e) =>
                    setPolicy({
                      ...policy,
                      properties: { ...policy.properties, upperCharFlag: e.target.checked },
                    })
                  }
                />
              </div>
            </div>
            {/* Restrict Consecutive Characters */}
            <div className="row form-group">
              <label className="col-md-4 text-left">{t("Restrict Consecutive Characters")}</label>
              <div className="col-md-8">
                <input
                  type="checkbox"
                  name="consecutiveCharFlag"
                  checked={policy.properties.consecutiveCharFlag}
                  onChange={(e) =>
                    setPolicy({
                      ...policy,
                      properties: { ...policy.properties, consecutiveCharFlag: e.target.checked },
                    })
                  }
                />
              </div>
            </div>
            {/* Require Special Characters */}
            <div className="row form-group">
              <label className="col-md-4 text-left">{t("Require Special Characters")}</label>
              <div className="col-md-8">
                <input
                  type="checkbox"
                  name="specialCharFlag"
                  checked={policy.properties.specialCharFlag}
                  onChange={(e) =>
                    setPolicy({
                      ...policy,
                      properties: { ...policy.properties, specialCharFlag: e.target.checked },
                    })
                  }
                />
              </div>
            </div>
            {/* Special Characters List */}
            {policy.properties.specialCharFlag && (
              <div className="row">
                <Text
                  required
                  name="specialCharList"
                  label={t("Allowed Special Characters")}
                  labelClass="col-md-4 text-left"
                  divClass="col-md-8"
                />
              </div>
            )}
            {/* Restrict Character Occurrence */}
            <div className="row form-group">
              <label className="col-md-4 text-left">{t("Restrict Character Occurrence")}</label>
              <div className="col-md-8">
                <input
                  type="checkbox"
                  name="restrictedOccurrenceFlag"
                  checked={policy.properties.restrictedOccurrenceFlag}
                  onChange={(e) =>
                    setPolicy({
                      ...policy,
                      properties: { ...policy.properties, restrictedOccurrenceFlag: e.target.checked },
                    })
                  }
                />
              </div>
            </div>
            {/* Maximum Character Occurrence */}
            {policy.properties.restrictedOccurrenceFlag && (
              <div className="row">
                <Text
                  required
                  name="maxCharOccurrence"
                  label={t("Maximum Character Occurrence")}
                  labelClass="col-md-4 text-left"
                  divClass="col-md-8"
                  type="number"
                />
              </div>
            )}
          </div>
        </Panel>
      </Form>
    </TopPanel>
  );
};

export default hot(withPageWrapper(PasswordPolicy));
