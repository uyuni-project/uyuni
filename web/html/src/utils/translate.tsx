import * as React from "react";

import Gettext from "node-gettext";
import ReactDOMServer from "react-dom/server";

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
function translate(msg: string): string;
function translate(msg: JSX.Element): JSX.Element;
function translate(msg: string | JSX.Element) {
  let result: string;
  let isResultJsx = false;

  if (typeof msg !== "string") {
    // If we're dealing with JSX, compile it and then replace
    isResultJsx = true;
    result = ReactDOMServer.renderToStaticMarkup(msg);
  } else {
    result = msg;
  }

  window.translationData && (result = window.translationData.gettext(result));

  // Minimal implementation of https://docs.oracle.com/javase/7/docs/api/java/text/MessageFormat.html
  for (var i = 1; i < arguments.length; i++) {
    result = result.replace(new RegExp("\\{" + (i - 1) + "}", "g"), arguments[i]);
  }

  if (isResultJsx) {
    return <span dangerouslySetInnerHTML={{ __html: result }} />;
  } else {
    return result;
  }
}

export { getTranslationData };
