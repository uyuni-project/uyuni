import { showErrorToastr, showSuccessToastr, showWarningToastr } from "components/toastr";

type Theme = "susemanager-light" | "susemanager-dark" | "uyuni";

const debugUtils = {
  theme() {
    const theme = document.querySelector<HTMLLinkElement>(
      'link[href^="/css/susemanager-light"],link[href^="/css/susemanager-dark"],link[href^="/css/uyuni"]'
    );
    if (!theme) {
      throw new TypeError("Unable to identify theme");
    }
    return `the theme is: ${theme.getAttribute("href")}`;
  },
  toggleTheme(toTheme?: Theme) {
    const lightTheme = document.querySelector('link[href^="/css/susemanager-light"]');
    if (lightTheme) {
      const to = toTheme || lightTheme.getAttribute("href")!.replace("susemanager-light", "uyuni");
      lightTheme.setAttribute("href", to);
      return `the theme is now: ${to}`;
    }

    const darkTheme = document.querySelector('link[href^="/css/susemanager-dark"]');
    if (darkTheme) {
      const to = toTheme || darkTheme.getAttribute("href")!.replace("susemanager-dark", "uyuni");
      darkTheme.setAttribute("href", to);
      return `the theme is now: ${to}`;
    }

    const uyuniTheme = document.querySelector('link[href^="/css/uyuni"]');
    if (uyuniTheme) {
      const to = toTheme || uyuniTheme.getAttribute("href")!.replace("uyuni", "susemanager-light");
      uyuniTheme.setAttribute("href", to);
      return `the theme is now: ${to}`;
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
