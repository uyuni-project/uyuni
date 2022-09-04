A simple dropdown:

```tsx
import { SubmitButton } from "components/buttons";

import { Form } from "./Form";
import { Select } from "./Select";

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
  <Select
    name="level"
    label={t("Level")}
    required
    labelClass="col-md-3"
    divClass="col-md-6"
    options={["beginner", "normal", "expert"]}
  />
  <SubmitButton className="btn-success" text={t("Submit")} />
</Form>
```

Custom layouts and colors:

```jsx
import { SubmitButton } from "components/buttons";

import { Form } from "./Form";
import { Select } from "./Select";

const model = {
  flavor: ["vanilla", "strawberry"],
};

const options = [
  { value: "chocolate", label: "Chocolate", color: "#7B3F00" },
  { value: "strawberry", label: "Strawberry", color: "#DF0000" },
  { value: "vanilla", label: "Vanilla", color: "#F3E5AB" },
];

<div className="panel panel-default">
  <div className="panel-body">
    <Form
      model={model}
      onChange={(newModel) => {
        model["flavor"] = newModel["flavor"];
      }}
      onSubmit={() => console.log(model)}
      divClass="col-md-12"
      formDirection="form-horizontal"
    >
      <Select
        name="flavor"
        label={t("Flavor")}
        options={options}
        placeholder={t("Start typing...")}
        emptyText={t("No flavors")}
        labelClass="col-md-3"
        divClass="col-md-6"
        isMulti
        formatOptionLabel={(object, { context }) => {
          if (context === "menu") {
            return <div style={{ color: object.color }}>{object.label}</div>;
          } else {
            const dotStyle = {
              backgroundColor: object.color,
              borderRadius: 10,
              display: "block",
              marginRight: 8,
              height: 10,
              width: 10,
            };
            return (
              <div style={{ alignItems: "center", display: "flex" }}>
                <div style={dotStyle}></div>
                <div>{object.label}</div>
              </div>
            );
          }
        }}
        required
      />
    </Form>
  </div>
</div>
```

Async example. To show a prefilled value for async data, use the `defaultValueOption` option with a value that matches the expected schema.

```jsx
import { SubmitButton } from "components/buttons";

import { Form } from "./Form";
import { Select } from "./Select";

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

<Form
  model={model}
  onChange={(newModel) => {
    model["level"] = newModel["level"];
  }}
  onSubmit={() => console.log(model)}
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
```
