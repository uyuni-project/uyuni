export enum UseCertsMode {
  Replace = "replace",
  Keep = "keep",
}

export enum SourceMode {
  Registry = "registry",
  RPM = "rpm",
}

export enum RegistryMode {
  Simple = "simple",
  Advanced = "advanced",
}

export type ProxyConfigModel = {
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

export interface Parent {
  id: number;
  name: string;
  selected: boolean;
  disabled: boolean;
}

export type ProxyConfigProps = {
  serverId: string;
  parents: Parent[];
  currentConfig: ProxyConfigModel;
  validationErrors?: string[];
  registryUrlExample?: string;
  registryTagExample?: string;
  hasCertificates?: boolean;
};

export const RegistryBaseURL = "registryBaseURL";
