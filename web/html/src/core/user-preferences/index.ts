export const pageSize: number = window.userPrefPageSize || 15;
export const docsLocale: string = window.docsLocale || "en";
export const preferredLocale: string = window.preferredLocale || "en_US";
// Locale strings come with '_' from the backend but many frontend libraries expect them with '-' so we exchange these
export const jsFormatPreferredLocale: string = preferredLocale.replace("_", "-");
