import pkg from "gettext-parser";
const { po } = pkg;

export default function (source) {
  this.cacheable();
  return JSON.stringify(po.parse(source, "utf8"));
}
