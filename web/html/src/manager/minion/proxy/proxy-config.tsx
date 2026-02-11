import { useEffect, useState } from "react";

import { AsyncButton, SubmitButton } from "components/buttons";
import { DEPRECATED_Select } from "components/input";
import { Form } from "components/input/form/Form";
import { FormMultiInput } from "components/input/form-multi-input/FormMultiInput";
import { unflattenModel } from "components/input/form-utils";
import { Radio } from "components/input/radio/Radio";
import { Text } from "components/input/text/Text";
import { Messages } from "components/messages/messages";
import { TopPanel } from "components/panels/TopPanel";
import Validation from "components/validation";

import Network from "utils/network";

import { ContainerConfigMessages } from "./proxy-config-messages";
import { ProxyConfigProps, RegistryMode, SourceMode, UseCertsMode } from "./proxy-config-types";
import { getRegistryData, modelDefaults, readFileFields, restoreRegistryInputs } from "./proxy-config-utils";

export function ProxyConfig({
  serverId,
  parents,
  currentConfig,
  validationErrors,
  registryUrlExample,
  registryTagExample,
  hasCertificates,
}: Readonly<ProxyConfigProps>) {
  const [messages, setMessages] = useState<React.ReactNode[]>([]);
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState<boolean | undefined>();
  const [isValidated, setIsValidated] = useState(false);

  const originalConfig = { ...currentConfig };

  const [model, setModel] = useState(() => {
    const initialModel = {
      ...modelDefaults,
      ...currentConfig,
    };

    return initialModel;
  });

  useEffect(() => {
    setModel((prev) => ({ ...prev }));

    if (validationErrors && validationErrors.length > 0) {
      setSuccess(false);
      setMessages(validationErrors);
    }
  }, [currentConfig]);

  const onSubmit = () => {
    setMessages([]);
    setLoading(true);

    const fileReaders = readFileFields(model);

    Promise.all(fileReaders).then((values) => {
      const commonData = {
        serverId: serverId,
        proxyPort: model.proxyPort ? parseInt(model.proxyPort, 10) : 8022,
        parentFQDN: model.parentFQDN,
        maxSquidCacheSize: parseInt(model.maxSquidCacheSize, 10),
        proxyAdminEmail: model.proxyAdminEmail,
        sourceMode: model.sourceMode,
        registryMode: model.registryMode,
        useCertsMode: model.useCertsMode,
      };
      const registryData = getRegistryData(model);

      const formData = unflattenModel(Object.assign({}, commonData, registryData, ...values));
      Network.post("/rhn/manager/systems/details/proxy-config", formData).then(
        (data) => {
          setSuccess(data.success);
          setMessages([]);
          setLoading(false);
        },
        (xhr) => {
          try {
            setSuccess(false);
            setMessages(JSON.parse(xhr.responseText).messages);
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
    setModel(modelDefaults);
  };

  const onValidate = (isValidated: boolean) => {
    setIsValidated(isValidated);
  };

  const onChange = (newModel) => {
    setModel({ ...newModel });
  };

  const onAddField = (fieldName: string) => {
    return (index: number) => setModel({ ...model, [fieldName + index]: "" });
  };

  const onRemoveField = (fieldName: string) => {
    return (index: number) => {
      const newModel = { ...model };
      delete newModel[`${fieldName}${index}`];
      setModel(newModel);
    };
  };

  const onChangeSourceMode = (e, v) => {
    if (SourceMode.Registry === v && SourceMode.Registry === originalConfig.sourceMode) {
      restoreRegistryInputs(model, setModel, originalConfig);
    }
  };

  const onChangeRegistryMode = (e, v) => {
    if (originalConfig.registryMode === v && Object.keys(originalConfig).length > 0) {
      restoreRegistryInputs(model, setModel, originalConfig);
    }
  };

  return (
    <TopPanel
      title={t("Proxy Configuration")}
      icon="fa fa-cogs"
      helpUrl="installation-and-upgrade/proxy-container-setup.html"
    >
      <p>{t("Convert an already onboarded minion to a proxy or update the configuration of an existing proxy.")}</p>
      {ContainerConfigMessages(success, messages, loading)}
      {(!validationErrors || validationErrors.length === 0) && (
        <Form
          className="mt-5"
          divClass="row"
          model={model}
          onValidate={onValidate}
          onChange={onChange}
          onSubmit={onSubmit}
          title={t("Convert Minion to Proxy")}
        >
          <DEPRECATED_Select
            name="parentFQDN"
            label={t("Parent FQDN")}
            hint={t("The FQDN of the parent (server or proxy) to connect to.")}
            required
            placeholder={t("e.g., server.domain.com")}
            labelClass="col-md-3"
            divClass="col-md-6"
            options={parents}
            isClearable={true}
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
            hint={t("The maximum value of the Squid cache in Gigabytes")}
            required
            validators={[Validation.isInt({ gt: 0 })]}
            placeholder={t("e.g., 100")}
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
          <hr />
          {hasCertificates && (
            <Radio
              name="useCertsMode"
              label={t("Certificates")}
              title={t("Certificates")}
              hint={"Use existing certificates or upload new ones"}
              inline={true}
              required
              labelClass="col-md-3"
              divClass="col-md-6"
              defaultValue={UseCertsMode.Keep}
              items={[
                { label: t("Keep"), value: UseCertsMode.Keep },
                { label: t("Replace"), value: UseCertsMode.Replace },
              ]}
            />
          )}
          {hasCertificates && model.useCertsMode === UseCertsMode.Keep && (
            <div className="offset-md-3 col-md-6">
              <Messages
                items={[
                  {
                    severity: "info",
                    text: t(
                      "Keeping the current certificates means no changes will be made to the Root CA, Intermediate CAs, Proxy certificate, or Proxy SSL private key."
                    ),
                  },
                ]}
              />
            </div>
          )}
          {(currentConfig === undefined || model.useCertsMode === UseCertsMode.Replace) && (
            <>
              <Text
                name="rootCA"
                label={t("Root CA")}
                hint={t("Certificate authority that issued the proxy certificate (PEM format)")}
                required
                type="file"
                labelClass="col-md-3"
                divClass="col-md-6"
              />
              <div className="form-group">
                <FormMultiInput
                  id="intermediateCAs"
                  title={t("Intermediate CAs")}
                  prefix="intermediateCAs"
                  onAdd={onAddField("intermediateCAs")}
                  onRemove={onRemoveField("intermediateCAs")}
                  panelHeading="label"
                  panelClassName="panel-default col-md-6 offset-md-3 no-padding"
                >
                  {(index) => (
                    <Text
                      name={`intermediateCAs${index}`}
                      label={t("An intermediate CA certificate (PEM format)")}
                      required
                      type="file"
                      divClass="col-md-8"
                      labelClass="col-md-4 no-padding"
                      className="col-md-11"
                    />
                  )}
                </FormMultiInput>
              </div>
              <Text
                name="proxyCertificate"
                label={t("Proxy certificate")}
                hint={t("SSL certificate issued for the proxy system (PEM format)")}
                required
                type="file"
                labelClass="col-md-3"
                divClass="col-md-6"
              />
              <Text
                name="proxyKey"
                label={t("Proxy SSL private key")}
                hint={t("SSL private key for the proxy system (PEM format)")}
                required
                type="file"
                labelClass="col-md-3"
                divClass="col-md-6"
              />
            </>
          )}
          <hr />
          <Radio
            name="sourceMode"
            label={t("Source")}
            title={t("Source")}
            hint={"Images/Containers source"}
            inline={true}
            required
            labelClass="col-md-3"
            divClass="col-md-6"
            defaultValue={SourceMode.RPM}
            items={[
              { label: t("RPM"), value: SourceMode.RPM },
              { label: t("Registry"), value: SourceMode.Registry },
            ]}
            onChange={onChangeSourceMode}
          />
          <div className="offset-md-3 col-md-6">
            <Messages
              items={[
                {
                  severity: "warning",
                  text: t(
                    "The availability of container images depends on your environment’s connectivity. In air-gapped/restricted environments, using RPM as the source mode is recommended to ensure all required images are available locally. Selecting an incompatible source mode may result in deployment failures."
                  ),
                },
              ]}
            />
          </div>
          {model.sourceMode === SourceMode.Registry && (
            <>
              <Radio
                name="registryMode"
                label={t("Registry Source")}
                title={t("Registry Source")}
                inline={true}
                required
                labelClass="col-md-3"
                divClass="col-md-6"
                defaultValue={RegistryMode.Simple}
                items={[
                  { label: t("Simple"), value: RegistryMode.Simple },
                  { label: t("Advanced"), value: RegistryMode.Advanced },
                ]}
                onChange={onChangeRegistryMode}
              />
              {model.registryMode === RegistryMode.Simple && (
                <>
                  <Text
                    name="registryBaseURL"
                    label={t("Registry URL")}
                    placeholder={t("e.g., " + registryUrlExample)}
                    required
                    labelClass="col-md-3"
                    divClass="col-md-6"
                  />
                  <Text
                    name="registryBaseTag"
                    label={t("Containers Tag")}
                    placeholder={t("e.g. {registryTagExample}", { registryTagExample })}
                    required
                    labelClass="col-md-3"
                    divClass="col-md-6"
                  />
                </>
              )}
              {model.registryMode === RegistryMode.Advanced && (
                <>
                  <Text
                    name="registryHttpdURL"
                    label={t("HTTPD URL")}
                    placeholder={t("e.g., {registryUrlExample}/proxy-httpd", { registryUrlExample })}
                    required
                    labelClass="col-md-3"
                    divClass="col-md-6"
                  />
                  <Text
                    name="registryHttpdTag"
                    label={t("HTTPD Tag")}
                    placeholder={t("e.g. {registryTagExample}", { registryTagExample })}
                    required
                    labelClass="col-md-3"
                    divClass="col-md-6"
                  />

                  <Text
                    name="registrySaltbrokerURL"
                    label={t("Salt Broker URL")}
                    placeholder={t("e.g. {registryUrlExample}/proxy-salt-broker", { registryUrlExample })}
                    required
                    labelClass="col-md-3"
                    divClass="col-md-6"
                  />
                  <Text
                    name="registrySaltbrokerTag"
                    label={t("Salt Broker Tag")}
                    placeholder={t("e.g. {registryTagExample}", { registryTagExample })}
                    required
                    labelClass="col-md-3"
                    divClass="col-md-6"
                  />

                  <Text
                    name="registrySquidURL"
                    label={t("Squid URL")}
                    placeholder={t("e.g. {registryUrlExample}/proxy-squid", { registryUrlExample })}
                    required
                    labelClass="col-md-3"
                    divClass="col-md-6"
                  />
                  <Text
                    name="registrySquidTag"
                    label={t("Squid Tag")}
                    placeholder={t("e.g. {registryTagExample}", { registryTagExample })}
                    required
                    labelClass="col-md-3"
                    divClass="col-md-6"
                  />

                  <Text
                    name="registrySshURL"
                    label={t("SSH URL")}
                    placeholder={t("e.g. {registryUrlExample}/proxy-ssh", { registryUrlExample })}
                    required
                    labelClass="col-md-3"
                    divClass="col-md-6"
                  />
                  <Text
                    name="registrySshTag"
                    label={t("SSH Tag")}
                    placeholder={t("e.g. {registryTagExample}", { registryTagExample })}
                    required
                    labelClass="col-md-3"
                    divClass="col-md-6"
                  />

                  <Text
                    name="registryTftpdURL"
                    label={t("TFTPD URL")}
                    placeholder={t("e.g. {registryUrlExample}/proxy-tftpd", { registryUrlExample })}
                    required
                    labelClass="col-md-3"
                    divClass="col-md-6"
                  />
                  <Text
                    name="registryTftpdTag"
                    label={t("TFTPD Tag")}
                    placeholder={t("e.g. {registryTagExample}", { registryTagExample })}
                    required
                    labelClass="col-md-3"
                    divClass="col-md-6"
                  />
                </>
              )}
            </>
          )}
          <div className="offset-md-3 col-md-6 mb-2">
            <SubmitButton id="submit-btn" className="btn-primary" text={t("Apply")} disabled={!isValidated} />
            <AsyncButton
              id="clear-btn"
              defaultType="btn-default pull-right"
              icon="fa-eraser"
              text={t("Clear fields")}
              action={clearFields}
              type="reset"
            />
          </div>
        </Form>
      )}
    </TopPanel>
  );
}
