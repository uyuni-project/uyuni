import * as React from "react";
import * as ReactDOM from "react-dom";
import { AccountEmailForm } from "../components/account-email-form";

/**
 * React entry point for Account Email Form
 * Rendered from Jade template with data
 */
const props = (window as any).emailFormProps || {
  currentEmail: "",
  targetUserId: undefined,
};

ReactDOM.render(
  <AccountEmailForm {...props} />,
  document.getElementById("account-email-form")
);

