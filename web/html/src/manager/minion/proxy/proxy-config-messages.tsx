import type { ReactNode } from "react";

import { Messages, MessageType } from "components/messages/messages";

type SuccessType = boolean | undefined;

export const ContainerConfigMessages = (success: SuccessType, messagesIn: ReactNode[], loading: boolean) => {
  let items: MessageType[] = [];
  if (success) {
    items = [
      {
        severity: "success",
        text: <p>{t("Proxy configuration action has been scheduled.")}</p>,
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
        text: <p>{t("Scheduling proxy configuration action...")}</p>,
      },
    ];
  }
  return <Messages items={items} autoScroll={autoScroll} />;
};
