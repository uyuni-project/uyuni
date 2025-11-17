import { Fragment } from "react";

import { PaygFullType } from "manager/admin/payg/payg";
import PaygStatus from "manager/admin/payg-shared/common/payg-status";

type Props = {
  payg: PaygFullType;
};

const PaygInfoView = (props: Props) => {
  return (
    <Fragment>
      <dl className="row">
        <dt className="col-2">{t("Description")}</dt>
        <dd className="col-10">{props.payg.properties.description}</dd>
      </dl>
      <dl className="row">
        <dt className="col-2">{t("Status")}</dt>
        <dd className="col-10">
          <PaygStatus status={props.payg.status} statusMessage={props.payg.statusMessage} />
        </dd>
      </dl>
      <dl className="row">
        <dt className="col-2">{t("Last Status Update")}</dt>
        <dd className="col-10">{props.payg.lastChange}</dd>
      </dl>
    </Fragment>
  );
};

export default PaygInfoView;
