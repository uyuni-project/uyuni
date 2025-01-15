/**
 * This module is a basic placeholder stopfix for CVE-2024-21528 in `node-gettext`.
 * There is currently no patched version available, once a fix is published, please update `node-gettext` and remove
 * this module and other changes introduced by this commit.
 */
// eslint-disable-next-line no-restricted-imports
import RawGettext from "node-gettext";

export default class Gettext extends RawGettext {
  addTranslations(locale: string, domain: string, translations: Record<string | symbol, unknown>) {
    if (String(locale) === "__proto__") {
      throw new RangeError("Invalid locale");
    }
    if (String(domain) === "__proto__") {
      throw new RangeError("Invalid domain");
    }
    return super.addTranslations.call(this, locale, domain, translations);
  }
}
