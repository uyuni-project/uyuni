import _isNil from "lodash/isNil";

// We can't infer NaN at a type level here since typeof NaN is number
const isNoValue = (input: any): input is null | undefined | "" => {
  return _isNil(input) || input === "" || (typeof input === "number" && Number.isNaN(input));
};

type MaybeInput = string | null | undefined;
type Validator = (input: MaybeInput) => boolean;

function matches(regex: RegExp): Validator {
  return (input: MaybeInput) => {
    if (isNoValue(input)) {
      return false;
    }

    const stringified = String(input);
    return regex.test(stringified);
  };
}

type IsIntConfig = { gt: number } | { gt: number; lt: number } | { lt: number };
function isInt(inputOrConfig: MaybeInput): boolean;
function isInt(inputOrConfig: IsIntConfig): Validator;
function isInt(inputOrConfig: MaybeInput | IsIntConfig) {
  if (isNoValue(inputOrConfig)) {
    return false;
  }

  if (typeof inputOrConfig === "string") {
    const parsed = Number(inputOrConfig);
    if (Number.isNaN(parsed) || !Number.isInteger(parsed)) {
      return false;
    }
    return true;
  }

  return (input: MaybeInput) => {
    if (isNoValue(input)) {
      return false;
    }

    const parsed = Number(input);
    if (Number.isNaN(parsed) || !Number.isInteger(parsed)) {
      return false;
    }

    if (inputOrConfig) {
      if ("gt" in inputOrConfig && parsed <= inputOrConfig.gt) {
        return false;
      }
      if ("lt" in inputOrConfig && parsed >= inputOrConfig.lt) {
        return false;
      }
    }
    return true;
  };
}

function isFloat(input: MaybeInput): boolean {
  if (isNoValue(input)) {
    return false;
  }

  const parsed = Number(input);
  if (Number.isNaN(parsed)) {
    return false;
  }
  return true;
}

function isURL(input: MaybeInput): boolean {
  if (isNoValue(input)) {
    return false;
  }

  let url: URL;
  try {
    url = new URL(input);
  } catch (_) {
    return false;
  }

  return url.protocol === "http:" || url.protocol === "https:";
}

export default { matches, isInt, isFloat, isURL };
