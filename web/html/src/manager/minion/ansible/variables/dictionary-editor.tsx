import React, { useState } from "react";
import { FieldInputProps, useField } from "formik";
import { Button } from "components/buttons";
import { showErrorToastr, MessagesContainer } from "components/toastr/toastr";

type PlainObject = Record<string, any>;

const DictionaryEditor = (props: FieldInputProps<PlainObject> & { edit?: boolean, onClose?: () => void }) => {
  const [field, , helper] = useField<PlainObject>(props.name);
  const { edit = false, onClose } = props;

  const [dictionaryName, setDictionaryName] = useState("");
  const [entries, setEntries] = useState([{ key: "", value: "" }]);

  const handleAddEntry = () => {
    setEntries([...entries, { key: "", value: "" }]);
  };

  const handleEntryChange = (index, field, value) => {
    const updated = [...entries];
    updated[index][field] = value;
    setEntries(updated);
  };

  const handleDeleteEntry = (index) => {
    const updatedEntries = entries.filter((val, idx) => idx !== index);
    setEntries(updatedEntries);
  };

  const handleSubmitDictionary = () => {
  if (!dictionaryName && !edit) return;

  const newDict = {};
  const existingKeys = edit ? Object.keys(field.value || {}) : [];

  let hasDuplicate = false;

  entries.forEach(({ key, value }) => {
    if (!key) return;

    if (!edit && existingKeys.includes(dictionaryName)) {
      hasDuplicate = true;
    } else if (edit && existingKeys.includes(key)) {
      hasDuplicate = true;
    }
    newDict[key] = value;
  });

  if (hasDuplicate) {
    showErrorToastr("Key already exist", { autoHide: false, containerId: "show-duplicate-key" });
    return;
  }

  const newTree = edit
    ? { ...field.value, ...newDict }
    : { ...field.value, [dictionaryName]: newDict };

  setEntries([{ key: "", value: "" }]);
  helper.setValue(newTree);
};

  return (
    <div className="border-top mt-4 mb-4 pt-3 w-100">
      {!edit && (
        <>
          <div className="d-flex justify-content-between">
            <h5>Add Dictionary</h5>
            <Button icon="fa-times" handler={onClose} />
          </div>
          
          <div className="row">
            <div className="col-md-8 offset-md-4 mb-3 ps-0">
              <MessagesContainer containerId="show-duplicate-key" className="mb-3" />
            </div>
            <div className="col-md-4 control-label">
              <label>{t("Name")}</label>
            </div>
            <div className="col-md-8 form-group">
              <input
                className="form-control"
                placeholder="Name"
                value={dictionaryName}
                onChange={(e) => setDictionaryName(e.target.value)}
              />
            </div>
          </div>
        </>
      )}
      <div className="row">
        <div className="col-md-8 offset-md-4 mb-3 ps-0">
          <MessagesContainer containerId="show-duplicate-key" className="mb-3" />
        </div>
        <div className="col-md-4 control-label">
          <label>{t("Key-Value Pairs")}</label>
        </div>
        <div className="col-md-8 form-group">
          {entries.map((entry, idx) => (
            <div className="d-flex gap-3 p-0 m-0 mb-3" key={`entries-${idx}`}>
              <div className="w-50">
                <input
                  className="form-control"
                  placeholder="Key"
                  value={entry.key}
                  onChange={(e) => handleEntryChange(idx, "key", e.target.value)}
                />
              </div>
              <div className="w-50">
                <input
                  className="form-control"
                  placeholder="Value"
                  value={entry.value}
                  onChange={(e) => handleEntryChange(idx, "value", e.target.value)}
                />
              </div>
              {idx + 1 === entries.length ? (
                <Button className="btn-default btn-sm" icon="fa-plus" title={t("Add Item")} handler={handleAddEntry} />
              ) : (
                <Button
                  className="btn-default btn-sm"
                  icon="fa-minus"
                  title={t("Delete Item")}
                  handler={() => handleDeleteEntry(idx)}
                />
              )}
            </div>
          ))}
        </div>
      </div>
      <div className="d-flex offset-md-4">
        <Button
          className={edit ? `btn-default btn-sm` : `btn-primary btn-sm`}
          text={edit ? t("Add Key-Value Pair") : t("Add Dictionary")}
          handler={handleSubmitDictionary}
        />
      </div>
    </div>
  )
}

export default DictionaryEditor;
