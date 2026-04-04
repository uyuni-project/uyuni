import { useState } from "react";

import { AsyncButton, SubmitButton } from "components/buttons";
import { Form } from "components/input/form/Form";
import { FormMultiInput } from "components/input/form-multi-input/FormMultiInput";
import { unflattenModel } from "components/input/form-utils";
import { Radio } from "components/input/radio/Radio";
import { Text } from "components/input/text/Text";
import { Panel } from "components/panels/Panel";
import { TopPanel } from "components/panels/TopPanel";
import Validation from "components/validation";

import { DEPRECATED_unsafeEquals } from "utils/legacy";
import Network from "utils/network";

import { ContainerConfigMessages } from "./container-config-messages";

enum SSLMode {
  NoSSL = "no-ssl",
  UseSSL = "use-ssl",
  CreateSSL = "create-ssl",
}

export function ProxyConfig({ noSSL }: { noSSL: boolean }) {
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
    proxyCertificate: "",
    proxyKey: "",
    proxyAdminEmail: "",
    sslMode: noSSL ? SSLMode.NoSSL : SSLMode.CreateSSL,
    maxSquidCacheSize: "",
    proxyFQDN: "",
    serverFQDN: "",
    proxyPort: "8022",
  };
  const [messages, setMessages] = useState<React.ReactNode[]>([]);
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState<boolean | undefined>();
  const [model, setModel] = useState(initialModel);
  const [isValidated, setIsValidated] = useState(false);

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

    const fileReaders = Object.keys(model)
      .filter((key) => {
        const matcher = key.match(/^([a-zA-Z0-9]*[a-zA-Z])[0-9]+$/);
        const fieldName = matcher ? matcher[1] : key;
        return fileFields[fieldName] === model.sslMode;
      })
      .map((fieldName) => {
        const field = document.getElementById(fieldName);
        if (field !== null && field instanceof HTMLInputElement && !DEPRECATED_unsafeEquals(field.files, null)) {
          const file = field.files[0];
          return new Promise((resolve) => {
            const reader = new FileReader();
            reader.onload = (e) => {
              if (e.target?.result instanceof ArrayBuffer) {
                // Should never happen since we call readAsText, just quiets tsc
                resolve(undefined);
              } else {
                resolve({ [fieldName]: e.target?.result });
              }
            };
            reader.readAsText(file);
          });
        }
        return undefined;
      })
      .filter((promise) => promise !== undefined);
    Promise.all(fileReaders).then((values) => {
      const commonData = {
        proxyFQDN: model.proxyFQDN,
        proxyPort: model.proxyPort ? parseInt(model.proxyPort, 10) : 8022,
        serverFQDN: model.serverFQDN,
        maxSquidCacheSize: parseInt(model.maxSquidCacheSize, 10),
        proxyAdminEmail: model.proxyAdminEmail,
        sslMode: model.sslMode,
      };

      const cnamesData = Object.fromEntries(Object.entries(model).filter(([key]) => key.startsWith("cnames")));
      const extraData =
        model.sslMode === SSLMode.CreateSSL
          ? Object.assign(
              {},
              {
                caPassword: model.caPassword,
                country: model.country,
                state: model.state,
                city: model.city,
                org: model.org,
                orgUnit: model.orgUnit,
                sslEmail: model.sslEmail,
              },
              cnamesData
            )
          : {};
      const formData = unflattenModel(Object.assign({}, commonData, extraData, ...values));
      Network.post("/rhn/manager/api/proxy/container-config", formData).then(
        (data) => {
          setSuccess(data.success);
          setMessages([]);
          setLoading(false);
          window.location.assign("/rhn/manager/api/proxy/container-config/" + data);
        },
        (xhr) => {
          try {
            setSuccess(false);
            setMessages([
              <>
                {xhr.responseJSON.messages.map((line: string) => (
                  <p key={line}>{line}</p>
                ))}
              </>,
            ]);
            setLoading(false);
          } catch (err) {
            const errMessages =
              xhr.status === 0
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

  const onValidate = (isValidated: boolean) => {
    setIsValidated(isValidated);
  };

  const onChange = (newModel) => {
    setModel(Object.assign({}, newModel));
  };

  const onAddField = (fieldName: string) => {
    return (index: number) => setModel(Object.assign({}, model, { [fieldName + index]: "" }));
  };

  const onRemoveField = (fieldName: string) => {
    return (index: number) => {
      const newModel = { ...model };
      delete newModel[`${fieldName}${index}`];
      setModel(newModel);
    };
  };

  return (
    <TopPanel
      title={t("Container Based Proxy Configuration")}
      icon="fa fa-cogs"
      helpUrl="installation-and-upgrade/proxy-container-setup.html"
    >
      <p>
        {t(
          "You can generate a set of configuration files and certificates in order to register and run a container-based proxy. Once the following form is filled out and submitted you will get a .tar.gz archive to download."
        )}
      </p>
      {ContainerConfigMessages(success, messages, loading)}
      <Form
        className="form-horizontal mt-5"
        model={model}
        onValidate={onValidate}
        onChange={onChange}
        onSubmit={onSubmit}
        title={t("Container Based Proxy Configuration")}
      >
        <Text
          name="proxyFQDN"
          label={t("Proxy FQDN")}
          hint={t("The unique, DNS-resolvable FQDN of this proxy.")}
          required
          placeholder={t("e.g., proxy.domain.com")}
          labelClass="col-md-3"
          divClass="col-md-6"
          validators={[Validation.matches(/^[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*$/)]}
          invalidHint={t("Has to be a valid FQDN address")}
        />
        <Text
          name="serverFQDN"
          label={t("Parent FQDN")}
          required
          placeholder={t("e.g., server.domain.com")}
          hint={t("The FQDN of the parent (server or proxy) to connect to.")}
          labelClass="col-md-3"
          divClass="col-md-6"
          validators={[Validation.matches(/^[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*$/)]}
          invalidHint={t("Has to be a valid FQDN address")}
        />
        <Text
          name="proxyPort"
          label={t("Proxy SSH port")}
          hint={t("Port range: 1 - 65535")}
          validators={[Validation.isInt({ gt: 0, lt: 65536 })]}
          defaultValue="8022"
          labelClass="col-md-3"
          divClass="col-md-6"
          type="number"
        />
        <Text
          name="maxSquidCacheSize"
          label={t("Max Squid cache size")}
          hint={t("The maximum value of the Squid cache in Megabytes")}
          required
          validators={[Validation.isInt({ gt: 0 })]}
          placeholder={t("e.g., 2048")}
          labelClass="col-md-3"
          divClass="col-md-6"
          type="number"
        />
        <Text
          name="proxyAdminEmail"
          label={t("Proxy admin email")}
          hint={t("The container-based proxy administrator email address")}
          placeholder={t("e.g., proxy.admin@mycompany.com")}
          required
          labelClass="col-md-3"
          divClass="col-md-6"
        />
        <Radio
          name="sslMode"
          label={t("SSL certificate")}
          title={t("SSL certificate")}
          hint={"Whether to generate an SSL certificate or reuse an existing one"}
          inline={true}
          required
          labelClass="col-md-3"
          divClass="col-md-6"
          defaultValue={noSSL ? SSLMode.NoSSL : SSLMode.CreateSSL}
          items={[
            { label: t("Skip SSL configuration"), value: SSLMode.NoSSL },
            ...(!noSSL ? [{ label: t("Generate"), value: SSLMode.CreateSSL }] : []),
            { label: t("Use existing"), value: SSLMode.UseSSL },
          ]}
        />
        {model.sslMode === SSLMode.CreateSSL && (
          <>
            <Text
              name="caCertificate"
              label={t("CA certificate")}
              hint={t("To sign the SSL certificate in PEM format")}
              required
              type="file"
              labelClass="col-md-3"
              divClass="col-md-6"
            />
            <Text
              name="caKey"
              label={t("CA private key")}
              hint={t("To sign the SSL certificate in PEM format")}
              required
              type="file"
              labelClass="col-md-3"
              divClass="col-md-6"
            />
            <Text
              name="caPassword"
              label={t("CA password")}
              hint={t("The CA private key password")}
              required
              type="password"
              labelClass="col-md-3"
              divClass="col-md-6"
            />
            <Panel
              key="ssl-certificate"
              title={t("SSL Certificate data")}
              headingLevel="h2"
              className="panel-default col-md-6 col-md-offset-3 offset-md-3 no-padding"
            >
              <div className="row">
                <FormMultiInput
                  id="cnames"
                  title={t("Alternate CNAMEs")}
                  prefix="cnames"
                  onAdd={onAddField("cnames")}
                  onRemove={onRemoveField("cnames")}
                  panelHeading="label"
                  panelClassName="panel-default col-md-8 col-md-offset-1 offset-md-1 no-padding"
                >
                  {(index) => (
                    <Text name={`cnames${index}`} label={t("CNAME")} labelClass="col-md-3" divClass="col-md-6" />
                  )}
                </FormMultiInput>
              </div>
              <Text
                name="country"
                label={t("2-letter country code")}
                validators={[Validation.matches(/^[A-Z]{2}$/)]}
                maxLength={2}
                labelClass="col-md-3"
                divClass="col-md-2"
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
              label={t("The root CA")}
              hint={t("To sign the SSL certificate in PEM format")}
              required
              type="file"
              labelClass="col-md-3"
              divClass="col-md-6"
            />
            <div className="row">
              <FormMultiInput
                id="intermediateCAs"
                title={t("Intermediate CAs")}
                prefix="intermediateCAs"
                onAdd={onAddField("intermediateCAs")}
                onRemove={onRemoveField("intermediateCAs")}
                panelClassName="panel-default col-md-8 col-md-offset-1 offset-md-1 no-padding"
                panelHeading="label"
              >
                {(index) => (
                  <Text
                    name={`intermediateCAs${index}`}
                    label={t("CA file in PEM format")}
                    required
                    type="file"
                    labelClass="col-md-3"
                    divClass="col-md-6"
                  />
                )}
              </FormMultiInput>
            </div>
            <Text
              name="proxyCertificate"
              label={t("Proxy certificate")}
              hint={t("In PEM format")}
              required
              type="file"
              labelClass="col-md-3"
              divClass="col-md-6"
            />
            <Text
              name="proxyKey"
              label={t("Proxy SSL private key")}
              hint={t("In PEM format")}
              required
              type="file"
              labelClass="col-md-3"
              divClass="col-md-6"
            />
          </>
        )}

        <div className="col-md-offset-3 offset-md-3 col-md-6">
          <SubmitButton id="submit-btn" className="btn-primary me-3" text={t("Generate")} disabled={!isValidated} />
          <AsyncButton id="clear-btn" defaultType="btn-default" text={t("Clear fields")} action={clearFields} />
        </div>
      </Form>
    </TopPanel>
  );
}
