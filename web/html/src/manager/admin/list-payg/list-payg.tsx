import { hot } from "react-hot-loader/root";

import * as React from "react";
import { useEffect } from "react";

import _truncate from "lodash/truncate";

import PaygStatus from "manager/admin/payg-shared/common/payg-status";

import { Button } from "components/buttons";
import { FromNow } from "components/datetime/FromNow";
import withPageWrapper from "components/general/with-page-wrapper";
import { ServerMessageType } from "components/messages";
import { SectionToolbar } from "components/section-toolbar/section-toolbar";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table } from "components/table/Table";
import { showSuccessToastr } from "components/toastr/toastr";
import { HelpLink } from "components/utils";

import { Utils } from "utils/functions";

const _SETUP_WIZARD_STEPS = [
  {
    id: "wizard-step-proxy",
    label: "HTTP Proxy",
    url: "/rhn/admin/setup/ProxySettings.do",
    active: false,
  },
  {
    id: "wizard-step-credentials",
    label: "Organization Credentials",
    url: "/rhn/admin/setup/MirrorCredentials.do",
    active: false,
  },
  {
    id: "wizard-step-suse-products",
    label: "Products",
    url: "/rhn/manager/admin/setup/products",
    active: false,
  },
  {
    id: "wizard-step-suse-payg",
    label: "Pay-as-you-go",
    url: "/rhn/manager/admin/setup/payg",
    active: true,
  },
];

type PaygOverviewType = {
  id: String;
  host: String;
  description: String;
  status: String;
  statusMessage: String;
  lastChange: moment.Moment;
};

type Props = {
  payg_instances: Array<PaygOverviewType>;
  flashMessage?: ServerMessageType;
};
const ListPayg = (props: Props) => {
  useEffect(() => {
    if (props.flashMessage) {
      showSuccessToastr(props.flashMessage);
    }
  }, []);

  const searchData = (row, criteria) => {
    const keysToSearch = ["host", "description", "statusMessage"];
    if (criteria) {
      const needle = criteria.toLocaleLowerCase();
      return keysToSearch.map((key) => row[key]).some((item) => item.toLocaleLowerCase().includes(needle));
    }
    return true;
  };

  const title = (
    <div className="spacewalk-toolbar-h1">
      <h1>
        <i className="fa fa-cogs"></i>
        &nbsp;
        {t("Setup Wizard")}
        &nbsp;
        <HelpLink url="reference/admin/setup-wizard.html" />
      </h1>
    </div>
  );

  const tabs = (
    <div className="spacewalk-content-nav">
      <ul className="nav nav-tabs">
        {_SETUP_WIZARD_STEPS.map((step) => (
          <li key={step.id} className={step.active ? "active" : ""}>
            <a className="js-spa" href={step.url}>
              {t(step.label)}
            </a>
          </li>
        ))}
      </ul>
    </div>
  );

  let pageContent = (
    <Table
      data={props.payg_instances}
      identifier={(row) => row.host}
      selectable={false}
      initialSortColumnKey="host"
      searchField={<SearchField filter={searchData} placeholder={t("Filter by any value")} />}
    >
      <Column
        columnKey="host"
        comparator={Utils.sortByText}
        header={t("Host")}
        cell={(row) => (
          <a className="js-spa" href={`/rhn/manager/admin/setup/payg/${row.id}`}>
            {row.host}
          </a>
        )}
      />
      <Column
        columnKey="description"
        comparator={Utils.sortByText}
        header={t("Description")}
        cell={(row) => _truncate(row.description, { length: 120 })}
      />
      <Column
        columnKey="statusMessage"
        comparator={Utils.sortByText}
        header={t("Status")}
        cell={(row) => (
          <PaygStatus status={row.status} statusMessage={_truncate(row.statusMessage || "-", { length: 120 })} />
        )}
      />
      <Column
        columnKey="lastBuildDate"
        comparator={Utils.sortByDate}
        header={t("Last Update Status")}
        cell={(row) => (row.lastChange ? <FromNow value={row.lastChange} /> : t("never"))}
      />
    </Table>
  );

  const prevStyle = { marginLeft: "10px", verticalAlign: "middle" };
  const activeStep = _SETUP_WIZARD_STEPS.find((step) => step.active);
  const currentStepIndex = activeStep ? _SETUP_WIZARD_STEPS.indexOf(activeStep) : -1;
  const footer = (
    <div className="panel-footer">
      <div className="btn-group">
        {currentStepIndex > 1 ? (
          <a className="btn btn-default" href={_SETUP_WIZARD_STEPS[currentStepIndex - 1].url}>
            <i className="fa fa-arrow-left"></i>
            {t("Prev")}
          </a>
        ) : null}
        {currentStepIndex < _SETUP_WIZARD_STEPS.length - 1 ? (
          <a className="btn btn-success" href={_SETUP_WIZARD_STEPS[currentStepIndex + 1].url}>
            <i className="fa fa-arrow-right"></i>
            {t("Next")}
          </a>
        ) : null}
      </div>
      <span style={prevStyle}>
        {currentStepIndex + 1}&nbsp;{t("of")}&nbsp;{_SETUP_WIZARD_STEPS.length}
      </span>
    </div>
  );
  const addPayg = () => {
    window.pageRenderers?.spaengine?.navigate?.(`/rhn/manager/admin/setup/payg/create`);
  };
  return (
    <div className="responsive-wizard">
      {title}
      {tabs}
      <div className="panel panel-default" id="products-content">
        <div className="panel-body">
          <SectionToolbar>
            <div className="action-button-wrapper">
              <div className="btn-group">
                <Button
                  id="addProducts"
                  icon="fa-plus"
                  className={"btn-success"}
                  text={t("Add Pay-as-you-go")}
                  handler={addPayg}
                />
              </div>
            </div>
          </SectionToolbar>
          {pageContent}
        </div>
      </div>
      {footer}
    </div>
  );
};

export default hot(withPageWrapper(ListPayg));
