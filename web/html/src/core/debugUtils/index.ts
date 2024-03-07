import { showErrorToastr, showSuccessToastr, showWarningToastr } from "components/toastr";

const debugUtils = {
  logTheme() {
    const oldTheme = document.querySelector<HTMLLinkElement>(
      'link[href^="/css/susemanager-light"],link[href^="/css/susemanager-dark"],link[href^="/css/uyuni"]'
    );
    const newTheme = document.querySelector<HTMLLinkElement>(
      'link[href^="/css/updated-susemanager-light"],link[href^="/css/updated-susemanager-dark"],link[href^="/css/updated-uyuni"]'
    );
    if (!oldTheme || !newTheme) {
      throw new TypeError("Unable to identify theme");
    }
    return `the theme is: ${oldTheme.getAttribute("href")} ${newTheme.getAttribute("href")}`;
  },
  toggleTheme() {
    // const oldWrapper = document.querySelector(".old-theme");
    // const newWrapper = document.querySelector(".new-theme");
    // oldWrapper?.classList.remove("old-theme");
    // oldWrapper?.classList.add("new-theme");
    // newWrapper?.classList.remove("new-theme");
    // newWrapper?.classList.add("old-theme");

    const oldLightTheme = document.querySelector('link[href^="/css/susemanager-light"]');
    const newLightTheme = document.querySelector('link[href^="/css/updated-susemanager-light"]');
    if (oldLightTheme && newLightTheme) {
      oldLightTheme.setAttribute("href", oldLightTheme.getAttribute("href")!.replace("susemanager-light", "uyuni"));
      newLightTheme.setAttribute(
        "href",
        newLightTheme.getAttribute("href")!.replace("updated-susemanager-light", "updated-uyuni")
      );
      this.logTheme();
      return;
    }

    const oldDarkTheme = document.querySelector('link[href^="/css/susemanager-dark"]');
    const newDarkTheme = document.querySelector('link[href^="/css/updated-susemanager-dark"]');
    if (oldDarkTheme && newDarkTheme) {
      oldDarkTheme.setAttribute("href", oldDarkTheme.getAttribute("href")!.replace("susemanager-dark", "uyuni"));
      newDarkTheme.setAttribute(
        "href",
        newDarkTheme.getAttribute("href")!.replace("updated-susemanager-dark", "updated-uyuni")
      );
      this.logTheme();
      return;
    }

    const oldUyuniTheme = document.querySelector('link[href^="/css/uyuni"]');
    const newUyuniTheme = document.querySelector('link[href^="/css/updated-uyuni"]');
    if (oldUyuniTheme && newUyuniTheme) {
      oldUyuniTheme.setAttribute("href", oldUyuniTheme.getAttribute("href")!.replace("uyuni", "susemanager-light"));
      newUyuniTheme.setAttribute(
        "href",
        newUyuniTheme.getAttribute("href")!.replace("updated-uyuni", "updated-susemanager-light")
      );
      this.logTheme();
      return;
    }

    throw new TypeError("Unable to identify theme");
  },
  toggleUpdatedTheme() {
    const oldTheme = document.querySelector(".old-theme");
    if (oldTheme) {
      oldTheme.classList.remove("old-theme");
      oldTheme.classList.add("new-theme");
      return;
    }
    const updatedTheme = document.querySelector(".new-theme");
    if (updatedTheme) {
      updatedTheme.classList.add("old-theme");
      updatedTheme.classList.remove("new-theme");
      return;
    }
  },
  showSuccessToastr,
  showWarningToastr,
  showErrorToastr,
};

declare global {
  interface Window {
    debugUtils: typeof debugUtils;
  }
}
window.debugUtils = debugUtils;

export default {};
