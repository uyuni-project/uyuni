/**
 * A list of updated page pathnames, e.g. `"/rhn/manager/foo/bar"`
 * NB! This must be in sync with java/code/src/com/suse/manager/webui/utils/ViewHelper.java
 */
const BOOTSTRAP_READY_PAGES: string[] = ["/rhn/YourRhn.do"];

export const onEndNavigate = () => {
  const pathname = window.location.pathname;
  const link = document.getElementById("web-theme");
  const themeHref = link?.getAttribute("data-theme");
  const updatedThemeHref = link?.getAttribute("data-updated-theme");

  if (!link || !themeHref || !updatedThemeHref) {
    Loggerhead.error(`Unable to identify web theme at ${pathname}`);
    return;
  }

  if (BOOTSTRAP_READY_PAGES.includes(pathname)) {
    if (link.getAttribute("href") !== updatedThemeHref) {
      link.setAttribute("href", updatedThemeHref);
    }
  } else if (link.getAttribute("href") !== themeHref) {
    link.setAttribute("href", themeHref);
  }
};
