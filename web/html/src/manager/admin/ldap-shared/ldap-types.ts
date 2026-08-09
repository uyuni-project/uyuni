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

export type LdapLookupResult = {
  login: string;
  dn: string;
  firstName?: string | null;
  lastName?: string | null;
  email?: string | null;
  groupLabels: string[];
};

/** Mirrors {@code LdapServerType} Java defaults for attribute / filter prefill. */
export type ServerTypeDefaults = {
  userFilter: string;
  loginAttribute: string;
  firstNameAttribute: string;
  lastNameAttribute: string;
  emailAttribute: string;
  groupFilter: string;
  groupNameAttribute: string;
};

export const SERVER_TYPE_DEFAULTS: Record<string, ServerTypeDefaults> = {
  ACTIVE_DIRECTORY: {
    userFilter: "(&(objectClass=user)(sAMAccountName={login}))",
    loginAttribute: "sAMAccountName",
    firstNameAttribute: "givenName",
    lastNameAttribute: "sn",
    emailAttribute: "mail",
    groupFilter: "(&(objectClass=group)(member:1.2.840.113556.1.4.1941:={userDn}))",
    groupNameAttribute: "cn",
  },
  FREE_IPA: {
    userFilter: "(&(objectClass=person)(uid={login}))",
    loginAttribute: "uid",
    firstNameAttribute: "givenName",
    lastNameAttribute: "sn",
    emailAttribute: "mail",
    groupFilter: "(&(objectClass=groupOfNames)(member={userDn}))",
    groupNameAttribute: "cn",
  },
  OPEN_LDAP: {
    userFilter: "(&(objectClass=inetOrgPerson)(uid={login}))",
    loginAttribute: "uid",
    firstNameAttribute: "givenName",
    lastNameAttribute: "sn",
    emailAttribute: "mail",
    groupFilter: "(&(objectClass=groupOfNames)(member={userDn}))",
    groupNameAttribute: "cn",
  },
};

export const TRANSPORT_DEFAULT_PORTS: Record<string, number> = {
  LDAPS: 636,
  STARTTLS: 389,
  PLAIN: 389,
};

export const SERVER_TYPE_ATTR_FIELDS: (keyof ServerTypeDefaults)[] = [
  "userFilter",
  "loginAttribute",
  "firstNameAttribute",
  "lastNameAttribute",
  "emailAttribute",
  "groupFilter",
  "groupNameAttribute",
];

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
  groupBaseDn: "",
  useMemberOf: false,
  provisioningMode: "JIT",
  defaultOrgId: null,
  autoJoinRegularUser: true,
  rootCa: "",
  // Prefill attribute/filter defaults for the initial server type (RFC Attribute mappings tab).
  ...SERVER_TYPE_DEFAULTS.OPEN_LDAP,
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
