import * as React from "react";
import { Form } from "./Form";
import { Select } from "./Select";
import { SubmitButton } from "components/buttons";

export default {
  component: Select,
  title: "Forms/Select",
};

let model = {
  level: "beginner",
};

export const Example = () => (
  <Form
    model={model}
    onChange={(newModel) => {
      model["level"] = newModel["level"];
    }}
    onSubmit={() => alert(`Level: ${model["level"]}`)}
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
    <SubmitButton id="submit-btn" className="btn-success" text={t("Submit")} />
  </Form>
);

export const AdvancedExample = () => {
  const [model, setModel] = React.useState({ flavor: ["vanilla", "strawberry"] });

  const options = [
    { value: "chocolate", label: "Chocolate", color: "#7B3F00" },
    { value: "strawberry", label: "Strawberry", color: "#DF0000" },
    { value: "vanilla", label: "Vanilla", color: "#F3E5AB" },
  ];

  return (
    <div className="panel panel-default">
      <div className="panel-body">
        <Form model={model} onChange={setModel} divClass="col-md-12" formDirection="form-horizontal">
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
  );
};

export const AsyncExample = () => {
  const [model, setModel] = React.useState({
    level: 2,
  });

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

  return (
    <Form
      model={model}
      onChange={setModel}
      onSubmit={() => alert(`Level: ${model["level"]}`)}
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
  );
};
