import * as React from "react";

import { Messages, MessageType } from "components/messages/messages";

export const getGlobalMessages = (validationErrors, schemaUpgradeRequired, diskspaceSeverity, sccForwardWarning) => {
  let messages: MessageType[] = [];

  if (validationErrors && validationErrors.length > 0) {
    messages = messages.concat(validationErrors.map((msg) => ({ severity: "error", text: msg })));
  }

  if (schemaUpgradeRequired) {
    const schemaUpgradeError = t(
      "A schema upgrade is required. Please upgrade your schema at your earliest convenience to receive latest bug fixes and avoid potential problems."
    );
    messages = messages.concat({ severity: "error", text: schemaUpgradeError });
  }

  if (diskspaceSeverity !== "ok") {
    const severity_messages = {
      undefined: Messages.info(
        t(
          "Unable to validate the disk space availability. Please contact your system admistrator if this problem persists."
        )
      ),
      misconfiguration: Messages.warning(
        t(
          "Some important directories are missing. Please contact your system administrator to review the configuration."
        )
      ),
      alert: Messages.warning(
        t(
          "The available disk space on the server is running low. Please contact your system administrator to add more disk space."
        )
      ),
      critical: Messages.error(
        t(
          "The available disk space on the server is critically low. Please contact your system administrator to add more disk space."
        )
      ),
    };

    if (diskspaceSeverity in severity_messages) {
      messages = messages.concat(severity_messages[diskspaceSeverity]);
    } else {
      Loggerhead.warn("Unknown disk space severity level: " + diskspaceSeverity);
      messages = messages.concat(severity_messages["undefined"]);
    }
  }

  if (sccForwardWarning) {
    const sccForwardWarningMessage = t(
      "SUSE Manager PAYG instances must forward registration data to SCC when credentials are provided. Data will be sent independently of the configuration setting. To hide this warning please set 'forward_registration = 1' in rhn.conf."
    );
    messages = messages.concat({ severity: "warning", text: sccForwardWarningMessage });
  }

  return messages;
};

export const getFormMessages = (success, messages) => {
  if (success) {
    return [{ severity: "success", text: <p>{t("Signing in ...")}.</p> }];
  }

  if (messages.length > 0) {
    return messages.map((msg) => ({ severity: "error", text: msg }));
  }

  return [];
};
