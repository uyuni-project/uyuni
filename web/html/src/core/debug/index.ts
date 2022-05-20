type Theme = "susemanager-light" | "susemanager-dark" | "uyuni";

class Debug {
  public toggleTheme(toTheme?: Theme) {
    const lightTheme = document.querySelector('link[href^="/css/susemanager-light"]');
    if (lightTheme) {
      lightTheme.setAttribute(
        "href",
        toTheme || lightTheme.getAttribute("href")!.replace("susemanager-light", "uyuni")
      );
      return;
    }

    const darkTheme = document.querySelector('link[href^="/css/susemanager-dark"]');
    if (darkTheme) {
      darkTheme.setAttribute("href", toTheme || darkTheme.getAttribute("href")!.replace("susemanager-dark", "uyuni"));
      return;
    }

    const uyuniTheme = document.querySelector('link[href^="/css/uyuni"]');
    if (uyuniTheme) {
      uyuniTheme.setAttribute(
        "href",
        toTheme || uyuniTheme.getAttribute("href")!.replace("uyuni", "susemanager-light")
      );
      return;
    }
  }
}

declare global {
  interface Window {
    debug?: Debug;
  }
}

const bindDebugHelpers = () => {
  window.debug = new Debug();
};

if (window.location.host.startsWith("localhost")) {
  bindDebugHelpers();
}

export default {};
