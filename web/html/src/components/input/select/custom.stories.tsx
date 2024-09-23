import { Form } from "../form/Form";
import { Select } from "./Select";

export default () => {
  const model = {
    flavor: ["vanilla", "strawberry"],
  };

  const options = [
    { value: "chocolate", label: "Chocolate", color: "#7B3F00" },
    { value: "strawberry", label: "Strawberry", color: "#DF0000" },
    { value: "vanilla", label: "Vanilla", color: "#F3E5AB" },
  ];

  return (
    <>
      <p>Custom layouts and colors:</p>

      <div className="panel panel-default">
        <div className="panel-body">
          <Form
            model={model}
            onChange={(newModel) => {
              model["flavor"] = newModel["flavor"];
            }}
            onSubmit={() => Loggerhead.info(model)}
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
    </>
  );
};
