import { hot } from "react-hot-loader/root";

import * as React from "react";

import withPageWrapper from "components/general/with-page-wrapper";

import SusemanagerThemeLogin from "./susemanager/login";
import UyuniThemeLogin from "./uyuni/login";

type Theme = "uyuni" | "susemanager-light" | "susemanager-dark";

const products = {
  suma: {
    key: "SUSE Manager",
    headerTitle: (
      <React.Fragment>
        <span>SUSE</span>
        <i className="fa fa-registered" />
        <span>Manager</span>
      </React.Fragment>
    ),
    bodyTitle: (
      <span>
        SUSE
        <br />
        {" Manager"}
      </span>
    ),
    url: "http://www.suse.com/products/suse-manager/",
    title: "SUSE Manager login page",
  },
  uyuni: {
    key: "Uyuni",
    headerTitle: "Uyuni",
    bodyTitle: "Uyuni",
    url: "http://www.uyuni-project.org/",
    title: "Uyuni login page",
  },
};

type Props = {
  isUyuni: boolean;
  theme: Theme;
  bounce: string;
  validationErrors: Array<string>;
  schemaUpgradeRequired: boolean;
  webVersion: string;
  productName: string;
  customHeader: string;
  customFooter: string;
  legalNote: string;
  loginLength: string;
  passwordLength: string;
  diskspaceSeverity: string;
};

export type ThemeProps = Props & {
  product: typeof products[keyof typeof products];
};

const Login = (props: Props) => {
  const product = props.isUyuni ? products.uyuni : products.suma;
  if (props.theme === "uyuni") {
    return <UyuniThemeLogin {...props} product={product} />;
  }
  return <SusemanagerThemeLogin {...props} product={product} />;
};

export default hot(withPageWrapper<Props>(Login));
