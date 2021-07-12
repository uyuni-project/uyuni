const decByte = "(?:(?:25[0-5])|(?:2[0-4][0-9])|(?:1[0-9]{2})|(?:[1-9][0-9])|(?:[0-9]))";

const ipv4Regex = `(?:${decByte}\\.){3}${decByte}`;
export const ipv4Pattern = new RegExp(`^${ipv4Regex}$`);

const hexDigit = "[0-9A-Fa-f]";
const hexWord = `${hexDigit}{1,4}`;
const ipv6Regex = `(?:(?:${hexWord}:){7}${hexWord})|(?:(?:${hexWord}:){6}:${hexWord})|(?:(?:${hexWord}:){5}:(?:${hexWord}:)?${hexWord})|(?:(?:${hexWord}:){4}:(?:${hexWord}:){0,2}${hexWord})|(?:(?:${hexWord}:){3}:(?:${hexWord}:){0,3}${hexWord})|(?:(?:${hexWord}:){2}:(?:${hexWord}:){0,4}${hexWord})|(?:(?:${hexWord}:){6}(?:${decByte}\\.){3}${decByte})|(?:(?:${hexWord}:){0,5}:(?:${decByte}\\.){3}${decByte})|(?:::(?:${hexWord}:){0,5}(?:${decByte}\\.){3}${decByte})|(?:${hexWord}::(?:${hexWord}:){0,5}${hexWord})|(?:::(?:${hexWord}:){0,6}${hexWord})|(?:(?:${hexWord}:){1,7}:)|(?:::)`;
export const ipv6Pattern = new RegExp(`${ipv6Regex}`);

export const macPattern = new RegExp(`^${hexDigit}{2}(?::${hexDigit}{2}){5}$`);

export const ipPattern = new RegExp(`^(?:${ipv4Regex})|(?:${ipv6Regex})$`);

export const dnsNamePattern = new RegExp(`^[a-zA-Z0-9.-]+$`);

export const uuidPattern = new RegExp(`^(?:${hexDigit}{8}-(${hexDigit}{4}-){3}${hexDigit}{12})|(?:${hexDigit}{32})$`);

export const allOrNone = value => {
  return Object.values(value).every(item => item != null) || Object.values(value).every(item => item == null);
};
