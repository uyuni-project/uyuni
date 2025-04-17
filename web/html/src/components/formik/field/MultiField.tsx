import { FieldArray, useField } from "formik";

import { Button } from "components/buttons";
import { FormGroup, Label } from "components/input";

import { useSharedFieldConfig } from "../sharedFieldConfig";
import { FieldBase } from "./FieldBase";
import { FieldProps } from "./FieldBase.types";
import styles from "./MultiField.module.scss";

type Props<ValueType, RendererProps> = FieldProps<ValueType, RendererProps> & {
  defaultNewItemValue: ValueType;
};

export const MultiField = <ValueType, RendererProps>(props: Props<ValueType, RendererProps>) => {
  const sharedFieldConfig = useSharedFieldConfig();
  const { name, defaultNewItemValue, ...rest } = props;
  const [field] = useField < ValueType[] > (props.name);

  return (
    <FieldArray
      name={name}
      render={(arrayHelpers) => {
        return (
          <>
            {field.value?.map((_, index) => (
              <FieldBase
                {...(rest as FieldProps<ValueType, RendererProps>)}
                name={`${props.name}.${index}`}
                label={index === 0 ? props.label : ""}
                key={`${props.name}.${index}-field`}
              >
                <div className={styles.buttons}>
                  <Button
                    className="btn-default btn-sm"
                    handler={() => arrayHelpers.remove(index)}
                    title={t("Remove item")}
                    disabled={props.disabled}
                    icon="fa-minus"
                  />
                </div>
              </FieldBase>
            ))}
            <FormGroup className={props.className}>
              {typeof props.label !== "undefined" && (
                <Label
                  name={field.value?.length === 0 ? props.label : ""}
                  className={props.labelClass ?? sharedFieldConfig.labelClass ?? ""}
                  required={props.required}
                  key={`${props.name}-label`}
                />
              )}
              <div className={props.divClass ?? sharedFieldConfig.divClass ?? ""}>
                <div className={styles.fieldWrapper}>
                  <Button
                    className="btn-default btn-sm"
                    handler={() => arrayHelpers.insert(field.value.length + 1, defaultNewItemValue)}
                    title={t("Add item")}
                    disabled={props.disabled}
                    icon="fa-plus"
                  >
                    {t("Add item")}
                  </Button>
                </div>
              </div>
            </FormGroup>
          </>
        );
      }}
    />
  );
};
