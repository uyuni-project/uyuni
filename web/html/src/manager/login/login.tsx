import * as React from "react";

import { isUyuni } from "core/user-preferences";

import withPageWrapper from "components/general/with-page-wrapper";

import SusemanagerThemeLogin from "./susemanager/login";
import UyuniThemeLogin from "./uyuni/login";

type Theme = "uyuni" | "suse-light" | "suse-dark";

const products = {
  suma: {
    productName: "SUSE Multi-Linux Manager",
    headerTitle: (
      <React.Fragment>
        <span>SUSE</span>
        <i className="fa fa-registered" /> <span>Multi-Linux Manager</span>
      </React.Fragment>
    ),
    bodyTitle: (
      <span>
        SUSE
        <br />
        {" Multi-Linux Manager"}
      </span>
    ),
    url: "https://www.suse.com/products/multi-linux-manager/",
    title: "SUSE Multi-Linux Manager login page",
  },
  uyuni: {
    productName: "Uyuni",
    headerTitle: "Uyuni",
    bodyTitle: "Uyuni",
    url: "https://www.uyuni-project.org/",
    title: "Uyuni login page",
  },
};

type Props = {
  theme: Theme;
  bounce: string;
  validationErrors: string[];
  schemaUpgradeRequired: boolean;
  webVersion: string;
  customHeader: string;
  customFooter: string;
  legalNote: string;
  loginLength: string;
  passwordLength: string;
  diskspaceSeverity: string;
  sccForwardWarning: boolean;
};

export type ThemeProps = Props & {
  product: (typeof products)[keyof typeof products];
};

const Login = (props: Props) => {
  const product = isUyuni ? products.uyuni : products.suma;
  if (props.theme === "uyuni") {
    return <UyuniThemeLogin {...props} product={product} />;
  }
  return <SusemanagerThemeLogin {...props} product={product} />;
};

export default withPageWrapper<Props>(Login);
