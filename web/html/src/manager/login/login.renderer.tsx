import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import Login from "./login";

export const renderer = (
  id: string,
  {
    isUyuni,
    theme,
    urlBounce,
    validationErrors,
    schemaUpgradeRequired,
    webVersion,
    productName,
    customHeader,
    customFooter,
    legalNote,
    loginLength,
    passwordLength,
    diskspaceSeverity,
    sccForwardWarning,
  }
) => {
  SpaRenderer.renderNavigationReact(
    <Login
      isUyuni={isUyuni}
      theme={theme}
      bounce={urlBounce}
      validationErrors={validationErrors}
      schemaUpgradeRequired={schemaUpgradeRequired}
      webVersion={webVersion}
      productName={productName}
      customHeader={customHeader}
      customFooter={customFooter}
      legalNote={legalNote}
      loginLength={loginLength}
      passwordLength={passwordLength}
      diskspaceSeverity={diskspaceSeverity}
      sccForwardWarning={sccForwardWarning}
    />,
    document.getElementById(id),
    true
  );
};
