/**
 * A list of updated page pathnames, e.g. `"/rhn/manager/foo/bar"`
 * NB! This must be in sync with java/code/src/com/suse/manager/webui/utils/ViewHelper.java
 */
const BOOTSTRAP_READY_PAGES: string[] = [];

export const onEndNavigate = () => {
  const pathname = window.location.pathname;
  const themeLink = document.getElementById("web-theme");
  const updatedThemeLink = document.getElementById("updated-web-theme");
  if (!themeLink || !updatedThemeLink) {
    Loggerhead.error(`Unable to identify web theme at ${pathname}`);
    return;
  }

  if (BOOTSTRAP_READY_PAGES.includes(pathname)) {
    themeLink.setAttribute("disabled", "disabled");
    updatedThemeLink.removeAttribute("disabled");
  } else {
    themeLink.removeAttribute("disabled");
    updatedThemeLink.setAttribute("disabled", "disabled");
  }
};
