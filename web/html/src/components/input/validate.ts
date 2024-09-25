// TODO: Add 100% test coverage to this whole file

type OneOrMany<T> = T | T[];
type SyncOrAsync<T> = T | Promise<T>;

export type ValidationResult = OneOrMany<Exclude<React.ReactNode, boolean> | undefined>;
export type Validator = (...args: any[]) => SyncOrAsync<ValidationResult>;

// TODO: This is really internal, do we need to expose this?
const all =
  (validators: Validator[]): Validator =>
  async (value: string) => {
    const result = await Promise.all(validators.map((item) => item(value)));
    return result.filter((item) => item !== undefined).flat();
  };

/** String must match `regex` */
const matches =
  (regex: RegExp, message?: string): Validator =>
  (value: string) => {
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
      const isInvalid = Object.values(value).some((item) => item.length < length);
      if (isInvalid) {
        return message ?? defaultMessage;
      }
    } else if (value.length < length) {
      return message ?? defaultMessage;
    }
  };

/** String must be no more than `length` chars long */
const maxLength =
  (length: number, message?: string): Validator =>
  (value: Record<string, string> | string) => {
    const defaultMessage = t(`Must be no more than ${length} characters long`);
    if (typeof value === "object") {
      const isInvalid = Object.values(value).some((item) => item.length > length);
      if (isInvalid) {
        return message ?? defaultMessage;
      }
    } else if (value.length > length) {
      return message ?? defaultMessage;
    }
  };

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

export const Validate = {
  all,
  matches,
  minLength,
  maxLength,
  isInt,
};
