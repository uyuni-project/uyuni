declare global {
  namespace NodeJS {
    interface Global {
      Loggerhead: never;
    }
  }

  var Loggerhead: never;
}

export {};
