```jsx
import { SubmitButton } from "components/buttons";

import { Form } from "./Form";
import { Text } from "./Text";

const model = {
  firstname: "John",
};

<Form
  model={model}
  onChange={(newModel) => {
    model["firstname"] = newModel["firstname"];
  }}
  onSubmit={() => alert(`Hello ${model["firstname"]}`)}
  divClass="col-md-12"
  formDirection="form-horizontal"
>
  <Text
    name="firstname"
    label={t("First Name")}
    required
    invalidHint={t("Minimum 2 characters")}
    labelClass="col-md-3"
    divClass="col-md-6"
    validators={[(value) => value.length > 2]}
  />
  <SubmitButton id="submit-btn" className="btn-success" text={t("Submit")} />
</Form>
```
