import { useState } from "react";

import { SubmitButton } from "components/buttons";
import { Form, FormMultiInput, Text, Validation } from "components/input";

export default () => {
  const [model, setModel] = useState({ user0_firstname: "John", user0_lastname: "Doe", user0_age: 42 });

  const header = (
    <div className="row multi-input-table-row">
      <div className="column-title col-md-4">Firstname</div>
      <div className="column-title col-md-4">Lastname</div>
      <div className="column-title col-md-4">Age</div>
    </div>
  );

  return (
    <>
      <p>As fields in a table:</p>
      <Form
        model={model}
        onChange={setModel}
        onSubmit={(result) => Loggerhead.info(result)}
        divClass="col-md-12"
        formDirection="form-horizontal"
      >
        <FormMultiInput
          id="users"
          title={t("Users")}
          prefix="user"
          onAdd={(index) => {
            const newModel = Object.assign({}, model, {
              [`user${index}_firstname`]: "",
              [`user${index}_lastname`]: "",
              [`user${index}_age`]: "",
            });
            setModel(newModel);
          }}
          onRemove={(index) => {
            const newModel = Object.entries(model).reduce((res, entry) => {
              const property = !entry[0].startsWith(`user${index}_`) ? { [entry[0]]: entry[1] } : undefined;
              return Object.assign(res, property);
            }, {} as typeof model);
            setModel(newModel);
          }}
          disabled={false}
          header={header}
          rowClass="multi-input-table-row"
        >
          {(index) => (
            <>
              <Text
                name={`user${index}_firstname`}
                required
                divClass="col-md-12"
                validate={Validation.minLength(2)}
                className="col-md-4"
              />
              <Text
                name={`user${index}_lastname`}
                required
                divClass="col-md-12"
                className="col-md-4"
                validate={Validation.minLength(2)}
              />
              <Text name={`user${index}_age`} required divClass="col-md-12" className="col-md-4" />
            </>
          )}
        </FormMultiInput>
        <SubmitButton id="submit-btn" className="btn-success" text={t("Submit")} />
      </Form>
    </>
  );
};
