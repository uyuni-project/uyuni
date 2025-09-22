import { useState } from "react";

import { useAsyncEffect } from "@etheryte/react-hooks";

import Network from "utils/network";

import { ProxySettings } from "./proxy-settings";

enum Verification {
  Unknown,
  Verifying,
  Valid,
  Invalid,
}

type Props = {
  savedSettings?: ProxySettings;
};

export const ProxyVerification = (props: Props) => {
  const [verification, setVerification] = useState(Verification.Unknown);
  const [isUpdate, setIsUpdate] = useState(false);

  useAsyncEffect(
    async (isStale) => {
      // On first render, whatever we have is the value the server gave us, so we don't need to bust cache
      const forceRefresh = isUpdate;
      setIsUpdate(true);

      if (!props.savedSettings) {
        setVerification(Verification.Unknown);
        return;
      }

      try {
        setVerification(Verification.Verifying);
        const result = await Network.post("/rhn/ajax/verify-proxy-settings", {
          forceRefresh,
        });
        if (isStale()) {
          return;
        }

        const valid = JSON.parse(result);
        if (valid) {
          setVerification(Verification.Valid);
        } else {
          setVerification(Verification.Invalid);
        }
      } catch (error) {
        setVerification(Verification.Invalid);
        Loggerhead.error(error);
      }
    },
    [props.savedSettings]
  );

  if (verification === Verification.Verifying) {
    return <div className="alert alert-info">{t("Verifying proxy settings")}</div>;
  }
  if (verification === Verification.Valid) {
    return <div className="alert alert-success">{t("Successfully verified proxy settings")}</div>;
  }
  if (verification === Verification.Invalid) {
    return <div className="alert alert-danger">{t("Proxy settings are not valid")}</div>;
  }
  return null;
};
