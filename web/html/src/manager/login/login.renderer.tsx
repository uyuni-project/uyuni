import ReactDOM from "react-dom";

import Login from "./login";

export const renderer = (
  id: string,
  {
    theme,
    urlBounce,
    validationErrors,
    schemaUpgradeRequired,
    webVersion,
    customHeader,
    customFooter,
    legalNote,
    loginLength,
    passwordLength,
    diskspaceSeverity,
    sccForwardWarning,
  }
) => {
  const elementToRender = document.getElementById(id);
  if (elementToRender) {
    ReactDOM.render(
      <Login
        theme={theme}
        bounce={urlBounce}
        validationErrors={validationErrors}
        schemaUpgradeRequired={schemaUpgradeRequired}
        webVersion={webVersion}
        customHeader={customHeader}
        customFooter={customFooter}
        legalNote={legalNote}
        loginLength={loginLength}
        passwordLength={passwordLength}
        diskspaceSeverity={diskspaceSeverity}
        sccForwardWarning={sccForwardWarning}
      />,
      elementToRender
    );
  }
};
