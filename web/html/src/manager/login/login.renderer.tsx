import * as React from "react";

import ReactDOM from "react-dom";

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
  }
) => {
  const elementToRender = document.getElementById(id);
  if (elementToRender) {
    ReactDOM.render(
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
      />,
      elementToRender
    );
  }
};
