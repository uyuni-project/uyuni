import React, { useState } from "react";

import { FieldInputProps, useField } from "formik";

import { Button } from "components/buttons";
import { MessagesContainer, showErrorToastr } from "components/toastr/toastr";

type PlainObject = Record<string, any>;

const StringEditor = (props: FieldInputProps<PlainObject> & { onClose?: () => void }) => {
  const [field, , helper] = useField<PlainObject>(props.name);
  const { onClose } = props;

  const [newKey, setNewKey] = useState("");
  const [newValue, setNewValue] = useState("");

  const handleAddString = () => {
    if (!newKey.trim() || newKey in field.value) {
      showErrorToastr("Key already exist", { autoHide: false, containerId: "show-duplicate-key-string" });
      return;
    }
    const newTree = { ...field.value, [newKey]: newValue };
    helper.setValue(newTree);
    setNewKey("");
    setNewValue("");
  };

  return (
    <div className="border-top w-100 mt-4 mb-4 p-0">
      <div className="d-block">
        <h5 className="pull-left">{t("Add String")}</h5>
        <Button className="pull-right" icon="fa-times" handler={onClose} />
      </div>
      <div className="row">
        <div className="col-md-4 control-label">
          <label>{t("Name and value")}</label>
        </div>
        <div className="col-md-8 form-group">
          <MessagesContainer containerId="show-duplicate-key-string" />
          <div className="d-flex p-0 m-0 mb-3">
            <div className="w-50 me-2">
              <input
                className="form-control"
                placeholder="Key"
                value={newKey}
                onChange={(e) => setNewKey(e.target.value)}
              />
            </div>
            <div className="w-50 me-2">
              <input
                className="form-control"
                placeholder="Value"
                value={newValue}
                onChange={(e) => setNewValue(e.target.value)}
              />
            </div>
          </div>
          <Button className=" btn-primary btn-sm mt-2" text={t("Add String")} handler={handleAddString} />
        </div>
      </div>
    </div>
  );
};

export default StringEditor;
