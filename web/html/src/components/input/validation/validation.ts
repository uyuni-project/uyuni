type OneOrMany<T> = T | T[];
type SyncOrAsync<T> = T | Promise<T>;

export type ValidationResult = OneOrMany<Exclude<JSX.Element | string, boolean> | undefined>;
export type Validator = (...args: any[]) => SyncOrAsync<ValidationResult>;

/** String must match `regex` */
const matches =
  (regex: RegExp, message = t("Doesn't match expected format")): Validator =>
  (value: string) => {
    // Here and elsewhere, if you want the value to be required, set the `required` flag instead
    if (value === "") {
      return;
    }

    if (!regex.test(value)) {
      return message;
    }
  };

// TODO: Some places that use this are off by one from what they want to be
/** String must be at least `length` chars long */
const minLength =
  (length: number, message = t(`Must be at least ${length} characters long`)): Validator =>
  (value: Record<string, string> | string) => {
    if (typeof value === "object") {
      const isInvalid = Object.values(value).some((item) => item.length !== 0 && item.length < length);
      if (isInvalid) {
        return message;
      }
    } else if (value.length !== 0 && value.length < length) {
      return message;
    }
  };

/** String must be no more than `length` chars long */
const maxLength =
  (length: number, message = t(`Must be no more than ${length} characters long`)): Validator =>
  (value: Record<string, string> | string) => {
    if (typeof value === "object") {
      const isInvalid = Object.values(value).some((item) => item.length !== 0 && item.length > length);
      if (isInvalid) {
        return message;
      }
    } else if (value.length !== 0 && value.length > length) {
      return message;
    }
  };

/** String is integer */
const isInt =
  (message = t(`Must be an integer`)): Validator =>
  (value: string) => {
    if (value === "") {
      return;
    }

    const parsed = parseInt(value, 10);
    if (isNaN(parsed) || parsed.toString() !== value) {
      return message;
    }
  };

/** Value is an integer no smaller than `minValue` */
const min =
  (minValue: number, message = t(`Must be an integer no smaller than ${minValue}`)): Validator =>
  (value: string) => {
    if (value === "") {
      return;
    }

    const parsed = parseInt(value, 10);
    if (isNaN(parsed) || parsed.toString() !== value || parsed < minValue) {
      return message;
    }
  };

/** Value is an integer no larger than `maxValue` */
const max =
  (maxValue: number, message = t(`Must be an integer no larger than ${maxValue}`)): Validator =>
  (value: string) => {
    if (value === "") {
      return;
    }

    const parsed = parseInt(value, 10);
    if (isNaN(parsed) || parsed.toString() !== value || parsed > maxValue) {
      return message;
    }
  };

/** Value is an integer greater than `gtValue` */
const gt =
  (gtValue: number, message = t(`Must be an integer greater than ${gtValue}`)): Validator =>
  (value: string) => {
    if (value === "") {
      return;
    }

    const parsed = parseInt(value, 10);
    if (isNaN(parsed) || parsed.toString() !== value || parsed <= gtValue) {
      return message;
    }
  };

/** Value is an integer smaller than `ltValue` */
const lt =
  (ltValue: number, message = t(`Must be an integer greater than ${ltValue}`)): Validator =>
  (value: string) => {
    if (value === "") {
      return;
    }

    const parsed = parseInt(value, 10);
    if (isNaN(parsed) || parsed.toString() !== value || parsed >= ltValue) {
      return message;
    }
  };

/** Value is an integer that is no smaller than `minValue` and no larger than `maxValue` */
const range =
  (minValue: number, maxValue: number, message?: string): Validator =>
  (value: string) => {
    if (value === "") {
      return;
    }

    const parsed = parseInt(value, 10);
    if (isNaN(parsed) || parsed.toString() !== value || parsed < minValue || parsed > maxValue) {
      return message;
    }
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
  range,
};
