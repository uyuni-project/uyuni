Multiple fields:

```jsx
import { useState } from "react";

import { SubmitButton } from "components/buttons";

import { Form } from "./Form";
import { FormMultiInput } from "./FormMultiInput";
import { Text } from "./Text";


const [model, setModel] = useState({ user0_firstname: "John", user0_lastname: "Doe" });

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
      }, {});
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
```

Single field:

```jsx
import { useState } from "react";

import { SubmitButton } from "components/buttons";

import { Form } from "./Form";
import { FormMultiInput } from "./FormMultiInput";
import { Text } from "./Text";


const [model, setModel] = useState({ user0_login: "jdoe" });

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
        [`user${index}_login`]: "",
      });
      setModel(newModel);
    }}
    onRemove={(index) => {
      const newModel = Object.entries(model).reduce((res, entry) => {
        const property = !entry[0].startsWith(`user${index}_`) ? { [entry[0]]: entry[1] } : undefined;
        return Object.assign(res, property);
      }, {});
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
```

As fields in a table:

```jsx
import { useState } from "react";

import { SubmitButton } from "components/buttons";

import { Form } from "./Form";
import { FormMultiInput } from "./FormMultiInput";
import { Text } from "./Text";


const [model, setModel] = useState({ user0_firstname: "John", user0_lastname: "Doe", user0_age: 42 });

const header = (
  <div className="row multi-input-table-row">
    <div className="column-title col-md-4">Firstname</div>
    <div className="column-title col-md-4">Lastname</div>
    <div className="column-title col-md-4">Age</div>
  </div>
);

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
        [`user${index}_age`]: "",
      });
      setModel(newModel);
    }}
    onRemove={(index) => {
      const newModel = Object.entries(model).reduce((res, entry) => {
        const property = !entry[0].startsWith(`user${index}_`) ? { [entry[0]]: entry[1] } : undefined;
        return Object.assign(res, property);
      }, {});
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
          invalidHint={t("Minimum 2 characters")}
          divClass="col-md-12"
          validators={[(value) => value.length > 2]}
          className="col-md-4"
        />
        <Text
          name={`user${index}_lastname`}
          required
          invalidHint={t("Minimum 2 characters")}
          divClass="col-md-12"
          className="col-md-4"
          validators={[(value) => value.length > 2]}
        />
        <Text name={`user${index}_age`} required divClass="col-md-12" className="col-md-4" />
      </>
    )}
  </FormMultiInput>
  <SubmitButton id="submit-btn" className="btn-success" text={t("Submit")} />
</Form>
```
