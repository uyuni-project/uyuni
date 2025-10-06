import gettextParser from "gettext-parser";

export default function (source) {
  this.cacheable();
  return JSON.stringify(gettextParser.po.parse(source, "utf8"));
}
