import { Dispatch, SetStateAction } from "react";

import Network from "utils/network";

import { ProxyConfigModel, RegistryMode, SourceMode, TagOptions, UseCertsMode } from "./proxy-config-types";

export const modelDefaults: ProxyConfigModel = {
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

export const imageNames = [
  "registryHttpdURL",
  "registrySaltbrokerURL",
  "registrySquidURL",
  "registrySshURL",
  "registryTftpdURL",
];

export const tagMapping = {
  registryBaseURL: "registryBaseTag",
  registryHttpdURL: "registryHttpdTag",
  registrySaltbrokerURL: "registrySaltbrokerTag",
  registrySquidURL: "registrySquidTag",
  registrySshURL: "registrySshTag",
  registryTftpdURL: "registryTftpdTag",
};

export const filesInput = ["rootCA", "intermediateCAs", "proxyCertificate", "proxyKey"];

export const retrieveRegistryTags = async (
  newModel: ProxyConfigModel,
  name: string,
  setErrors: Dispatch<SetStateAction<any>>,
  setTagOptions: Dispatch<SetStateAction<TagOptions>>,
  setModel: Dispatch<SetStateAction<ProxyConfigModel>>
) => {
  const registryUrl = newModel[name];
  if (!registryUrl) {
    setErrors((prev) => ({ ...prev, [name]: [] }));
    setTagOptions((prev) => ({ ...prev, [name]: [] }));
    return;
  }

  try {
    const response = await Network.post("/rhn/manager/systems/details/proxy-config/get-registry-tags", {
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

export const readFileFields = (model: any) => {
  return Object.keys(model)
    .filter((key) => {
      const matcher = /^([a-zA-Z\d]*[A-Za-z])\d*$/.exec(key);
      const fieldName = matcher ? matcher[1] : key;
      return filesInput.includes(fieldName);
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
};

export const restoreRegistryInputs = (model, setModel, originalConfig) => {
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

export const getRegistryData = (model: ProxyConfigModel) => {
  if (model.sourceMode !== SourceMode.Registry) {
    return {};
  }

  if (model.registryMode === RegistryMode.Simple) {
    return {
      registryBaseURL: model.registryBaseURL,
      registryBaseTag: model.registryBaseTag,
    };
  }

  return {
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
  };
};
