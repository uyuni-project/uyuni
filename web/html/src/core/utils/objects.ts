// TODO: Would be nice to make this a generic where `defaultValue` is a partial or a subset of `target`
export function objectDefaultValueHandler(defaultValue: any) {
    return {
        get: function(target: any, name: string) {
            return target.hasOwnProperty(name) ? target[name] : defaultValue;
        },
    };
}
