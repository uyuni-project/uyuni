import {
  // eslint-disable-next-line no-restricted-imports
  Form as FormikForm,
  Formik,
  FormikConfig,
  FormikValues,
} from "formik";

import { SharedFieldConfigProvider, SharedFieldConfigType } from "./sharedFieldConfig";

export type OnSubmit<T> = FormikConfig<T>["onSubmit"];

type CustomProps = SharedFieldConfigType & {
  formDirection?: string;
  className?: string;
};

export const Form = <Values extends FormikValues = FormikValues>(props: FormikConfig<Values> & CustomProps) => {
  return (
    <Formik {...props}>
      {(context) => (
        <SharedFieldConfigProvider labelClass={props.labelClass} divClass={props.divClass}>
          <FormikForm className={`${props.formDirection ?? "form-horizontal"} ${props.className ?? ""}`}>
            {typeof props.children === "function" ? props.children(context) : props.children}
          </FormikForm>
        </SharedFieldConfigProvider>
      )}
    </Formik>
  );
};
