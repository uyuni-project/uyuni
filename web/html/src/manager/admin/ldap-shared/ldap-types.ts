export type OrgOption = {
  id: number;
  name: string;
};

export type LdapServerProperties = {
  label: string;
  enabled: boolean;
  priority: number | "";
  serverType: string;
  host: string;
  port: number | "";
  transport: string;
  connectTimeout: number | "";
  responseTimeout: number | "";
  bindDn: string;
  bindPassword: string;
  userBaseDn: string;
  userFilter: string;
  loginAttribute: string;
  firstNameAttribute: string;
  lastNameAttribute: string;
  emailAttribute: string;
  groupBaseDn: string;
  groupFilter: string;
  groupNameAttribute: string;
  useMemberOf: boolean;
  provisioningMode: string;
  defaultOrgId: number | null;
  autoJoinRegularUser: boolean;
  rootCa: string;
  hasBindPassword?: boolean;
  hasRootCa?: boolean;
};

export type LdapServerResume = {
  id: number;
  label: string;
  host: string;
  port: number;
  transport: string;
  serverType: string;
  enabled: boolean;
  priority: number;
  provisioningMode: string;
  modified?: string;
};

export type LdapServerFull = LdapServerProperties & {
  id: number;
  modified?: string;
};

export const emptyLdapProperties = (): LdapServerProperties => ({
  label: "",
  enabled: true,
  priority: 0,
  serverType: "OPEN_LDAP",
  host: "",
  port: 636,
  transport: "LDAPS",
  connectTimeout: "",
  responseTimeout: "",
  bindDn: "",
  bindPassword: "",
  userBaseDn: "",
  userFilter: "",
  loginAttribute: "",
  firstNameAttribute: "",
  lastNameAttribute: "",
  emailAttribute: "",
  groupBaseDn: "",
  groupFilter: "",
  groupNameAttribute: "",
  useMemberOf: false,
  provisioningMode: "JIT",
  defaultOrgId: null,
  autoJoinRegularUser: true,
  rootCa: "",
});

export const SERVER_TYPE_OPTIONS = [
  { value: "OPEN_LDAP", label: "OpenLDAP" },
  { value: "FREE_IPA", label: "FreeIPA" },
  { value: "ACTIVE_DIRECTORY", label: "Active Directory" },
];

export const TRANSPORT_OPTIONS = [
  { value: "LDAPS", label: "LDAPS" },
  { value: "STARTTLS", label: "StartTLS" },
  { value: "PLAIN", label: "Plain" },
];

export const PROVISIONING_MODE_OPTIONS = [
  { value: "JIT", label: "Just-in-time (create users on first login)" },
  { value: "EXISTING_ONLY", label: "Existing users only" },
];
