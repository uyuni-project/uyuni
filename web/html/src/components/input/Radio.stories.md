Vertical:

```jsx
import { SubmitButton } from "components/buttons";

import { Form } from "./Form";
import { Radio } from "./Radio";

const model = {
  level: "beginner",
};

<Form
  model={model}
  onChange={(newModel) => {
    model["level"] = newModel["level"];
  }}
  onSubmit={() => console.log(model)}
  divClass="col-md-12"
  formDirection="form-horizontal"
>
  <Radio
    name="level"
    label={t("Level")}
    required
    labelClass="col-md-3"
    divClass="col-md-6"
    items={[
      { label: t("Beginner"), value: "beginner" },
      { label: t("Normal"), value: "normal" },
      { label: t("Expert"), value: "expert" },
    ]}
  />
  <SubmitButton className="btn-success" text={t("Submit")} />
</Form>
```

Vertical with open option:
```jsx
import { SubmitButton } from "components/buttons";

import { Form } from "./Form";
import { Radio } from "./Radio";

const model = {
  level: "beginner",
};

<Form
  model={model}
  onChange={(newModel) => {
    model["level"] = newModel["level"];
  }}
  onSubmit={() => console.log(model)}
  divClass="col-md-12"
  formDirection="form-horizontal"
>
  <Radio
    name="level"
    label={t("Level")}
    required
    openOption={true}
    labelClass="col-md-3"
    divClass="col-md-6"
    items={[
      { label: t("Beginner"), value: "beginner" },
      { label: t("Normal"), value: "normal" },
      { label: t("Expert"), value: "expert" },
    ]}
  />
  <SubmitButton className="btn-success" text={t("Submit")} />
</Form>
```

Horizontal:
```jsx
import { SubmitButton } from "components/buttons";

import { Form } from "./Form";
import { Radio } from "./Radio";

const model = {
  level: "beginner",
};

<Form
  model={model}
  onChange={(newModel) => {
    model["level"] = newModel["level"];
  }}
  onSubmit={() => console.log(model)}
  divClass="col-md-12"
  formDirection="form-horizontal"
>
  <Radio
    name="level"
    inline={true}
    label={t("Level")}
    required
    labelClass="col-md-3"
    divClass="col-md-6"
    items={[
      { label: t("Beginner"), value: "beginner" },
      { label: t("Normal"), value: "normal" },
      { label: t("Expert"), value: "expert" },
    ]}
  />
  <SubmitButton className="btn-success" text={t("Submit")} />
</Form>
```

Horizontal with open option:

```jsx
import { SubmitButton } from "components/buttons";

import { Form } from "./Form";
import { Radio } from "./Radio";

const model = {
  level: "beginner",
};

<Form
  model={model}
  onChange={(newModel) => {
    model["level"] = newModel["level"];
  }}
  onSubmit={() => console.log(model)}
  divClass="col-md-12"
  formDirection="form-horizontal"
>
  <Radio
    name="level"
    inline={true}
    label={t("Level")}
    required
    openOption={true}
    labelClass="col-md-3"
    divClass="col-md-6"
    items={[
      { label: t("Beginner"), value: "beginner" },
      { label: t("Normal"), value: "normal" },
      { label: t("Expert"), value: "expert" },
    ]}
  />
  <SubmitButton className="btn-success" text={t("Submit")} />
</Form>
```
