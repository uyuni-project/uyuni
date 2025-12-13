import intlApostropheCurly from "./intl-apostrophe-curly.js";
import noRawDate from "./no-raw-date.js";

const plugin = {
  rules: {
    "no-raw-date": noRawDate,
    "intl-apostrophe-curly": intlApostropheCurly,
  },
};
export default plugin;
