import * as React from "react";
import { Formik, Form, Field, ErrorMessage } from "formik";
import { cloneReactElement } from "components/utils";

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

  /** TODO: This is only used in stories, deprecate it */
  onSubmitInvalid?: Function;

  /** CSS class of the form */
  className?: string;

  /** TODO: Merge with below? CSS class of the div right within the form */
  divClass?: string;

  /** CSS class name for the form direction style */
  formDirection?: string;

  /** TODO: Check if used: Accessible title of the form */
  title?: string;
};

export default class FormikForm extends React.PureComponent<Props> {
  onSubmit: FormikProps["onSubmit"] = async (values, helpers) => {
    // For backwards compatibility, this.props.onSubmit is optional
    await this.props.onSubmit?.(values, helpers);
    helpers.setSubmitting(false);
  };

  render() {
    return (
      <Formik
        initialValues={this.props.model ?? {}}
        onSubmit={this.onSubmit}
        className={this.props.className}
        title={this.props.title}
      >
        {formikProps => (
          <Form>
            <div
              className={`${this.props.formDirection || ""} ${this.props.divClass ? ` ${this.props.divClass}` : ""}`}
            >
              {React.Children.toArray(this.props.children).map(child => cloneReactElement(child, { ...formikProps }))}
            </div>
          </Form>
        )}
      </Formik>
    );
  }
}
