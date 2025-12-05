import { SubmitButton } from "components/buttons";
import { Field, Form, MultiField, OnSubmit } from "components/formik";

import { localizedMoment } from "utils/datetime";
import { Utils } from "utils/functions";

export default () => {
  const initialValues = {
    field: "field value",
    checkbox: false,
    password: Utils.generatePassword(),
    datetime: localizedMoment(),
    multiple: [],
    longText: "multiple\nlines",
    range_start: "1000",
    range_end: "1100",
    radio: "one",
    select: "foo",
  };

  const onSubmit: OnSubmit<typeof initialValues> = async (values, helpers) => {
    Loggerhead.info("onSubmit:");
    Loggerhead.info(values);
  };

  return (
    <Form initialValues={initialValues} onSubmit={onSubmit} labelClass="col-md-3" divClass="col-md-6">
      {({ isSubmitting }) => (
        <>
          <Field
            name="field"
            label={t("Field")}
            required
            validate={(value) => (value.length < 2 ? t("Minimum 2 characters") : "")}
          />

          <Field name="checkbox" label={t("Checkbox")} as={Field.Check} />

          <Field name="password" label={t("Password")} as={Field.Password} />

          <Field name="datetime" label={t("Datetime")} as={Field.DateTimePicker} />

          <MultiField name="multiple" label={t("Multiple values")} defaultNewItemValue="" />

          <Field name="longText" label={t("Long text")} as={Field.TextArea} rows={10} />

          <Field name="range" label={t("Range")} as={Field.Range} />

          <Field
            name="radio"
            label={t("Radio")}
            items={[
              { label: t("One"), value: "one" },
              { label: t("Two"), value: "two" },
              { label: t("Three"), value: "three" },
            ]}
            as={Field.Radio}
          />

          <Field
            name="select"
            label={t("Select")}
            options={[
              {
                label: t("Foo"),
                value: "foo",
              },
              {
                label: t("Bar"),
                value: "bar",
              },
            ]}
            as={Field.Select}
          />

          <SubmitButton className="btn-primary" text={t("Submit")} disabled={isSubmitting} />
        </>
      )}
    </Form>
  );
};
