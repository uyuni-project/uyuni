function getUrlParam(paramName: string, parser?: StringConstructor): string | undefined;
// eslint-disable-next-line no-redeclare
function getUrlParam(paramName: string, parser: NumberConstructor): number | undefined;
// eslint-disable-next-line no-redeclare
function getUrlParam(paramName: string, parser: StringConstructor | NumberConstructor = String) {
  const urlSearchParams = new URLSearchParams(window.location.search);
  const params = Object.fromEntries(urlSearchParams.entries());
  const value = params[paramName];
  if (parser === Number) {
    // See https://stackoverflow.com/a/13676265/1470607
    const safeValue = (value || "").trim() || undefined;
    const result = parser(safeValue);
    return isNaN(result) ? undefined : result;
  }
  return parser(value);
}

export default getUrlParam;
