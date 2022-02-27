import * as React from "react";

import { paygProperties } from "manager/admin/payg/payg";

import { Form, Text } from "components/input";

type Props = {
  payg: paygProperties;
  errors: any;
  onChange: Function;
  editing?: boolean;
};
const PaygInfoForm = (props: Props) => {
  return (
    <Form
      model={props.payg}
      errors={props.errors}
      onChange={(model) => {
        props.onChange(model);
      }}
    >
      <div className="row">
        <Text required name="description" label={t("Description")} labelClass="col-md-2" divClass="col-md-10" />
      </div>
    </Form>
  );
};

export default PaygInfoForm;
