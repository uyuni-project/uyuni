import { createIntl, createIntlCache } from "@formatjs/intl";

import Gettext from "core/intl/node-gettext";
import { jsFormatPreferredLocale } from "core/user-preferences";

import type { Values } from "./inferValues";

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
    // eslint-disable-next-line @typescript-eslint/no-require-imports
    return require(`../../../../po/${locale}.po`);
  } catch (_) {
    return "";
  }
}

// We proxy every translation request directly to gettext so we can use the po files as-is without any transformations
const alwaysExists = { configurable: true, enumerable: true };
const messages = new Proxy(
  {},
  {
    get(_, key: string) {
      return gt.gettext(key);
    },
    getOwnPropertyDescriptor() {
      return alwaysExists;
    },
  }
);

const cache = createIntlCache();
const intl = createIntl(
  {
    locale: jsFormatPreferredLocale,
    messages,
  },
  cache
);

// This is exported for tests, everywhere else feel free to use the global reference
export const t = <Message extends string>(
  // This is always the default string in English, even if the page is in another locale
  defaultMessage: Message,
  /**
   * An object providing values to placeholders, e.g. for `"example {foo}"`, providing `{ foo: "text" }` would return `"example text"`.
   *
   * DOM nodes, React components, etc can also be used, e.g. `"example <bold>text</bold>"` and `{ bold: str => <b>{str}</b> }` would give `"example <b>text</b>"`.
   */
  // We could optionally ` | Record<string, any>` here if we wanted to be more lax about values in some contexts while keeping autocomplete
  values?: Values<Message>
) => {
  // react-intl is unhappy when an emtpy string is used as an id
  if (!defaultMessage) {
    return "";
  }

  return intl.formatMessage(
    {
      id: defaultMessage,
      defaultMessage: defaultMessage,
    },
    values
  );
};

export type tType = typeof t;

window.t = t;

// If we need to, we have the option to export stuff such as formatNumber etc here in the future

export default {};
