import {
  ErrorMessage,
  // eslint-disable-next-line no-restricted-imports
  Field as FormikField,
  useFormikContext,
} from "formik";
import _isNil from "lodash/isNil";

import { FormGroup, Label } from "components/input";

import { useId } from "utils/hooks";

import { useSharedFieldConfig } from "../sharedFieldConfig";
import styles from "./FieldBase.module.scss";
import { FieldProps } from "./FieldBase.types";

const isEmptyValue = (input: any) => {
  if (typeof input === "string") {
    return input.trim() === "";
  }
  return _isNil(input);
};

export const FieldBase = <ValueType, RendererProps>(props: FieldProps<ValueType, RendererProps>) => {
  const labelId = useId();
  const { errors, touched } = useFormikContext();
  const sharedFieldConfig = useSharedFieldConfig();

  const { children, inputClass, label, labelClass, divClass, ...rest } = props;

  let validate = props.validate;
  if (props.required) {
    validate = (value) => {
      if (isEmptyValue(value)) {
        return props.label ? t(`${props.label} is required.`) : t("required");
      }
      return props.validate?.(value);
    };
  }

  const isError = !!errors[props.name] && !!touched[props.name];
  return (
    <FormGroup isError={isError} key={`${props.name}-group`} className={props.className}>
      {typeof props.label !== "undefined" && (
        <Label
          name={props.label}
          className={props.labelClass ?? sharedFieldConfig.labelClass ?? ""}
          required={props.required}
          key={`${props.name}-label`}
          htmlFor={labelId}
        />
      )}
      <div className={props.divClass ?? sharedFieldConfig.divClass ?? ""}>
        <div className={styles.fieldWrapper}>
          <FormikField
            {...rest}
            id={labelId}
            className={`form-control ${props.inputClass ? ` ${props.inputClass}` : ""}`}
            validate={validate}
            label={props.label}
          />
          {props.children}
        </div>
        <ErrorMessage name={props.name} component="div" className={styles.helpBlock} />
      </div>
    </FormGroup>
  );
};
