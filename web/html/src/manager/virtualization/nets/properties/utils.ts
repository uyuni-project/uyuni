import ipRegex from "ip-regex";

export const ipv4Pattern = ipRegex.v4({ exact: true });
export const ipv6Pattern = ipRegex.v6({ exact: true });
export const ipPattern = ipRegex({ exact: true });

export const dnsNamePattern = new RegExp(`^[a-zA-Z0-9.-]+$`);

const hexDigit = "[0-9A-Fa-f]";
export const macPattern = new RegExp(`^${hexDigit}{2}(?::${hexDigit}{2}){5}$`);
export const uuidPattern = new RegExp(`^(?:${hexDigit}{8}-(${hexDigit}{4}-){3}${hexDigit}{12})|(?:${hexDigit}{32})$`);

export const allOrNone = (value) => {
  return Object.values(value).every((item) => item != null) || Object.values(value).every((item) => item == null);
};
