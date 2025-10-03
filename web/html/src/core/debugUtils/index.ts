import { showErrorToastr, showSuccessToastr, showWarningToastr } from "components/toastr";

const debugUtils = {
  logTheme() {
    const theme = document.querySelector<HTMLLinkElement>(
      'link[href^="/css/updated-suse-light"],link[href^="/css/updated-suse-dark"],link[href^="/css/updated-uyuni"]'
    );
    if (!theme) {
      throw new TypeError("Unable to identify theme");
    }
    return `the theme is: ${theme.getAttribute("href")}`;
  },
  toggleTheme() {
    if (document.body.className.includes("theme-susemanager-")) {
      document.body.className = document.body.className.replace("theme-suse-light", "theme-uyuni");
      document.body.className = document.body.className.replace("theme-suse-dark", "theme-uyuni");
    } else if (document.body.className.includes("theme-uyuni")) {
      document.body.className = document.body.className.replace("theme-uyuni", "theme-suse-light");
    }

    const sumaLight = document.querySelector('link[href^="/css/updated-suse-light"]');
    if (sumaLight) {
      sumaLight.setAttribute("href", sumaLight.getAttribute("href")!.replace("updated-suse-light", "updated-uyuni"));
      this.logTheme();
      return;
    }

    const sumaDark = document.querySelector('link[href^="/css/updated-suse-dark"]');
    if (sumaDark) {
      sumaDark.setAttribute("href", sumaDark.getAttribute("href")!.replace("updated-suse-dark", "updated-uyuni"));
      this.logTheme();
      return;
    }

    const uyuni = document.querySelector('link[href^="/css/updated-uyuni"]');
    if (uyuni) {
      uyuni.setAttribute("href", uyuni.getAttribute("href")!.replace("updated-uyuni", "updated-suse-light"));
      this.logTheme();
      return;
    }

    throw new TypeError("Unable to identify theme");
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

export default debugUtils;
