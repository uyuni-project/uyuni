type Validator = (input?: any) => boolean;

function matches(regex: RegExp): Validator {
  return (input?: any) => {
    const stringified = String(input);
    return regex.test(stringified);
  };
}

function isInt(range?: { gt: number } | { gt: number; lt: number } | { lt: number }): Validator {
  return (input?: any) => {
    const parsed = parseFloat(input);
    if (isNaN(parsed) || !Number.isInteger(parsed)) {
      return false;
    }

    if (range) {
      if (range) {
        if ("gt" in range && parsed <= range.gt) {
          return false;
        }
        if ("lt" in range && parsed >= range.lt) {
          return false;
        }
      }
    }

    return true;
  };
}

export default { matches, isInt };
