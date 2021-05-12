// TODO: This is obsolete, see https://github.com/SUSE/spacewalk/issues/13648
// TEST
export function objectDefaultValueHandler(defaultValue: any) {
  return {
    get: function(target: any, name: string) {
      return target.hasOwnProperty(name) ? target[name] : defaultValue;
    },
  };
}
