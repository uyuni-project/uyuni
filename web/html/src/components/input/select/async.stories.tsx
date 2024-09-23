import { Form } from "../form/Form";
import { Select } from "./Select";

export default () => {
  const model = {
    level: 2,
  };

  const loadOptions = () => {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve([
          {
            value: 1,
            label: "Level 1",
          },
          {
            value: 2,
            label: "Level 2",
          },
        ]);
      }, 100);
    });
  };

  return (
    <>
      <p>
        Async example. To show a prefilled value for async data, use the `defaultValueOption` option with a value that
        matches the expected schema.
      </p>
      <Form
        model={model}
        onChange={(newModel) => {
          model["level"] = newModel["level"];
        }}
        onSubmit={() => Loggerhead.info(model)}
        divClass="col-md-12"
        formDirection="form-horizontal"
      >
        <Select
          loadOptions={loadOptions}
          name="level"
          label={t("Level")}
          labelClass="col-md-3"
          divClass="col-md-6"
          getOptionValue={(item) => item.value}
          getOptionLabel={(item) => item.label}
          defaultValueOption={{ value: 2, label: "Level 2" }}
        />
      </Form>
    </>
  );
};
