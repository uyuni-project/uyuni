declare global {
  interface Window {
    _IS_UYUNI: boolean;

    // Translation data
    translationData?: any;
    preferredLocale?: string;
  }

  // WIP test env setup, see ./utils/test-utils
  namespace NodeJS {
    interface Global {
      Loggerhead: any;
      jQuery: (window: Window, noGlobal?: boolean) => JQueryStatic;
    }
  }

  function t(msg: string, ...args: Array<any>): string;
  // TODO: This should be obsolete after https://github.com/SUSE/spacewalk/issues/13145
  var moment: any;
}

export {};