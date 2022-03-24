import * as React from "react";

import { AsyncButton } from "components/buttons";
import { SubmitButton } from "components/buttons";
import { Form } from "components/input/Form";
import { Radio } from "components/input/Radio";
import { Text } from "components/input/Text";
import { Panel } from "components/panels/Panel";
import { TopPanel } from "components/panels/TopPanel";

import { DEPRECATED_unsafeEquals } from "utils/legacy";
import Network from "utils/network";

import { ContainerConfigMessages } from "./container-config-messages";

enum SSLMode {
  UseSSL = "use-ssl",
  CreateSSL = "create-ssl",
}

const initialModel = {
  caCertificate: "",
  caKey: "",
  caPassword: "",
  country: "",
  state: "",
  city: "",
  org: "",
  orgUnit: "",
  sslEmail: "",
  rootCA: "",
  intermediateCAs: [],
  proxyCertificate: "",
  proxyKey: "",
  proxyAdminEmail: "",
  sslMode: SSLMode.CreateSSL,
  maxSquidCacheSize: "",
  proxyFQDN: "",
  serverFQDN: "",
  proxyPort: "8022",
};

function replaceCRLF(content: string | undefined | null) {
  return content != null ? content.replace(/\r\n/g, "\n") : content;
}

export function ProxyConfig() {
  const [messages, setMessages] = React.useState<string[]>([]);
  const [loading, setLoading] = React.useState(false);
  const [success, setSuccess] = React.useState<boolean | undefined>();
  const [model, setModel] = React.useState(initialModel);
  const [invalid, setInvalid] = React.useState(false);

  const onSubmit = () => {
    setMessages([]);
    setLoading(true);

    const fileFields = {
      caCertificate: SSLMode.CreateSSL,
      caKey: SSLMode.CreateSSL,
      rootCA: SSLMode.UseSSL,
      intermediateCAs: SSLMode.UseSSL,
      proxyCertificate: SSLMode.UseSSL,
      proxyKey: SSLMode.UseSSL,
    };

    const fileReaders = Object.entries(fileFields)
      .filter(([fieldName, mode]) => mode === model.sslMode)
      .map(([fieldName, mode]) => {
        const field = document.getElementById(fieldName);
        if (field != null && field instanceof HTMLInputElement && field.files != null) {
          const file = field.files[0];
          return new Promise((resolve) => {
            const reader = new FileReader();
            reader.onload = (e) => {
              if (e.target?.result instanceof ArrayBuffer) {
                // Should never happen since we call readAsText, just quiets tsc
                resolve(undefined);
              } else {
                resolve({ [fieldName]: replaceCRLF(e.target?.result) });
              }
            };
            reader.readAsText(file);
          });
        }
        return undefined;
      })
      .filter((promise) => promise != null);
    Promise.all(fileReaders).then((values) => {
      const commonData = {
        proxyFQDN: model.proxyFQDN,
        proxyPort: model.proxyPort ? parseInt(model.proxyPort, 10) : 8022,
        serverFQDN: model.serverFQDN,
        maxSquidCacheSize: parseInt(model.maxSquidCacheSize, 10),
        proxyAdminEmail: model.proxyAdminEmail,
        sslMode: model.sslMode,
      };

      const extraData =
        model.sslMode === SSLMode.CreateSSL
          ? {
              caPassword: model.caPassword,
              country: model.country,
              state: model.state,
              city: model.city,
              org: model.org,
              orgUnit: model.orgUnit,
              sslEmail: model.sslEmail,
            }
          : {};
      const formData = Object.assign({}, commonData, extraData, ...values);
      Network.post("/rhn/manager/api/proxy/container-config", formData).then(
        (data) => {
          setSuccess(data.success);
          setMessages([]);
          setLoading(false);
        },
        (xhr) => {
          try {
            setSuccess(false);
            setMessages([JSON.parse(xhr.responseText)]);
            setLoading(false);
          } catch (err) {
            const errMessages = DEPRECATED_unsafeEquals(xhr.status, 0)
              ? t("Request interrupted or invalid response received from the server.")
              : Network.errorMessageByStatus(xhr.status)[0];
            setSuccess(false);
            setMessages([errMessages]);
            setLoading(false);
          }
        }
      );
    });
  };

  const clearFields = () => {
    setModel(initialModel);
  };

  const onValidate = (isValid: boolean) => {
    setInvalid(!isValid);
  };

  const onChange = (newModel: any) => {
    setModel(Object.assign({}, newModel));
  };

  return (
    <TopPanel
      title={t("Container Based Proxy Configuration")}
      icon="fa fa-cogs"
      helpUrl="reference/proxy/container-based-config.html"
    >
      <p>{t("TODO: some info text message about this page")}</p>
      {ContainerConfigMessages(success, messages, loading)}
      <Form
        className="form-horizontal"
        model={model}
        onValidate={onValidate}
        onChange={onChange}
        onSubmit={onSubmit}
        title={t("Container Based Proxy Configuration")}
      >
        <Text
          name="proxyFQDN"
          label={t("Proxy FQDN")}
          required
          placeholder={t("e.g., proxy.domain.com")}
          labelClass="col-md-3"
          divClass="col-md-6"
        />
        <Text
          name="serverFQDN"
          label={t("Server FQDN")}
          required
          placeholder={t("e.g., server.domain.com")}
          hint={t("FQDN of the server of proxy to connect to.")}
          labelClass="col-md-3"
          divClass="col-md-6"
        />
        <Text
          name="proxyPort"
          label={t("Proxy SSH port")}
          hint={t("Port range: 1 - 65535")}
          defaultValue="8022"
          labelClass="col-md-3"
          divClass="col-md-6"
        />
        <Text
          name="maxSquidCacheSize"
          label={t("Max Squid cache size (MB)")}
          required
          placeholder={t("e.g., 2048")}
          labelClass="col-md-3"
          divClass="col-md-6"
        />
        <Text
          name="proxyAdminEmail"
          label={t("Proxy administrator email")}
          placeholder={t("e.g., proxy.admin@mycompany.com")}
          required
          labelClass="col-md-3"
          divClass="col-md-6"
        />
        <Radio
          name="sslMode"
          label={t("SSL certificate")}
          title={t("SSL certificate")}
          inline={true}
          required
          labelClass="col-md-3"
          divClass="col-md-6"
          defaultValue={SSLMode.CreateSSL}
          items={[
            { label: t("Create"), value: SSLMode.CreateSSL },
            { label: t("Use existing"), value: SSLMode.UseSSL },
          ]}
        />
        {model.sslMode === SSLMode.CreateSSL && (
          <>
            <Text
              name="caCertificate"
              label={t("CA certificate to use to sign the SSL certificate in PEM format")}
              required
              type="file"
              labelClass="col-md-3"
              divClass="col-md-6"
            />
            <Text
              name="caKey"
              label={t("CA private key to use to sign the SSL certificate in PEM format")}
              required
              type="file"
              labelClass="col-md-3"
              divClass="col-md-6"
            />
            <Text
              name="caPassword"
              label={t("The CA private key password")}
              required
              type="password"
              labelClass="col-md-3"
              divClass="col-md-6"
            />
            <Panel key="ssl-certificate" title={t("SSL Certificate data")} headingLevel="h2">
              <Text
                name="country"
                label={t("2-letter country code")}
                maxLength={2}
                labelClass="col-md-3"
                divClass="col-md-1"
              />
              <Text name="state" label={t("State")} labelClass="col-md-3" divClass="col-md-6" />
              <Text name="city" label={t("City")} labelClass="col-md-3" divClass="col-md-6" />
              <Text name="org" label={t("Organization")} labelClass="col-md-3" divClass="col-md-6" />
              <Text name="orgUnit" label={t("Organization Unit")} labelClass="col-md-3" divClass="col-md-6" />
              <Text name="sslEmail" label={t("Email")} labelClass="col-md-3" divClass="col-md-6" />
            </Panel>
          </>
        )}
        {model.sslMode === SSLMode.UseSSL && (
          <>
            <Text
              name="rootCA"
              label={t("The root CA used to sign the SSL certificate in PEM format")}
              required
              type="file"
              labelClass="col-md-3"
              divClass="col-md-6"
            />
            <Text
              name="intermediateCAs"
              label={t("intermediate CA used to sign the SSL certificate in PEM format")}
              required
              type="file"
              labelClass="col-md-3"
              divClass="col-md-6"
            />
            <Text
              name="proxyCertificate"
              label={t("Proxy CRT content in PEM format")}
              required
              type="file"
              labelClass="col-md-3"
              divClass="col-md-6"
            />
            <Text
              name="proxyKey"
              label={t("Proxy SSL private key in PEM format")}
              required
              type="file"
              labelClass="col-md-3"
              divClass="col-md-6"
            />
          </>
        )}

        <div className="col-md-offset-3 col-md-6">
          <SubmitButton id="submit-btn" className="btn-success" text={t("Generate")} disabled={invalid} />
          <AsyncButton
            id="clear-btn"
            defaultType="btn-default pull-right"
            icon="fa-eraser"
            text={t("Clear fields")}
            action={clearFields}
          />
        </div>
      </Form>
    </TopPanel>
  );
}
