import { showErrorToastr, showSuccessToastr, showWarningToastr } from "components/toastr";

type Theme = "susemanager-light" | "susemanager-dark" | "uyuni";

const logTheme = () => {
  const theme = document.querySelector(
    'link[href*="susemanager-light"]:not([disabled]),link[href*="susemanager-dark"]:not([disabled]),link[href*="uyuni"]:not([disabled])'
  );
  const name = (theme?.getAttribute("href") ?? "").replace("/css/", "").replace(/\.css.*/, "");
  console.log(`the theme is now: ${name}`);
};

const debugUtils = {
  toggleTheme(toTheme?: Theme) {
    const lightTheme = document.querySelector('link[href*="susemanager-light"]:not([disabled])');
    if (lightTheme) {
      lightTheme.setAttribute(
        "href",
        toTheme || lightTheme.getAttribute("href")!.replace("susemanager-light", "uyuni")
      );
      logTheme();
      return;
    }

    const darkTheme = document.querySelector('link[href*="susemanager-dark"]:not([disabled])');
    if (darkTheme) {
      darkTheme.setAttribute("href", toTheme || darkTheme.getAttribute("href")!.replace("susemanager-dark", "uyuni"));
      logTheme();
      return;
    }

    const uyuniTheme = document.querySelector('link[href*="uyuni"]:not([disabled])');
    if (uyuniTheme) {
      uyuniTheme.setAttribute(
        "href",
        toTheme || uyuniTheme.getAttribute("href")!.replace("uyuni", "susemanager-light")
      );
      logTheme();
      return;
    }
  },
  toggleUpdatedTheme() {
    const regularTheme = document.getElementById("web-theme");
    if (regularTheme?.getAttribute("disabled")) {
      regularTheme.removeAttribute("disabled");
    } else {
      regularTheme?.setAttribute("disabled", "disabled");
    }
    const updatedTheme = document.getElementById("updated-web-theme");
    if (updatedTheme?.getAttribute("disabled")) {
      updatedTheme.removeAttribute("disabled");
    } else {
      updatedTheme?.setAttribute("disabled", "disabled");
    }
    logTheme();
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
