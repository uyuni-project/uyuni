/* eslint-disable no-console */

import "font-awesome/css/font-awesome.css";
import "manager/polyfills";

import type { Decorator, Preview } from "@storybook/react-webpack5";
import suseDarkTheme from "branding/css/suse-dark.scss?lazy";
import suseLightTheme from "branding/css/suse-light.scss?lazy";
import uyuniTheme from "branding/css/uyuni.scss?lazy";
import jQueryImport from "jquery";

import { t } from "core/intl";
import Loggerhead from "core/log/loggerhead";

const jQuery = jQueryImport as unknown as JQueryStatic;

const themeNames = ["uyuni", "suse-light", "suse-dark"] as const;

type ThemeName = (typeof themeNames)[number];

type LazyStylesheet = {
  use(): void;
  unuse(): void;
};

const themes: Record<ThemeName, LazyStylesheet> = {
  uyuni: uyuniTheme,
  "suse-light": suseLightTheme,
  "suse-dark": suseDarkTheme,
};

const STORYBOOK_SCROLL_OVERRIDES_ID = "uyuni-storybook-scroll-overrides";

// Uyuni themes set html/body to a fixed, hidden-overflow app shell. Storybook renders without that shell,
// so the preview iframe needs normal document scrolling.
const installStorybookScrollOverrides = () => {
  if (document.getElementById(STORYBOOK_SCROLL_OVERRIDES_ID)) {
    return;
  }

  const styleElement = document.createElement("style");
  styleElement.id = STORYBOOK_SCROLL_OVERRIDES_ID;
  styleElement.textContent = `
    html,
    body {
      height: auto !important;
      max-height: none !important;
      overflow: auto !important;
    }

    body {
      min-width: 0 !important;
    }
  `;
  document.head.appendChild(styleElement);
};

installStorybookScrollOverrides();

let activeTheme: ThemeName | undefined;

const isThemeName = (theme: unknown): theme is ThemeName =>
  typeof theme === "string" && themeNames.includes(theme as ThemeName);

const setTheme = (theme: ThemeName) => {
  if (activeTheme === theme) {
    return;
  }

  if (activeTheme) {
    themes[activeTheme].unuse();
  }

  themes[theme].use();
  document.body.classList.remove(...themeNames.map((themeName) => `theme-${themeName}`));
  document.body.classList.add("new-theme", `theme-${theme}`);
  activeTheme = theme;
};

type StorybookWindow = typeof window & {
  $?: JQueryStatic;
  jQuery?: JQueryStatic;
  preferredLocale?: string;
  docsLocale?: string;
  isUyuni?: boolean;
  userPrefPageSize?: number;
  csrfToken?: string;
  serverTimeZone?: string;
  userTimeZone?: string;
  userDateFormat?: string;
  userTimeFormat?: string;
  userShortTimeFormat?: string;
};

type StorybookGlobal = typeof globalThis & {
  t: typeof t;
  jQuery: JQueryStatic;
  Loggerhead: Loggerhead;
};

const storybookWindow = window as StorybookWindow;
const storybookGlobal = globalThis as StorybookGlobal;

// Provide the minimal Uyuni runtime globals that shared components normally get from the JSP/Jade app shell.
storybookWindow.preferredLocale ??= "en_US";
storybookWindow.docsLocale ??= "en";
storybookWindow.isUyuni ??= true;
storybookWindow.userPrefPageSize ??= 15;
storybookWindow.csrfToken ??= "storybook";
storybookWindow.serverTimeZone ??= "UTC";
storybookWindow.userTimeZone ??= "UTC";
storybookWindow.userDateFormat ??= "YYYY-MM-DD";
storybookWindow.userTimeFormat ??= "HH:mm:ss";
storybookWindow.userShortTimeFormat ??= "HH:mm";
storybookWindow.jQuery = jQuery;
storybookWindow.$ = jQuery;

storybookGlobal.t = t;
storybookGlobal.jQuery = jQuery;

const loggerHead = new Loggerhead("", (headers) => headers);
loggerHead.info = console.info.bind(console, "[Loggerhead] INFO:");
loggerHead.debug = console.debug.bind(console, "[Loggerhead] DEBUG:");
loggerHead.warn = console.warn.bind(console, "[Loggerhead] WARN:");
loggerHead.error = console.error.bind(console, "[Loggerhead] ERROR:");
storybookGlobal.Loggerhead = loggerHead;

// Mirror the JSP app shell: exactly one branded stylesheet is active and components render below `.new-theme`.
const withUyuniTheme: Decorator = (Story, context) => {
  const theme = isThemeName(context.globals.theme) ? context.globals.theme : "uyuni";
  setTheme(theme);

  return (
    <div className={`theme-${theme} new-theme`}>
      <Story />
    </div>
  );
};

const preview: Preview = {
  decorators: [withUyuniTheme],
  globalTypes: {
    theme: {
      description: "Uyuni web theme",
      toolbar: {
        title: "Theme",
        icon: "paintbrush",
        items: [
          { value: "uyuni", title: "Uyuni" },
          { value: "suse-light", title: "SUSE Light" },
          { value: "suse-dark", title: "SUSE Dark" },
        ],
      },
    },
  },
  initialGlobals: {
    theme: "uyuni",
  },
  parameters: {
    controls: {
      expanded: true,
      matchers: {
        color: /(background|color)$/i,
        date: /Date$/i,
      },
    },
    docs: {
      source: {
        type: "dynamic",
      },
    },
    options: {
      storySort: {
        order: ["Components", "Manager", "Legacy"],
        method: "alphabetical",
      },
    },
  },
};

export default preview;
