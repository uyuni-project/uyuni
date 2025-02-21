import * as React from "react";
import { useCallback, useEffect, useState } from "react";

import { debounce } from "lodash";

import { AsyncButton, SubmitButton } from "components/buttons";
import { Select } from "components/input";
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

// See java/code/src/com/suse/manager/webui/templates/proxy/proxy-config.jade
enum UseCertsMode {
  Replace = "replace",
  Keep = "keep",
}

enum SourceMode {
  Registry = "registry",
  RPM = "rpm",
}

enum RegistryMode {
  Simple = "simple",
  Advanced = "advanced",
}

type ProxyConfigModel = {
  rootCA: string;
  proxyCertificate: string;
  proxyKey: string;
  intermediateCAs?: string[];
  proxyAdminEmail: string;
  maxSquidCacheSize: string;
  parentFQDN: string;
  proxyPort: string;
  useCertsMode: UseCertsMode;
  sourceMode: SourceMode;
  registryMode: RegistryMode;
  registryBaseURL: string;
  registryBaseTag: string;
  registryHttpdURL: string;
  registryHttpdTag: string;
  registrySaltbrokerURL: string;
  registrySaltbrokerTag: string;
  registrySquidURL: string;
  registrySquidTag: string;
  registrySshURL: string;
  registrySshTag: string;
  registryTftpdURL: string;
  registryTftpdTag: string;
};

const modelDefaults = {
  rootCA: "",
  proxyCertificate: "",
  proxyKey: "",
  proxyAdminEmail: "",
  maxSquidCacheSize: "",
  parentFQDN: "",
  proxyPort: "8022",
  useCertsMode: UseCertsMode.Replace,
  sourceMode: SourceMode.Registry,
  registryMode: RegistryMode.Simple,
  registryBaseURL: "",
  registryBaseTag: "",
  registryHttpdURL: "",
  registryHttpdTag: "",
  registrySaltbrokerURL: "",
  registrySaltbrokerTag: "",
  registrySquidURL: "",
  registrySquidTag: "",
  registrySshURL: "",
  registrySshTag: "",
  registryTftpdURL: "",
  registryTftpdTag: "",
};

interface Parent {
  id: number;
  name: string;
  selected: boolean;
  disabled: boolean;
}

interface ProxyConfigProps {
  serverId: string;
  isUyuni: boolean;
  parents: Parent[];
  currentConfig: ProxyConfigModel;
  initFailMessage?: string;
}

type TagOptions = {
  registryBaseURL?: string[];
  registryHttpdURL?: string[];
  registrySaltbrokerURL?: string[];
  registrySquidURL?: string[];
  registrySshURL?: string[];
  registryTftpdURL?: string[];
};

const imageNames = [
  "registryHttpdURL",
  "registrySaltbrokerURL",
  "registrySquidURL",
  "registrySshURL",
  "registryTftpdURL",
];

const tagMapping = {
  registryBaseURL: "registryBaseTag",
  registryHttpdURL: "registryHttpdTag",
  registrySaltbrokerURL: "registrySaltbrokerTag",
  registrySquidURL: "registrySquidTag",
  registrySshURL: "registrySshTag",
  registryTftpdURL: "registryTftpdTag",
};

