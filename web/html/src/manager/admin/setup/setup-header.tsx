import { HelpLink } from "components/utils";

export const SetupHeader = () => {
  const SETUP_STEPS = [
    {
      id: "wizard-step-proxy",
      label: t("HTTP Proxy"),
      url: "/rhn/manager/admin/setup/proxy",
    },
    {
      id: "wizard-step-credentials",
      label: t("Organization Credentials"),
      url: "/rhn/admin/setup/MirrorCredentials.do",
    },
    {
      id: "wizard-step-suse-products",
      label: t("Products"),
      url: "/rhn/manager/admin/setup/products",
    },
    {
      id: "wizard-step-suse-payg",
      label: t("PAYG Connections"),
      url: "/rhn/manager/admin/setup/payg",
    },
  ];

  return (
    <>
      <div className="spacewalk-toolbar-h1">
        <h1>
          <i className="fa fa-cogs"></i> {t("Setup Wizard")} <HelpLink url="reference/admin/setup-wizard.html" />
        </h1>
      </div>
      <div className="spacewalk-content-nav">
        <ul className="nav nav-tabs">
          {SETUP_STEPS.map((step) => (
            <li key={step.id} className={window.location.pathname === step.url ? "active" : ""}>
              <a className="js-spa" href={step.url}>
                {t(step.label)}
              </a>
            </li>
          ))}
        </ul>
      </div>
    </>
  );
};
