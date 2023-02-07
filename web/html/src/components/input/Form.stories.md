```jsx
import { SubmitButton } from "components/buttons";

import { Form } from "./Form";
import { Text } from "./Text";

const model = {
  name: "John",
};

<Form
  model={model}
  onChange={(newModel) => {
    model["name"] = newModel["name"];
  }}
  onSubmit={() => console.log(model)}
  divClass="col-md-12"
  formDirection="form-horizontal"
>
  <Text
    name="name"
    label={t("Name")}
    required
    invalidHint={t("Minimum 2 characters")}
    labelClass="col-md-3"
    divClass="col-md-6"
    validators={[(value) => value.length > 2]}
  />
  <SubmitButton className="btn-success" text={t("Submit")} />
</Form>
```
