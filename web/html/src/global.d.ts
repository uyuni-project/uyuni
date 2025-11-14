import type Bootstrap from "bootstrap";

import type { tType } from "core/intl";

declare global {
  interface Window {
    // See java/code/webapp/WEB-INF/includes/leftnav.jsp
    JSONMenu: any[];

    // CSRF loopback
    csrfToken?: string;

    // Translation data
    translationData?: any;
    preferredLocale?: string;
    docsLocale?: string;

    // SPA engine and renderer
    pageRenderers?: {
      spa?: {
        globalRenderersToUpdate?: {
          onSPAEndNavigation?: (...args: any[]) => any;
        }[];
        reactAppsName?: string[];
        reactRenderers?: unknown[];
        previousReactRenderers?: unknown[];
      };
      spaengine?: {
        init?: (...args: any[]) => any;
        navigate?: (...args: any[]) => any;
        appInstance?: any;
        onSpaEndNavigation?: (callback: (...args: any[]) => any) => void;
      };
    };
    spaImportReactPage: (pageName: string) => Promise<unknown>;

    userPrefPageSize?: number;
    isUyuni?: boolean;
  }

  // Test env setup, see ./utils/test-utils/setup/index.ts
  namespace NodeJS {
    interface Global {
      jQuery: (window: Window, noGlobal?: boolean) => JQueryStatic;
    }
  }

  var bootstrap: Bootstrap;
  var t: tType;

  var onDocumentReadyInitOldJS: (...args: any[]) => any;
  var d3: d3;

  // Defined in spacewalk-essentials.js
  var handleSst: (...args: any[]) => any;
  var spacewalkContentObserver: MutationObserver;
  var registerSpacewalkContentObservers: (...args: any[]) => any | undefined;

  // Defined in spacewalk-checkall.js
  var numericValidate: (event: any) => any;
  var update_server_set: (variable: any, set_label: any, checked: any, values: any) => void;

  /** DEPRECATED: Do **NOT** use this global for new code, prefer `useUserLocalization()` instead */
  var localTime: string | undefined;
}

export {};
