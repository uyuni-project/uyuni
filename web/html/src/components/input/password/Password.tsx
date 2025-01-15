import * as React from "react";

import { InputBaseProps } from "../InputBase";
import { Text } from "../text/Text";

type Props = InputBaseProps & {
  /** Value placeholder to display when no value is entered */
  placeholder?: string;

  /** CSS class for the <input> element */
  inputClass?: string;

  /** name of the field to map in the form model */
  name: string;
};

export function Password(props: Props) {
  return <Text type="password" {...props} />;
}

Password.defaultProps = {
  placeholder: undefined,
  inputClass: undefined,
  defaultValue: undefined,
  label: undefined,
  hint: undefined,
  labelClass: undefined,
  divClass: undefined,
  required: false,
  disabled: false,
  invalidHint: undefined,
  onChange: undefined,
};
