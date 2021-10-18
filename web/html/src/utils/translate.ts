import Gettext from "node-gettext";

const domain = "messages";
const gt = new Gettext();

function getTranslationData() {
  if (!window.translationData) {
    window.translationData = gt;

    const poData = getPoAsJson(window.preferredLocale);
    gt.addTranslations("", domain, poData);
    window.t = translate;
  }
}

/**
 * Get the translation data. If the file is not found e.g. because the language is not (yet) supported
 * return an empty string and use the default translation en_US
 */
function getPoAsJson(locale?: string) {
  if (!locale) {
    return "";
  }
  try {
    return require(`../../../po/${locale}.po`);
  } catch (_) {
    return "";
  }
}

/**
 * Translates a string, implemented now as a 'true-bypass',
 * with placeholder replacement like Java's MessageFormat class.
 * Accepts any number of arguments after key.
 */
function translate(key: string) {
  var result = key;

  window.translationData && (result = window.translationData.gettext(result));

  // Minimal implementation of https://docs.oracle.com/javase/7/docs/api/java/text/MessageFormat.html
  for (var i = 1; i < arguments.length; i++) {
    result = result.replace(new RegExp("\\{" + (i - 1) + "}", "g"), arguments[i]);
  }

  return result;
}

export { getTranslationData };
