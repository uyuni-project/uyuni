import * as React from "react";

import { paygProperties } from "manager/admin/payg/payg";

import { Form, Password, Text } from "components/input";
import { InputBase } from "components/input/InputBase";

type PropsForm = {
  paygSshData: paygProperties;
  errors: any;
  onChange: Function;
  isInstance: boolean;
  editing?: boolean;
};

const PaygSshDataForm = (props: PropsForm) => {
  return (
    <Form
      model={props.paygSshData}
      errors={props.errors}
      onChange={(model) => {
        props.onChange(model);
      }}
    >
      <PaygSshDataFormFields paygSshData={props.paygSshData} isInstance={props.isInstance} editing={props.editing} />
    </Form>
  );
};

export default PaygSshDataForm;

type PropsFields = {
  paygSshData: paygProperties;
  isInstance: boolean;
  editing?: boolean;
};

export const PaygSshDataFormFields = (props: PropsFields) => {
  let prefix = props.isInstance ? "" : "bastion_";
  return (
    <React.Fragment>
      {props.editing && (
        <div className="alert alert-info" style={{ marginTop: "0px" }}>
          {t("When editing the SSH connection all needed credentials must be re-provided.")}
        </div>
      )}
      <div className="row">
        <Text
          required={props.isInstance}
          disabled={props.editing && props.isInstance}
          name={prefix + "host"}
          label={t("Host")}
          labelClass="col-md-2"
          divClass="col-md-10"
        />
      </div>
      <div className="row">
        <Text
          name={prefix + "port"}
          label={t("SSH Port")}
          labelClass="col-md-2"
          divClass="col-md-10"
          type={"number"}
          maxLength={4}
        />
      </div>
      <div className="row">
        <Text
          required={props.isInstance}
          name={prefix + "username"}
          label={t("User")}
          labelClass="col-md-2"
          divClass="col-md-10"
        />
      </div>
      <div className="row">
        <Password name={prefix + "password"} label={t("Password")} labelClass="col-md-2" divClass="col-md-10" />
      </div>
      <div className="row">
        <InputBase name={prefix + "key"} label={t("SSH Private Key")} labelClass="col-md-2" divClass="col-md-10">
          {({ setValue, onBlur }) => {
            const onChange = (event: any) => {
              setValue(event.target.name, event.target.value);
            };
            return (
              <textarea
                className={`form-control`}
                name={prefix + "key"}
                id={prefix + "key"}
                value={(props.isInstance ? props.paygSshData.key : props.paygSshData.bastion_key) || ""}
                onChange={onChange}
                onBlur={onBlur}
                rows={15}
              />
            );
          }}
        </InputBase>
      </div>
      <div className="row">
        <Password
          name={prefix + "key_password"}
          label={t("SSH Private Key Passphrase")}
          labelClass="col-md-2"
          divClass="col-md-10"
        />
      </div>
    </React.Fragment>
  );
};
