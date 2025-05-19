import { FieldInputProps } from "formik";

import { AsCheck } from "./AsCheck";
import { AsDateTimePicker } from "./AsDateTimePicker";
import { AsPassword } from "./AsPassword";
import { AsRadio } from "./AsRadio";
import { AsRange } from "./AsRange";
import { AsSelect } from "./AsSelect";
import { AsTextArea } from "./AsTextArea";
import { FieldBase } from "./FieldBase";
import { MultiField as MultiFieldBase } from "./MultiField";

const as = {
  Check: AsCheck,
  DateTimePicker: AsDateTimePicker,
  Password: AsPassword,
  TextArea: AsTextArea,
  Range: AsRange,
  Radio: AsRadio,
  Select: AsSelect,
};

export type AsProps<T> = FieldInputProps<T>;

export const Field = Object.assign(FieldBase, as);
export const MultiField = Object.assign(MultiFieldBase, as);
