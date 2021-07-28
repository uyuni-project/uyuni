import * as React from "react";
import { Formik, Form, Field, ErrorMessage } from "formik";

type FormikProps = React.ComponentProps<typeof Formik>;
type ModelType = {
  [field: string]: any;
};

type Props = {
  /**
   * Object storing the data of the form.
   * Each field name in the form needs to map to a property of this
   * object. The value is the one displayed in the form
   */
  model?: ModelType; // TODO: Rename initialModel or initialValues

  /** Function to trigger when the Submit button is clicked */
  onSubmit?: FormikProps["onSubmit"];

  /** Children elements of the form. Usually includes fields and a submit button */
  children: React.ReactNode;
};

export default class FormikForm extends React.PureComponent<Props> {
  onSubmit: FormikProps["onSubmit"] = async (values, helpers) => {
    // For backwards compatibility, this.props.onSubmit is optional
    await this.props.onSubmit?.(values, helpers);
    helpers.setSubmitting(false);
  };

  render() {
    return (
      <Formik initialValues={this.props.model ?? {}} onSubmit={this.onSubmit}>
        {(formikProps) => {
          <Form>
            {/* TODO: Pass props to children too? See https://formik.org/docs/api/formik#formik-render-methods-and-props */}
            {this.props.children}
          </Form>;
        }}
      </Formik>
    );
  }
}
