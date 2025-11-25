import { useState } from "react";

type Props = {
  setExtraVars: (value: string) => void;
};

const ExtraVariable = (props: Props) => {
  const { setExtraVars } = props;
  const [newValue, setNewValue] = useState("");

  const handleChange = (e) => {
    const value = e.target.value;
    setNewValue(value);
    setExtraVars(value);
  };

  return (
    <>
      {
        <div className="border-top mb-4">
          <div className="d-grid">
            <h4 className="pull-left">{t("Additonal variables")}</h4>
            <p>
              Use this free-form text field to define any additional variables that are not included in the main vars
              section.
            </p>
            <small className="mb-4">
              <strong>Note:</strong> If any variable defined in the Additonal variables already exists in the main
              playbook variables, the value from Additonal variables will overwrite the existing one.
            </small>
          </div>
          <div className="row">
            <div className="col-md-12 form-group">
              <textarea
                className="form-control"
                rows={10}
                value={newValue}
                onChange={handleChange}
                placeholder="Paste YAML (e.g. webapps:\n  version: 2.0)"
              />
            </div>
          </div>
        </div>
      }
    </>
  );
};

export default ExtraVariable;
