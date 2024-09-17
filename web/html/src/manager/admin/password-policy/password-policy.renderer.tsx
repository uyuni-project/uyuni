import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import PasswordPolicy from "./password-policy";

export const renderer = (id: string) => {
  SpaRenderer.renderNavigationReact(
    <PasswordPolicy />,
    document.getElementById(id)
  );
};
