//@flow

export function objectDefaultValueHandler (defaultValue: Object) {
  return {
    get: function(target: Object, name: string) {
      return target.hasOwnProperty(name) ? target[name] : defaultValue;
    }
  }
};
