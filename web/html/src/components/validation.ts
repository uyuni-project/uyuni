import _isNil from "lodash/isNil";

// We can't infer NaN at a type level here since typeof NaN is number
const isNoValue = (input: any): input is null | undefined => {
  return _isNil(input) || (typeof input === "number" && isNaN(input));
};

type MaybeInput = string | null | undefined;
type Validator = (input: MaybeInput) => boolean;

const matches = (regex: RegExp): Validator => {
  return (input: MaybeInput) => {
    if (isNoValue(input)) {
      return false;
    }

    const stringified = String(input);
    return regex.test(stringified);
  };
};

const isInt = (inputOrRange: MaybeInput | { gt: number } | { gt: number; lt: number } | { lt: number }) => {
  if (isNoValue(inputOrRange)) {
    return false;
  }

  if (typeof inputOrRange === "string") {
    const parsed = parseFloat(inputOrRange);
    if (isNaN(parsed) || !Number.isInteger(parsed)) {
      return false;
    }
    return true;
  }

  return (input: MaybeInput) => {
    if (isNoValue(input)) {
      return false;
    }

    const parsed = parseFloat(input);
    if (isNaN(parsed) || !Number.isInteger(parsed)) {
      return false;
    }

    if (inputOrRange) {
      if ("gt" in inputOrRange && parsed <= inputOrRange.gt) {
        return false;
      }
      if ("lt" in inputOrRange && parsed >= inputOrRange.lt) {
        return false;
      }
    }
    return true;
  };
};

const isFloat: Validator = (input: MaybeInput) => {
  if (isNoValue(input)) {
    return false;
  }

  const parsed = parseFloat(input);
  if (isNaN(parsed)) {
    return false;
  }
  return true;
};

const isURL: Validator = (input: MaybeInput) => {
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
};

export default { matches, isInt, isFloat, isURL };
