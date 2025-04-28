import React, { useState } from "react";
import { Button } from "components/buttons";

type Props = {
  path: string;
  setFieldValue: (key: string, value: any) => void;
  onClose?: () => void;
};

const BooleanEditor = (props: Props) => {
  const {
    path,
    setFieldValue,
    onClose
  } = props;

  const [newKey, setNewKey] = useState("");
  const [newValue, setNewValue] = useState(false);
  console.log("newValue", newValue);
  const handleAddBoolean = () => {
    if (!newKey.trim()) return;
    setFieldValue(`${path}.${newKey}`, newValue);
    setNewKey("");
    setNewValue("");
  };

  return (
    <>
      {
        <div className="border-top mt-4 mb-4 pt-3">
          <div className="d-block" >
            <h4 className="pull-left">Add Boolean</h4>
            <Button className="pull-right" icon="fa-times" handler={() => onClose()} />
          </div>
          <div className="row">
            <div className="col-md-4 text-right"><label>Name</label></div>
            <div className="col-md-8 form-group">
              <input
                className="form-control"
                placeholder="Key"
                value={newKey}
                onChange={(e) => setNewKey(e.target.value)}
              />
            </div>
          </div>

          <div className="row">
            <div className="col-md-4"></div>
            <div className="col-md-8 form-group">
              <label className="d-flex gap-3 p-0">
                <input
                  type="radio"
                  checked={newValue === true}
                  onChange={() => setNewValue(true)}
                /> {t("True")}
              </label>
              <label className="d-flex gap-3 p-0 mt-2">
                <input
                  type="radio"
                  checked={newValue === false}
                  onChange={() => setNewValue(false)}
                /> {t("False")}
              </label>
            </div>
          </div>
          <div className="d-flex offset-md-4 mt-3" >
            <Button
              className=" btn-primary btn-sm mt-2"
              text={t("Add Boolean")}
              handler={handleAddBoolean}
            />
          </div>
        </div>
      }
    </>
  )
};

export default BooleanEditor;
