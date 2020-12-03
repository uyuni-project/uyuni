declare global {
  interface Window {
    _IS_UYUNI: boolean;
  }

  // jQuery(...args: any): any;
  function t(msg: string, ...args: Array<any>): string;
  // TODO: Specify
  var moment: any;
}

export {};