export function ProxyConfig({
  serverId,
  isUyuni,
  parents,
  currentConfig,
  initFailMessage,
}: Readonly<ProxyConfigProps>) {
  const [messages, setMessages] = useState<React.ReactNode[]>([]);
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState<boolean | undefined>();
  const [isValidated, setIsValidated] = useState(false);
  const [errors, setErrors] = useState({});
  const [tagOptions, setTagOptions] = useState<TagOptions>({});

  const hasExistingConfig = currentConfig !== undefined && Object.keys(currentConfig).length > 0;
  const originalConfig = { ...currentConfig };

  const [model, setModel] = useState(() => {
    const initialModel = {
      ...modelDefaults,
      ...currentConfig,
    };

    return initialModel;
  });

  useEffect(() => {
    if (currentConfig.sourceMode === SourceMode.RPM) {
      //work-around to trigger validation for filled forms using RPM
      retrieveRegistryTags(currentConfig, null);
    } else if (currentConfig.registryBaseURL) {
      retrieveRegistryTags(currentConfig, "registryBaseURL");
    } else {
      imageNames.forEach((url) => {
        if (currentConfig[url]) {
          retrieveRegistryTags(currentConfig, url);
        }
      });
    }
    if (initFailMessage) {
      setSuccess(false);
      setMessages([initFailMessage]);
    }
  }, [currentConfig]);

  const registryUrlExample = isUyuni ? "registry.opensuse.org/.../uyuni" : "registry.suse.com/suse/manager/...";

  const onSubmit = () => {
    setMessages([]);
    setLoading(true);

    const fileFields = ["rootCA", "intermediateCAs", "proxyCertificate", "proxyKey"];

    const fileReaders = Object.keys(model)
      .filter((key) => {
        const matcher = key.match(/^([a-zA-Z0-9]*[A-Za-z])[0-9]*$/);
        const fieldName = matcher ? matcher[1] : key;
        return fileFields.includes(fieldName);
      })
      .map((fieldName) => {
        const field = document.getElementById(fieldName) as HTMLInputElement;
        if (field?.files?.[0]) {
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
        serverId: serverId,
        proxyPort: model.proxyPort ? parseInt(model.proxyPort, 10) : 8022,
        parentFQDN: model.parentFQDN,
        maxSquidCacheSize: parseInt(model.maxSquidCacheSize, 10),
        proxyAdminEmail: model.proxyAdminEmail,
        sourceMode: model.sourceMode,
        registryMode: model.registryMode,
        useCertsMode: model.useCertsMode,
      };
      const registryData =
        model.sourceMode === SourceMode.Registry
          ? Object.assign(
              {},
              model.registryMode === RegistryMode.Simple
                ? {
                    registryBaseURL: model.registryBaseURL,
                    registryBaseTag: model.registryBaseTag,
                  }
                : {
                    registryHttpdURL: model.registryHttpdURL,
                    registryHttpdTag: model.registryHttpdTag,
                    registrySaltbrokerURL: model.registrySaltbrokerURL,
                    registrySaltbrokerTag: model.registrySaltbrokerTag,
                    registrySquidURL: model.registrySquidURL,
                    registrySquidTag: model.registrySquidTag,
                    registrySshURL: model.registrySshURL,
                    registrySshTag: model.registrySshTag,
                    registryTftpdURL: model.registryTftpdURL,
                    registryTftpdTag: model.registryTftpdTag,
                  }
            )
          : {};

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
    setModel(Object.assign({}, newModel));
    asyncValidate(newModel);
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

  /**
   * Restore registry inputs
   */
  const restoreRegistryInputs = () => {
    setModel({
      ...model,
      registryMode: originalConfig.registryMode,
      registryBaseURL: originalConfig.registryBaseURL,
      registryBaseTag: originalConfig.registryBaseTag,
      registryHttpdURL: originalConfig.registryHttpdURL,
      registryHttpdTag: originalConfig.registryHttpdTag,
      registrySaltbrokerURL: originalConfig.registrySaltbrokerURL,
      registrySaltbrokerTag: originalConfig.registrySaltbrokerTag,
      registrySquidURL: originalConfig.registrySquidURL,
      registrySquidTag: originalConfig.registrySquidTag,
      registrySshURL: originalConfig.registrySshURL,
      registrySshTag: originalConfig.registrySshTag,
      registryTftpdURL: originalConfig.registryTftpdURL,
      registryTftpdTag: originalConfig.registryTftpdTag,
    });
  };

  const onChangeSourceMode = (e, v) => {
    if (SourceMode.Registry === v && SourceMode.Registry === originalConfig.sourceMode) {
      restoreRegistryInputs();
    }
  };

  const onChangeRegistryMode = (e, v) => {
    if (originalConfig.registryMode === v && Object.keys(originalConfig).length > 0) {
      restoreRegistryInputs();
    }
  };

  const useDebounce = (callback: (...args: any) => any, timeoutMs: number) =>
    useCallback(debounce(callback, timeoutMs), []);

  const asyncValidate = useDebounce(async (newModel: typeof model) => {
    setErrors({});
    if (newModel.registryMode === RegistryMode.Simple) {
      if (newModel.registryBaseURL && !tagOptions.registryBaseURL?.length) {
        retrieveRegistryTags(newModel, "registryBaseURL");
      }
    } else if (newModel.registryMode === RegistryMode.Advanced) {
      imageNames.forEach((property) => {
        if (newModel[property] && !tagOptions[property]?.length) {
          retrieveRegistryTags(newModel, property);
        }
      });
    }
  }, 500);

  const retrieveRegistryTags = async (newModel: typeof model, name) => {
    const registryUrl = newModel[name];
    if (!registryUrl) {
      setErrors((prev) => ({ ...prev, [name]: [] }));
      setTagOptions((prev) => ({ ...prev, [name]: [] }));
      return;
    }

    try {
      const response = await Network.post("/rhn/manager/systems/details/proxy-config/registry-url", {
        registryUrl: registryUrl,
        isExact: name !== "registryBaseURL",
      });

      if (response?.success) {
        setErrors((prev) => ({ ...prev, [name]: [] }));
        setTagOptions((prev) => ({ ...prev, [name]: response.data || [] }));

        // Check if the current tag is still in the new options
        const currentTag = newModel[tagMapping[name]];
        if (currentTag && !response.data.includes(currentTag)) {
          setModel((prev) => ({ ...prev, [tagMapping[name]]: "" }));
        }
      } else {
        const errorMessage = response?.messages?.join(", ") || "Validation Failed";
        setErrors((prev) => ({ ...prev, [name]: errorMessage }));
        setTagOptions((prev) => ({ ...prev, [name]: [] }));
        setModel((prev) => ({ ...prev, [tagMapping[name]]: "" }));
      }
    } catch (error) {
      setErrors((prev) => ({ ...prev, [name]: "Error during validation" }));
      setTagOptions((prev) => ({ ...prev, [name]: [] }));
      setModel((prev) => ({ ...prev, [tagMapping[name]]: "" }));
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
      {!initFailMessage && (
        <Form
          className=""
          divClass="row"
          model={model}
          onValidate={onValidate}
          onChange={onChange}
          onSubmit={onSubmit}
          title={t("Convert Minion to Proxy")}
          errors={errors}
        >
          <Select
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
          {hasExistingConfig && (
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
          {Object.keys(currentConfig).length > 0 && model.useCertsMode === UseCertsMode.Keep && (
            <>
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
            </>
          )}
          {(currentConfig === undefined || model.useCertsMode === UseCertsMode.Replace) && (
            <>
              <Text
                name="rootCA"
                label={t("Root CA")}
                hint={t("To sign the SSL certificate in PEM format")}
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
                      label={t("CA file in PEM format")}
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
            defaultValue={SourceMode.Registry}
            items={[
              { label: t("Registry"), value: SourceMode.Registry },
              { label: t("RPM"), value: SourceMode.RPM },
            ]}
            onChange={onChangeSourceMode}
          />
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
                  <Select
                    name="registryBaseTag"
                    label={t("Containers Tag")}
                    required
                    placeholder={t("e.g., latest")}
                    labelClass="col-md-3"
                    divClass="col-md-6"
                    options={
                      tagOptions.registryBaseURL?.map((tag) => ({
                        value: tag,
                        label: tag,
                      })) || []
                    }
                    isClearable={true}
                  />
                </>
              )}
              {model.registryMode === RegistryMode.Advanced && (
                <>
                  <Text
                    name="registryHttpdURL"
                    label={t("HTTPD URL")}
                    placeholder={t("e.g., " + registryUrlExample + "/proxy-httpd")}
                    required
                    labelClass="col-md-3"
                    divClass="col-md-6"
                  />
                  <Select
                    name="registryHttpdTag"
                    label={t("HTTPD Tag")}
                    required
                    placeholder={t("e.g., latest")}
                    labelClass="col-md-3"
                    divClass="col-md-6"
                    options={
                      tagOptions.registryHttpdURL?.map((tag) => ({
                        value: tag,
                        label: tag,
                      })) || []
                    }
                    isClearable={true}
                  />

                  <Text
                    name="registrySaltbrokerURL"
                    label={t("Salt Broker URL")}
                    placeholder={t("e.g., " + registryUrlExample + "/proxy-salt-broker")}
                    required
                    labelClass="col-md-3"
                    divClass="col-md-6"
                  />
                  <Select
                    name="registrySaltbrokerTag"
                    label={t("Salt Broker Tag")}
                    required
                    placeholder={t("e.g., latest")}
                    labelClass="col-md-3"
                    divClass="col-md-6"
                    options={
                      tagOptions.registrySaltbrokerURL?.map((tag) => ({
                        value: tag,
                        label: tag,
                      })) || []
                    }
                    isClearable={true}
                  />

                  <Text
                    name="registrySquidURL"
                    label={t("Squid URL")}
                    placeholder={t("e.g., " + registryUrlExample + "/proxy-squid")}
                    required
                    labelClass="col-md-3"
                    divClass="col-md-6"
                  />
                  <Select
                    name="registrySquidTag"
                    label={t("Squid Tag")}
                    required
                    placeholder={t("e.g., latest")}
                    labelClass="col-md-3"
                    divClass="col-md-6"
                    options={
                      tagOptions.registrySquidURL?.map((tag) => ({
                        value: tag,
                        label: tag,
                      })) || []
                    }
                    isClearable={true}
                  />

                  <Text
                    name="registrySshURL"
                    label={t("SSH URL")}
                    placeholder={t("e.g., " + registryUrlExample + "/proxy-ssh")}
                    required
                    labelClass="col-md-3"
                    divClass="col-md-6"
                  />
                  <Select
                    name="registrySshTag"
                    label={t("SSH Tag")}
                    required
                    placeholder={t("e.g., latest")}
                    labelClass="col-md-3"
                    divClass="col-md-6"
                    options={
                      tagOptions.registrySshURL?.map((tag) => ({
                        value: tag,
                        label: tag,
                      })) || []
                    }
                    isClearable={true}
                  />

                  <Text
                    name="registryTftpdURL"
                    label={t("TFTPD URL")}
                    placeholder={t("e.g., " + registryUrlExample + "/proxy-tftpd")}
                    required
                    labelClass="col-md-3"
                    divClass="col-md-6"
                  />
                  <Select
                    name="registryTftpdTag"
                    label={t("TFTPD Tag")}
                    required
                    placeholder={t("e.g., latest")}
                    labelClass="col-md-3"
                    divClass="col-md-6"
                    options={
                      tagOptions.registryTftpdURL?.map((tag) => ({
                        value: tag,
                        label: tag,
                      })) || []
                    }
                    isClearable={true}
                  />
                </>
              )}
            </>
          )}
          <div className="offset-md-3 col-md-6 mb-2">
            <SubmitButton id="submit-btn" className="btn-success" text={t("Apply")} disabled={!isValidated} />
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
