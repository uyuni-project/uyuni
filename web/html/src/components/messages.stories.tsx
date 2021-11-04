import * as React from "react";
import { Messages, Utils } from "./messages";

export default {
  component: Messages,
  title: "Alert Messages",
};

export const allTypes = () => (
  <Messages
    items={[
      { severity: "error", text: "This is an example error message." },
      { severity: "warning", text: "This is an example warning message." },
      { severity: "success", text: "This is an example success message." },
      { severity: "info", text: "This is an example info message." },
    ]}
  />
);

export const usingHelperMethods = () => (
  <Messages items={Utils.success("My success message created using the `Utils.success()` method.")} />
);
