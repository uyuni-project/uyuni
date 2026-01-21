import type { ReactNode } from "react";

import { MessagesContainer, showErrorToastr, showInfoToastr, showSuccessToastr } from "components/toastr/toastr";
type SuccessType = boolean | undefined;

export const ContainerConfigMessages = (success: SuccessType, messagesIn: ReactNode[], loading: boolean) => {
  const hasErrors = messagesIn.length > 0;

  const renderErrors = () => (
    <>
      {messagesIn.map((msg, index) => (
        <div key={`error-${index}`}>{msg}</div>
      ))}
    </>
  );

  if (success) {
    showSuccessToastr(t("Proxy configuration action has been scheduled."));
  } else if (hasErrors) {
    showErrorToastr(renderErrors(), { autoHide: false });
  } else if (loading) {
    showInfoToastr(t("Scheduling proxy configuration action..."), {
      autoHide: false,
    });
  }

  return <MessagesContainer />;
};
