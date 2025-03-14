import * as React from "react";

import { Messages, MessageType } from "components/messages/messages";

type SuccessType = boolean | undefined;

export const ContainerConfigMessages = (success: SuccessType, messagesIn: React.ReactNode[], loading: boolean) => {
  let items: MessageType[] = [];
  if (success) {
    items = [
      {
        severity: "success",
        text: <p>{t("Proxy configuration successfully applied.")}</p>,
      },
    ];
  } else if (messagesIn.length > 0) {
    items = messagesIn.map((msg) => ({
      severity: "error",
      text: <p>{msg}</p>,
    }));
  } else if (loading) {
    items = [
      {
        severity: "info",
        text: <p>{t("Applying proxy configuration: waiting for a response...")}</p>,
      },
    ];
  }
  return <Messages items={items} />;
};
