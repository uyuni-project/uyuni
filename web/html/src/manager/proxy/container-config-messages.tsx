import * as React from "react";

import { Messages } from "components/messages";

export const ContainerConfigMessages = (stateIn) => {
  var messages: React.ReactNode = null;
  if (stateIn.success) {
    messages = (
      <Messages
        items={[
          {
            severity: "success",
            text: (
              <p>
                {t(
                  "The container based proxy configuration has been generated! The 'proxy-config.zip archive can be found at /bla/bla/bla on the server, or it can be"
                )}{" "}
                <a className="js-spa" href="/rhn/systems/SystemList.do">
                  {t("downloaded")}
                </a>
                {"."}
              </p>
            ),
          },
        ]}
      />
    );
  } else if (stateIn.messages.length > 0) {
    messages = (
      <Messages
        items={stateIn.messages.map(function (msg) {
          return { severity: "error", text: msg };
        })}
      />
    );
  } else if (stateIn.loading) {
    messages = (
      <Messages
        items={[
          {
            severity: "info",
            text: <p>{t("Generation of the proxy configuration in progress: waiting for a response...")}</p>,
          },
        ]}
      />
    );
  }
  return messages;
};
