declare global {
  interface Window {
    // See java/code/webapp/WEB-INF/includes/leftnav.jsp
    JSONMenu: any[];
    _IS_UYUNI: boolean;

    // CSRF loopback
    csrfToken?: string;

    // Translation data
    translationData?: any;
    preferredLocale?: string;
    docsLocale?: string;

    // SPA engine and renderer
    pageRenderers?: {
      spa?: {
        globalRenderersToUpdate?: Array<{
          onSPAEndNavigation?: Function;
        }>;
        reactAppsName?: string[];
        reactRenderers?: unknown[];
        previousReactRenderers?: unknown[];
      };
      spaengine?: {
        init?: Function;
        navigate?: Function;
        appInstance?: any;
        onSpaEndNavigation?: (callback: Function) => void;
      };
    };
    spaImportReactPage: (pageName: string) => Promise<unknown>;

    userPrefPageSize?: number;
  }

  // WIP test env setup, see ./utils/test-utils
  namespace NodeJS {
    interface Global {
      jQuery: (window: Window, noGlobal?: boolean) => JQueryStatic;
    }
  }

  function t(msg: string, ...args: any[]): string;
  function t(msg: JSX.Element, ...args: any[]): JSX.Element;
  var onDocumentReadyInitOldJS: Function;
  var ace: any;
  var d3: d3;

  // Defined in spacewalk-essentials.js
  var handleSst: Function;
  var spacewalkContentObserver: MutationObserver;
  var registerSpacewalkContentObservers: Function | undefined;

  // Used by cveaudit and spacewalk-checkall.js
  var DWRItemSelector: any;
  var dwr: any;

  // Defined in spacewalk-checkall.js
  var numericValidate: (event: any) => any;
  var update_server_set: (variable: any, set_label: any, checked: any, values: any) => void;

  /** DEPRECATED: Do **NOT** use this global for new code, prefer `useUserLocalization()` instead */
  var localTime: string | undefined;
}

export {};
