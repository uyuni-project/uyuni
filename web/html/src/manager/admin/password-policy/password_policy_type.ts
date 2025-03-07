export type PasswordPolicyData = {
  minLength: bigint;
  maxLength: bigint;
  digitFlag: boolean;
  lowerCharFlag: boolean;
  upperCharFlag: boolean;
  consecutiveCharsFlag: boolean;
  specialCharFlag: boolean;
  specialChars: string | null;
  restrictedOccurrenceFlag: boolean;
  maxCharacterOccurrence: bigint;
};

export type PasswordPolicyProps = {
  policy: PasswordPolicyData;
  defaults: PasswordPolicyData;
};
