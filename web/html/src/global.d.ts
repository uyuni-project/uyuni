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
      };
    };

    userPrefPageSize?: number;
  }

  // WIP test env setup, see ./utils/test-utils
  namespace NodeJS {
    interface Global {
      Loggerhead: any;
      jQuery: (window: Window, noGlobal?: boolean) => JQueryStatic;
    }
  }

  function t(msg: string, ...args: Array<any>): string;
  var onDocumentReadyInitOldJS: Function;
  var Loggerhead: any;
  var ace: any;
  // TODO: This should be obsolete after https://github.com/SUSE/spacewalk/issues/13145
  var moment: any;

  // Used by section-toolbar, defined in spacewalk-essentials.js
  // TODO: Handle "ReferenceError: handleSst is not defined" in jest
  var handleSst: Function | undefined;

  // Used by cveaudit and spacewalk-checkall.js
  var DWRItemSelector: any;
  var dwr: any;

  // Defined in spacewalk-checkall.js
  var numericValidate: (event: any) => any;

  /** DEPRECATED: Do **NOT** use this global for new code, prefer `useUserLocalization()` instead */
  var localTime: string | undefined;
}

export {};
