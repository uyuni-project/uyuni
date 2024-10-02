type OneOrMany<T> = T | T[];
type SyncOrAsync<T> = T | Promise<T>;

export type ValidationResult = OneOrMany<Exclude<JSX.Element | string, boolean> | undefined>;
export type Validator = (...args: any[]) => SyncOrAsync<ValidationResult>;

/** String must match `regex` */
const matches =
  (regex: RegExp, message?: string): Validator =>
  (value: string) => {
    // Here and elsewhere, if you want the value to be required, set the `required` flag instead
    if (value === "") {
      return;
    }

    if (!regex.test(value)) {
      return message ?? t("Doesn't match expected format");
    }
  };

// TODO: Some places that use this are off by one from what they want to be
/** String must be at least `length` chars long */
const minLength =
  (length: number, message?: string): Validator =>
  (value: Record<string, string> | string) => {
    const defaultMessage = t(`Must be at least ${length} characters long`);
    if (typeof value === "object") {
      const isInvalid = Object.values(value).some((item) => item.length !== 0 && item.length < length);
      if (isInvalid) {
        return message ?? defaultMessage;
      }
    } else if (value.length !== 0 && value.length < length) {
      return message ?? defaultMessage;
    }
  };

/** String must be no more than `length` chars long */
const maxLength =
  (length: number, message?: string): Validator =>
  (value: Record<string, string> | string) => {
    const defaultMessage = t(`Must be no more than ${length} characters long`);
    if (typeof value === "object") {
      const isInvalid = Object.values(value).some((item) => item.length !== 0 && item.length > length);
      if (isInvalid) {
        return message ?? defaultMessage;
      }
    } else if (value.length !== 0 && value.length > length) {
      return message ?? defaultMessage;
    }
  };

/** String is integer */
const isInt =
  (message?: string): Validator =>
  (value: string) => {
    if (value === "") {
      return;
    }

    const parsed = parseInt(value, 10);
    if (isNaN(parsed) || parsed.toString() !== value) {
      return message ?? t(`Must be an integer`);
    }
  };

/** Value is no smaller than `minValue` */
const min =
  (minValue: number, message?: string): Validator =>
  (value: string) => {
    if (value === "") {
      return;
    }

    const parsed = parseFloat(value);
    if (isNaN(parsed) || parsed < minValue) {
      return message ?? t(`Must be no smaller than ${minValue}`);
    }
  };

/** Value is no larger than `maxValue` */
const max =
  (maxValue: number, message?: string): Validator =>
  (value: string) => {
    if (value === "") {
      return;
    }

    const parsed = parseFloat(value);
    if (isNaN(parsed) || parsed > maxValue) {
      return message ?? t(`Must be no larger than ${maxValue}`);
    }
  };

/** Value is greater than `gtValue` */
const gt =
  (gtValue: number, message?: string): Validator =>
  (value: string) => {
    if (value === "") {
      return;
    }

    const parsed = parseFloat(value);
    if (isNaN(parsed) || parsed <= gtValue) {
      return message ?? t(`Must be greater than ${gtValue}`);
    }
  };

/** Value is smaller than `ltValue` */
const lt =
  (ltValue: number, message?: string): Validator =>
  (value: string) => {
    if (value === "") {
      return;
    }

    const parsed = parseFloat(value);
    if (isNaN(parsed) || parsed >= ltValue) {
      return message ?? t(`Must be greater than ${ltValue}`);
    }
  };

/** Value is an integer that is no smaller than `minValue` and no larger than `maxValue` */
const intRange =
  (minValue: number, maxValue: number, message?: string): Validator =>
  (value: string) => {
    return isInt(message)(value) || min(minValue, message)(value) || max(maxValue, message)(value);
  };

/** Value is a number that is no smaller than `minValue` and no larger than `maxValue` */
const floatRange =
  (minValue: number, maxValue: number, message?: string): Validator =>
  (value: string) => {
    return min(minValue, message)(value) || max(maxValue, message)(value);
  };

export const Validation = {
  matches,
  minLength,
  maxLength,
  min,
  max,
  gt,
  lt,
  isInt,
  intRange,
  floatRange,
};
