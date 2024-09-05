import { useState } from "react";

import { SubmitButton } from "components/buttons";

import { Form } from "../form/Form";
import { Text } from "../text/Text";
import { FormMultiInput } from "./FormMultiInput";

export default () => {
  const [model, setModel] = useState({ user0_login: "jdoe" });

  return (
    <>
      <p>Single field:</p>
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
              [`user${index}_login`]: "",
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
        >
          {(index) => (
            <>
              <Text
                name={`user${index}_login`}
                label={t("Login")}
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
