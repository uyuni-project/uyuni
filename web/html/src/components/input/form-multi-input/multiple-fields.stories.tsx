import { useState } from "react";

import { SubmitButton } from "components/buttons";

import { Form } from "../form/Form";
import { Text } from "../text/Text";
import { FormMultiInput } from "./FormMultiInput";

export default () => {
  const [model, setModel] = useState({ user0_firstname: "John", user0_lastname: "Doe" });

  return (
    <>
      <p>Multiple fields:</p>
      <Form
        model={model}
        onChange={setModel}
        onSubmit={(result) => console.log(result)}
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
          panelTitle={(index) => model[`user${index}_lastname`] || "New user"}
        >
          {(index) => (
            <>
              <Text
                name={`user${index}_firstname`}
                label={t("First Name")}
                required
                invalidHint={t("Minimum 2 characters")}
                labelClass="col-md-3"
                divClass="col-md-6"
                validators={[(value) => value.length > 2]}
              />
              <Text
                name={`user${index}_lastname`}
                label={t("Last Name")}
                required
                invalidHint={t("Minimum 2 characters")}
                labelClass="col-md-3"
                divClass="col-md-6"
                validators={[(value) => value.length > 2]}
              />
            </>
          )}
        </FormMultiInput>
        <SubmitButton className="btn-success" text={t("Submit")} />
      </Form>
    </>
  );
};
