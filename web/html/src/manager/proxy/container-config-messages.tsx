import type { ReactNode } from "react";

import { Messages } from "components/messages/messages";
import { MessagesContainer, showSuccessToastr, showErrorToastr, showInfoToastr } from "components/toastr/toastr";
type SuccessType = boolean | undefined;

export const ContainerConfigMessages = (success: SuccessType, messagesIn: ReactNode[], loading: boolean) => {
  if (success) {
    showSuccessToastr(t("The container based proxy configuration has been generated correctly."));
  } else if (messagesIn.length > 0) {
     showErrorToastr(
      <>
        {messagesIn.map((msg, i) => (
          <div key={i}>{msg}</div>
        ))}
      </>,
      { autoHide: false }
    );
  } else if (loading) {
    showInfoToastr(t("Generation of the proxy configuration in progress: waiting for a response..."), { autoHide: false });
  }
  return <MessagesContainer />;
};
