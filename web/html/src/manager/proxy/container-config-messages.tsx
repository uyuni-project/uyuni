import * as React from "react";

import { Messages } from "components/messages";

type SuccessType = boolean | undefined;

export const ContainerConfigMessages = (success: SuccessType, messagesIn: string[], loading: boolean) => {
  if (success) {
    return (
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
  } else if (messagesIn.length > 0) {
    return (
      <Messages
        items={messagesIn.map(function (msg) {
          return Messages.error(msg);
        })}
      />
    );
  } else if (loading) {
    return (
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
  return null;
};
