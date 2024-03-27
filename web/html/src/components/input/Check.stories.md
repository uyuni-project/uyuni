```jsx
import { SubmitButton } from "components/buttons";

import { Check } from "./Check";
import { Form } from "./Form";

const model = {
  booleanValue: false,
};

<Form
  model={model}
  onSubmit={() => console.log(model)}
  divClass="col-md-12"
  formDirection="form-horizontal"
  onChange={(newModel) => {
    model["booleanValue"] = newModel["booleanValue"];
  }}
>
  <Check name="booleanValue" label="Boolean value" divClass="col-md-6 col-md-offset-3 offset-md-3" />
  <SubmitButton className="btn-success" text={t("Submit")} />
</Form>
```
