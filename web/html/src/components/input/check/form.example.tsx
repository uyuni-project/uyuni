import { SubmitButton } from "components/buttons";
import { Field, Form, OnSubmit } from "components/formik";

export default () => {
  const initialValues = {
    foo: false,
  };

  const onSubmit: OnSubmit<typeof initialValues> = async (values, helpers) => {
    Loggerhead.info("onSubmit:");
    Loggerhead.info(values);
  };

  return (
    <Form initialValues={initialValues} onSubmit={onSubmit} labelClass="col-md-3" divClass="col-md-6">
      {({ isSubmitting }) => (
        <>
          <Field name="foo" label={t("Checkbox")} as={Field.Check} />

          <SubmitButton className="btn-primary" text={t("Submit")} disabled={isSubmitting} />
        </>
      )}
    </Form>
  );
};
