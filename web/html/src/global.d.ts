declare global {
  interface Window {
    _IS_UYUNI: boolean;
  }

  function t(msg: string, ...args: Array<any>): string;
  // TODO: Type this this in https://github.com/SUSE/spacewalk/issues/13145
  var moment: any;
}

export {};
