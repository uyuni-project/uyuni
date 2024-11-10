import { hot } from "react-hot-loader/root";

import withPageWrapper from "components/general/with-page-wrapper";
import { Form, Text, Check } from "components/input";
import { Panel } from "components/panels/Panel";
import { TopPanel } from "components/panels/TopPanel";
import { AsyncButton } from "components/buttons";
import { showErrorToastr, showSuccessToastr } from "components/toastr/toastr";

import usePasswordPolicyApi from "manager/admin/password-policy/use-password-policy-api";
import { useState } from "react";

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
  isUyuni: boolean;
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
      isUyuni: props.isUyuni,
    },
    errors: {},
  });

  const { updatePolicy } = usePasswordPolicyApi();

  return (
    <TopPanel
      title={t("Server Configuration - Password Policy")}
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
                  showSuccessToastr(t("Password Policy Changed"))
                })
                .catch((error) => {
                  showErrorToastr(error, { autoHide: false });
                })
            }
          />
        </div>
      }
    >
      <div className="page-summary">
        <p>{t("Set up your server local users password policy.")}</p>
      </div>
      <Form
        model={policy.properties}
        errors={policy.errors}
        onChange={(newProperties) =>
          setPolicy({ ...policy, properties: newProperties })
        }
        >
        <Panel headingLevel="h2" title={t("Password Policy Settings")}>
          <div className="col-md-8">
            {/* Minimum Length */}
            <div className="row">
              <div className="col-md-4 text-left">
                <label htmlFor="minLength">{t("Min Password Length")}</label>
              </div>
              <Text
                required
                name="minLength"
                divClass="col-md-2"
                type="number"
              />
            </div>
            {/* Maximum Length */}
            <div className="row">
              <div className="col-md-4 text-left">
                <label htmlFor="minLength">{t("Max Password Length")}</label>
              </div>
              <Text
                required
                name="maxLength"
                divClass="col-md-2"
                type="number"
              />
            </div>
            {/* Require Digits */}
            <div className="row">
              <div className="col-md-4 text-left">
                <label htmlFor="digitsFlag">{t("Require Digits")}</label>
              </div>
              <Check
                name="digitsFlag"
                key="digitsFlag"
                divClass="col-md-2"
              />
            </div>
            {/* Require Lowercase Characters */}
            <div className="row">
              <div className="col-md-4 text-left">
                <label htmlFor="digitsFlag">{t("Require Lowercase Characters")}</label>
              </div>
              <Check
                 name="lowerCharFlag"
                 key="lowerCharFlag"
                 divClass="col-md-2"
               />
            </div>
            {/* Require Uppercase Characters */}
            <div className="row">
              <div className="col-md-4 text-left">
                <label htmlFor="digitsFlag">{t("Require Uppercase Characters")}</label>
              </div>
              <Check
                name="upperCharFlag"
                key="upperCharFlag"
                divClass="col-md-2"
                />
            </div>
            {/* Restrict Consecutive Characters */}
            <div className="row">
              <div className="col-md-4 text-left">
                <label htmlFor="digitsFlag">{t("Restrict Consecutive Characters")}</label>
              </div>
              <Check
                name="consecutiveCharFlag"
                key="consecutiveCharFlag"
                divClass="col-md-2"
              />
            </div>
            {/* Require Special Characters */}
            <div className="row">
              <div className="col-md-4 text-left">
                <label htmlFor="digitsFlag">{t("Require Special Characters")}</label>
              </div>   
              <Check
                name="specialCharFlag"
                key="specialCharFlag"
                divClass="col-md-2"
              />
            </div>
            {/* Allowed Special Characters */}
            <div className="row form-group">
              <div className="col-md-4 text-left">
                <label htmlFor="digitsFlag">{t("Allowed Special Characters")}</label>
              </div>
              <Text
                required={policy.properties.specialCharFlag}
                disabled={!policy.properties.specialCharFlag}
                name="specialCharList"
                divClass="col-md-4"
              />
            </div>
            {/* Restrict Character Occurrence */}
            <div className="row">
              <div className="col-md-4 text-left">
                <label htmlFor="digitsFlag">{t("Restrict Character Occurrence")}</label>
              </div>
              <Check
                key="restrictedOccurrenceFlag"
                name="restrictedOccurrenceFlag"
                divClass="col-md-2"  
              />
            </div>
            {/* Maximum Character Occurrence */}
            <div className="row form-group">
              <div className="col-md-4 text-left">
                <label htmlFor="digitsFlag">{t("Restrict Character Occurrence")}</label>
              </div>
              <Text
                required={policy.properties.restrictedOccurrenceFlag}
                disabled={!policy.properties.restrictedOccurrenceFlag}
                name="maxCharOccurrence"
                divClass="col-md-2"
                type="number"
              />
            </div>
          </div>
        </Panel>
      </Form>
    </TopPanel>
  );
};

export default hot(withPageWrapper(PasswordPolicy));
