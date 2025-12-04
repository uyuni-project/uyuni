import type { ReactNode } from "react";

import { MessagesContainer, showErrorToastr, showInfoToastr, showSuccessToastr } from "components/toastr/toastr";
type SuccessType = boolean | undefined;

export const ContainerConfigMessages = (success: SuccessType, messagesIn: ReactNode[], loading: boolean) => {
  let items: MessageType[] = [];
  if (success) {
    showSuccessToastr(t("Proxy configuration action has been scheduled."));
  } else if (messagesIn.length > 0) {
    showErrorToastr(
      <>
        {messagesIn.map((msg, i) => (
          <div key={msg}>{msg}</div>
        ))}
      </>,
      { autoHide: false }
    );
  } else if (loading) {
    showInfoToastr(t("Scheduling proxy configuration action..."), { autoHide: false });
  }
  return <MessagesContainer />;
};
