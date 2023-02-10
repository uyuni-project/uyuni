// TODO: Get this dynamically after https://github.com/SUSE/spacewalk/issues/18942 is implemented
/**
 * A list of updated page pathnames, e.g. `"/rhn/manager/foo/bar"`
 */
const UPDATED_PAGES: string[] = [];

export const onEndNavigate = () => {
  const pathname = window.location.pathname;
  const themeLink = document.getElementById("web-theme");
  const updatedThemeLink = document.getElementById("updated-web-theme");
  if (!themeLink || !updatedThemeLink) {
    Loggerhead.error(`Unable to identify web theme at ${pathname}`);
    return;
  }

  if (UPDATED_PAGES.includes(pathname)) {
    themeLink.setAttribute("disabled", "disabled");
    updatedThemeLink.removeAttribute("disabled");
  } else {
    themeLink.removeAttribute("disabled");
    updatedThemeLink.setAttribute("disabled", "disabled");
  }
};
