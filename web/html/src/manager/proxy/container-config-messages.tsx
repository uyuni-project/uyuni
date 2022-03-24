import * as React from "react";

import { Messages } from "components/messages";

type SuccessType = boolean | undefined;
type MessagesType = React.ReactNode[];

export const ContainerConfigMessages = (success: SuccessType, messagesIn: MessagesType, loading: boolean) => {
  var messages: React.ReactNode = null;
  if (success) {
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
  } else if (messagesIn.length > 0) {
    messages = (
      <Messages
        items={messagesIn.map(function (msg) {
          return Messages.error(msg);
        })}
      />
    );
  } else if (loading) {
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
