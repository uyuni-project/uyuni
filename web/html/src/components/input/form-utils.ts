import _isNil from "lodash/isNil";
import validator from "validator";

type SingleOrArray<T> = T | T[];
interface TreeLikeModel<T = any> {
  [key: string]: SingleOrArray<T | TreeLikeModel<T>>;
}

/**
 * Convert a tree-like model into a flat model for use with the Form and Input components.
 * The tree is converted into a list of properties according to the following rules:
 *   - Each property name is the path in the tree model, with '_' to separate each level.
 *     See the form-utils.test.js for examples.
 *   - Each array item is converted by appending the item index to the property name.
 *     So {a: [{b: 12, c: 34}, {b: 56, c: 78}]} will be converted into:
 *     {a0_b: 12, a0_c: 34, a1_b: 56, a1_c: 78}
 */
export function flattenModel<T = any>(treeModel: TreeLikeModel<T>): Record<string, T> {
  return Object.entries(treeModel).reduce((result, entry) => {
    const name = entry[0];
    const value = entry[1];
    if (Array.isArray(value)) {
      const allValues = value
        .map((item, idx) => {
          if (item instanceof Object) {
            const flatValue = flattenModel(item);
            return Object.entries(flatValue).reduce((res, flatEntry) => {
              return Object.assign({}, res, { [`${name}${idx}_${flatEntry[0]}`]: flatEntry[1] });
            }, {});
          }
          return { [`${name}${idx}`]: item };
        })
        .reduce((res, item) => Object.assign({}, res, item), {});
      return Object.assign({}, result, allValues);
    } else if (value instanceof Object) {
      const flatValue = flattenModel(value);
      const subValues = Object.entries(flatValue).reduce((res, flatEntry) => {
        return Object.assign({}, res, { [`${name}_${flatEntry[0]}`]: flatEntry[1] });
      }, {});
      return Object.assign({}, result, subValues);
    }
    return Object.assign({}, result, { [name]: value });
  }, {});
}

/**
 * Remove the empty string and null values from the model.
 */
export function stripBlankValues<T = any>(flatModel: Record<string, T>): Record<string, T> {
  return Object.fromEntries(
    Object.entries(flatModel).filter((entry) => {
      if (typeof entry[1] === "string") {
        return entry[1] !== "";
      }
      return !_isNil(entry[1]);
    })
  );
}

/** Parse string values to integers where possible */
export function convertNumbers<T>(flatModel: Record<string, T>): Record<string, T> {
  return Object.fromEntries(
    Object.entries(flatModel).map((entry) => {
      if (typeof entry[1] === "string" && (validator.isFloat(entry[1]) || validator.isInt(entry[1]))) {
        const floatValue = Number.parseFloat(entry[1]);
        if (!Number.isNaN(floatValue)) {
          return [entry[0], floatValue];
        }
      }
      return entry;
    })
  );
}

/**
 * Reverse of flattenModel.
 */
export function unflattenModel<T>(flatModel: Record<string, T>): TreeLikeModel<T> {
  let treeModel: any = {};

  const aggregate = (obj: Record<string, any>, name: string, value: any) => {
    const pos = name.indexOf("_");
    if (pos >= 0) {
      const segment = name.substring(0, pos);
      const tail = name.substring(pos + 1);
      if (obj[segment] == null) {
        obj[segment] = {};
      }
      aggregate(obj[segment], tail, value);
    } else {
      obj[name] = value;
    }
  };

  const mergeArrays = (model: any): any => {
    return Object.keys(model).reduce((result, key) => {
      const matcher = key.match(/^([a-zA-Z0-9]*[A-zA-Z])[0-9]+$/);
      const mergedValue =
        typeof model[key] === "object" && !Array.isArray(model[key]) ? mergeArrays(model[key]) : model[key];
      if (matcher) {
        const name = matcher[1];
        const array = result[name] || [];
        array.push(mergedValue);
        return Object.assign({}, result, { [name]: array });
      }
      return Object.assign({}, result, { [key]: mergedValue });
    }, {});
  };

  Object.keys(flatModel)
    .sort()
    .forEach((name) => {
      aggregate(treeModel, name, flatModel[name]);
    });

  return mergeArrays(treeModel);
}
