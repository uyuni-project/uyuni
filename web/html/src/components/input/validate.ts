import validator from "validator";

export type Validator = (...args: any[]) => string | string[] | undefined | Promise<string | string[] | undefined>;

// TODO: Implement
// const combine = ()

const all =
  (validators: Validator[]): Validator =>
  async (value: string) => {
    const result = await Promise.all(validators.map((item) => item(value)));
    return result.filter((item) => item !== undefined).flat();
  };

const matches =
  (regex: RegExp, message?: string): Validator =>
  (value: string) => {
    if (!regex.test(value)) {
      return message ?? t("Doesn't match expected format");
    }
  };

const minLength =
  (length: number, message?: string): Validator =>
  (value: string) => {
    if (value.length < length) {
      return message ?? t(`Minimum ${length} characters`);
    }
  };

export const Validate = {
  all,
  matches,
  minLength,
};
