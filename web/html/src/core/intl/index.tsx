import { createIntl, createIntlCache } from "@formatjs/intl";
import Gettext from "node-gettext";

const gt = new Gettext();
const domain = "messages";
const poData = getPoAsJson(window.preferredLocale);
gt.addTranslations("", domain, poData);

/**
 * Get the translation data. If the file is not found e.g. because the language is not (yet) supported
 * return an empty string and use the default translation en_US
 */
function getPoAsJson(locale?: string) {
  if (!locale) {
    return "";
  }
  try {
    return require(`../../../../po/${locale}.po`);
  } catch (_) {
    return "";
  }
}

// Proxy every translation request through to gettext so we can use the po files as-is
const alwaysExists = { configurable: true, enumerable: true };
const messages = new Proxy(
  {},
  {
    get(_, prop) {
      return gt.gettext(prop);
    },
    getOwnPropertyDescriptor() {
      return alwaysExists;
    },
  }
);

// TODO: Lift this out and share with `web-calendar.tsx`
const jsFormatLocale = window.preferredLocale ? window.preferredLocale.replace("_", "-") : "en-US";
const cache = createIntlCache();
const intl = createIntl(
  {
    locale: jsFormatLocale,
    messages,
  },
  cache
);

export const t2 = <Message extends string, Values extends Record<string, any>>(
  // This is always the default string in English, even if the page is in another locale
  defaultMessage: Message,
  /**
   * An object providing values to placeholders, e.g. for `"example {foo}"`, providing `{ foo: "text" }` would return `"example text"`.
   *
   * DOM nodes, React components, etc can also be used, e.g. `"example <bold>text</bold>"` and `{ bold: str => <b>{str}</b> }` would give `"example <b>text</b>"`.
   */
  values?: Values
) => {
  console.log(`getting for "${defaultMessage}"`);
  return intl.formatMessage(
    {
      id: defaultMessage,
      defaultMessage: defaultMessage,
    },
    values
  );
};
