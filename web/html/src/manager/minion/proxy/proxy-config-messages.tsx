import * as React from "react";

import { Messages } from "components/messages/messages";

type SuccessType = boolean | undefined;

export const ContainerConfigMessages = (success: SuccessType, messagesIn: React.ReactNode[], loading: boolean) => {
  if (success) {
    return (
      <Messages
        items={[
          {
            severity: "success",
            text: <p>{t("Proxy configuration successfully applied.")}</p>,
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
            text: <p>{t("Applying proxy configuration: waiting for a response...")}</p>,
          },
        ]}
      />
    );
  }
  return null;
};
