declare global {
  interface Window {
    _IS_UYUNI: boolean;
  }

  function t(msg: string, ...args: Array<any>): string;
  // TODO: This should be obsolete after https://github.com/SUSE/spacewalk/issues/13145
  var moment: any;
}

export {};
