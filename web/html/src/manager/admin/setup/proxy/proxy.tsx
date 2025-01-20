import { useEffect, useState } from "react";

import { Form, Password, Text } from "components/input";
import { Panel } from "components/panels/Panel";

import Network from "utils/network";

import { SetupHeader } from "../setup-header";
import img from "./http-proxy.png";
import { ProxySettings } from "./proxy-settings";

enum Verification {
  Unknown,
  Checking,
  Valid,
  Invalid,
}

type Props = {
  proxySettings: Readonly<ProxySettings>;
};

export default (props: Props) => {
  const hasSavedSettings = Boolean(
    props.proxySettings.hostname || props.proxySettings.username || props.proxySettings.password
  );
  const [isEditing, setIsEditing] = useState(!hasSavedSettings);

  const [settings, setSettings] = useState<ProxySettings>({
    ...props.proxySettings,
  });

  const [verification, setVerification] = useState(hasSavedSettings ? Verification.Checking : Verification.Unknown);

  const verifySettings = async (forceRefresh: boolean) => {
    try {
      const result = await Network.post("/rhn/ajax/verify-proxy-settings", {
        forceRefresh,
      });
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
  };

  const saveSettings = async (newSettings: typeof settings) => {
    if (verification !== Verification.Unknown) {
      setVerification(Verification.Checking);
    }

    try {
      const savedProxySettings = await Network.post("/rhn/ajax/save-proxy-settings", newSettings);
      setSettings((prevSettings) => ({ ...prevSettings, ...savedProxySettings }));
      verifySettings(true);
    } catch (error) {
      setVerification(Verification.Invalid);
      Loggerhead.error(error);
    }
  };

  useEffect(() => {
    if (hasSavedSettings) {
      verifySettings(false);
    }
  }, []);

  const footer = (
    <div className="d-flex flex-row justify-content-between">
      <div>
        <button
          type="button"
          className="btn btn-default"
          onClick={() => {
            if (isEditing) {
              setSettings({ ...props.proxySettings });
            }
            setIsEditing(!isEditing);
          }}
        >
          {isEditing ? t("Cancel") : t("Edit")}
        </button>
      </div>
      <div>
        {isEditing ? (
          <button
            id="http-proxy-save"
            type="submit"
            className="btn btn-primary"
            disabled={verification === Verification.Checking}
          >
            {t("Save and Verify")}
          </button>
        ) : null}
      </div>
    </div>
  );

  const passwordPlaceholder = hasSavedSettings && !isEditing ? "●".repeat(8) : t("Password");

  return (
    <div className="responsive-wizard">
      <SetupHeader />
      <div className="row">
        <div className="col-sm-9">
          {verification === Verification.Checking ? (
            <div className="alert alert-info">{t("Verifying proxy settings")}</div>
          ) : null}
          {verification === Verification.Valid ? (
            <div className="alert alert-success">{t("Successfully verified proxy settings")}</div>
          ) : null}
          {verification === Verification.Invalid ? (
            <div className="alert alert-danger">{t("Proxy settings are not valid")}</div>
          ) : null}

          <Form model={settings} onChange={setSettings} onSubmit={saveSettings} autoComplete="off">
            <Panel footer={footer}>
              <Text
                name="hostname"
                label={t("HTTP Proxy Hostname")}
                placeholder={t("hostname:port")}
                hint={t("For example: <code>example.com:8080</code> or <code>192.0.2.0:8080</code>", {
                  code: (text: string) => <code key={text}>{text}</code>,
                })}
                validators={(value) => {
                  if (!value) {
                    return true;
                  }
                  const parts = value.split(":");
                  return parts.length === 2 && parts[0].length > 0 && parts[1].length > 0;
                }}
                invalidHint="Please provide both a hostname or an IP, and a port"
                labelClass="col-md-4"
                divClass="col-md-8"
                disabled={!isEditing}
              />
              <Text
                name="username"
                label={t("HTTP Proxy Username")}
                placeholder={t("Username")}
                labelClass="col-md-4"
                divClass="col-md-8"
                autoComplete="off"
                disabled={!isEditing}
              />
              <Password
                name="password"
                label={t("HTTP Proxy Password")}
                placeholder={passwordPlaceholder}
                labelClass="col-md-4"
                divClass="col-md-8"
                autoComplete="off"
                disabled={!isEditing}
              />
            </Panel>
          </Form>
        </div>
        <div className="col-sm-3 hidden-xs" id="wizard-faq">
          <img src={img} alt={t("Illustration of a proxy server")} />
          <h4>{t("HTTP Proxy")}</h4>
          <p>
            {t(
              "If this server uses an HTTP proxy to access the outside network, you can use this form to configure it. If that is not the case, move on to <link>Organization Credentials</link>.",
              {
                link: (text: string) => (
                  <a href="https://server.local/rhn/admin/setup/MirrorCredentials.do" key={text}>
                    {text}
                  </a>
                ),
              }
            )}
          </p>
        </div>
      </div>
    </div>
  );
};
