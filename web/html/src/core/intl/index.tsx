import { createIntl, createIntlCache } from "@formatjs/intl";

import { jsFormatPreferredLocale } from "core/user-preferences";

import type { Values } from "./inferValues";

const poData = getPoAsJson(window.preferredLocale);
console.log(poData);

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

const cache = createIntlCache();
const intl = createIntl(
  {
    locale: jsFormatPreferredLocale,
    messages: poData,
    onError: (error) => {
      if (error.code === "MISSING_TRANSLATION") {
        // Do nothing, translations are handled separately out of sync with development
        console.error(error);
      } else {
        console.error(error);
      }
    },
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
