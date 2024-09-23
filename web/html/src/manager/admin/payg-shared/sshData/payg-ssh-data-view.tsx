import * as React from "react";

import { paygProperties } from "manager/admin/payg/payg";

const passHidden = "*****";
type Props = {
  payg: paygProperties;
  isInstance: boolean;
};

const PaygShhDataView = (props: Props) => {
  return (
    <div>
      <dl className="row">
        <dt className="col-2 col-xs-2">{t("Host")}</dt>
        <dd className="col-10 col-xs-10">{props.isInstance ? props.payg.host : props.payg.bastion_host}</dd>
      </dl>
      <dl className="row">
        <dt className="col-2 col-xs-2">{t("SSH Port")}</dt>
        <dd className="col-10 col-xs-10">{props.isInstance ? props.payg.port : props.payg.bastion_port}</dd>
      </dl>
      <dl className="row">
        <dt className="col-2 col-xs-2">{t("User")}</dt>
        <dd className="col-10 col-xs-10">{props.isInstance ? props.payg.username : props.payg.bastion_username}</dd>
      </dl>
      <dl className="row">
        <dt className="col-2 col-xs-2">{t("Password")}</dt>
        <dd className="col-10 col-xs-10">
          {(props.isInstance ? props.payg.password : props.payg.bastion_password) || passHidden}
        </dd>
      </dl>
      <dl className="row">
        <dt className="col-2 col-xs-2">{t("SSH Private Key")}</dt>
        <dd className="col-10 col-xs-10" style={{ whiteSpace: "pre-wrap" }}>
          {(props.isInstance ? props.payg.key : props.payg.bastion_key) || passHidden}
        </dd>
      </dl>
      <dl className="row">
        <dt className="col-2 col-xs-2">{t("SSH Private Key Passphrase")}</dt>
        <dd className="col-10 col-xs-10">
          {(props.isInstance ? props.payg.key_password : props.payg.bastion_key_password) || passHidden}
        </dd>
      </dl>
    </div>
  );
};

export default PaygShhDataView;
