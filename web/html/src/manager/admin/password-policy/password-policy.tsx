import { hot } from "react-hot-loader/root";

import withPageWrapper from "components/general/with-page-wrapper";

import { Panel } from "components/panels/Panel";

type PasswordPolicyProps = {
  minLength: bigint,
  maxLength: bigint,
  digitsFlag: boolean,
  lowerCharFlag: boolean,
  upperCharFlag: boolean,
  consecutiveCharFlag: boolean,
  specialCharFlag: boolean,
  specialCharList: string | null,
  restrictedOccurrenceFlag: boolean,
  maxCharOccurrence: bigint
};

const PasswordPolicy = (props : PasswordPolicyProps) => {

  const pageContent = (
    <Panel
      key="policy"
      title={t("Password Policy")}
      headingLevel="h4"
      footer={
        <div className="row">
          <div className="col-md-offset-3 offset-md-3 col-md-9"></div>
        </div>
      }
    >
    <div className="row">
      <div className="col-md-9">
        <div className="row">
          <div className="col-md-4 text-left">
            <label>{t("Password Policy")}</label>
          </div>
          <div className="col-md-8">
          </div>
        </div>
      </div>
    </div>
  </Panel>

  )
  return (
    <div className="responsive-wizard">
      <div className="spacewalk-toolbar-h1">
        <div className="spacewalk-toolbar"></div>
        <h1>
          <i className="fa fa-info-circle"></i>
          {t("SUSE Manager Configuration - Password Policy")}
        </h1>
      </div>
      <div className="page-summary">
        <p>{t("Setup your SUSE Manager server local users password policy.")}</p>
      </div>
      <div className="spacewalk-content-nav">
        <ul className="nav nav-tabs">
          <li>
            <a className="js-spa" href="/rhn/admin/config/GeneralConfig.do?">
              {t("General")}
            </a>
          </li>
          <li>
            <a className="js-spa" href="/rhn/admin/config/BootstrapConfig.do?">
              {t("Bootstrap Script")}
            </a>
          </li>
          <li>
            <a className="js-spa" href="/rhn/admin/config/Orgs.do?">
              {t("Organizations")}
            </a>
          </li>
          <li>
            <a className="js-spa" href="/rhn/admin/config/Restart.do?">
              {t("Restart")}
            </a>
          </li>
          <li>
            <a className="js-spa" href="/rhn/admin/config/Cobbler.do?">
              {t("Cobbler")}
            </a>
          </li>
          <li className="js-spa">
            <a href="/rhn/manager/admin/config/monitoring?">{t("Monitoring")}</a>
          </li>
          <li className="active js-spa">
            <a href="/rhn/manager/admin/config/password-policy?">{t("Password Policy")}</a>
          </li>
        </ul>
      </div>
      {pageContent}
    </div>
  );
}

export default hot(withPageWrapper(PasswordPolicy));
