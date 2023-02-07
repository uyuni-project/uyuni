Dates and times are shown in the user's configured timezone.

```jsx
import { SubmitButton } from "components/buttons";

import { localizedMoment } from "utils";

import { DateTime } from "./DateTime";
import { Form } from "./Form";

const model = {
  time: localizedMoment(),
};

<Form
  model={model}
  onChange={(newModel) => {
    model["time"] = newModel["time"];
  }}
  onSubmit={() => console.log(model)}
  divClass="col-md-12"
  formDirection="form-horizontal"
>
  <DateTime name="time" label={t("Time")} required labelClass="col-md-3" divClass="col-md-6" />
  <SubmitButton className="btn-success" text={t("Submit")} />
</Form>
```
