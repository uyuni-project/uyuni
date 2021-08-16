/** Get URL parameter `paramName` as a string, or undefined if not found */
function getUrlParam(paramName: string): string | undefined;
/** Get URL parameter `paramName` as a number, or undefined if not found */
function getUrlParam(paramName: string, parser: NumberConstructor): number | undefined;
function getUrlParam(paramName: string, parser?: NumberConstructor | undefined) {
  const urlSearchParams = new URLSearchParams(window.location.search);
  const params = Object.fromEntries(urlSearchParams.entries());
  const value = params[paramName];
  if (parser === Number) {
    // Avoid `Number()` making an empty string into `0`
    // See https://stackoverflow.com/a/13676265/1470607
    const safeValue = (value || "").trim() || undefined;
    const result = Number(safeValue);
    return isNaN(result) ? undefined : result;
  }
  return value || undefined;
}

function urlBounce(defaultUrl: string, qstrParamKey?: string): void {
  window.location.href = getUrlParam(qstrParamKey || "url_bounce") || defaultUrl;
}

export { getUrlParam, urlBounce };
