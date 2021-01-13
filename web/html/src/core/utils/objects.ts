export function objectDefaultValueHandler<T extends Object>(defaultValue: Partial<T>) {
    return {
        get: function(target: T, name: keyof T) {
            // TODO: This looks bugged, see https://github.com/SUSE/spacewalk/issues/13648
            return target.hasOwnProperty(name) ? target[name] : defaultValue;
        },
    };
}
