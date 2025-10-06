import { po } from "gettext-parser";

export default function (source) {
  this.cacheable();
  return JSON.stringify(po.parse(source, "utf8"));
}
