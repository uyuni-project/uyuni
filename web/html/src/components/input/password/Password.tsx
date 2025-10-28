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

export function Password({ required = false, disabled = false, ...props }: Props) {
  return <Text type="password" required={required} disabled={disabled} {...props} />;
}
