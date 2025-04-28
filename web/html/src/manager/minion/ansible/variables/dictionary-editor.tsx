import React, { useState } from "react";
import { Button } from "components/buttons";

type Props = {
  path: string;
  setFieldValue: (key: string, value: any) => void;
  edit: boolean;
  onClose?: () => void;
  value: any,
};

const DictionaryEditor = (props: Props) => {
  const {
    path,
    setFieldValue,
    edit = false,
    onClose,
    value
  } = props;

  const [dictName, setDictName] = useState("");
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
  }

  const handleSubmitDictionary = () => {
    if (!dictName && !edit) return;

    const newDict = {};
    entries.forEach(({ key, value }) => {
      if (key) newDict[key] = value;
    });
    const updatedDict = { ...value, ...newDict };
    setEntries([{ key: "", value: "" }]);
    !edit ? setFieldValue(`${path}.${dictName}`, newDict) : setFieldValue(`${path}`, updatedDict);
    onClose();
  };

  return (
    <div className="border-top mt-4 mb-4 pt-3">
      {!edit && (
        <>
          <div className="d-flex justify-content-between">
            <h4>Add Dictionary</h4>
            <Button icon="fa-times" handler={onClose} />
          </div>
          <div className="row">
            <div className="col-md-4 text-right"><label>{t("Name")}</label></div>
            <div className="col-md-8 form-group">
              <input
                className="form-control"
                placeholder="Name"
                value={dictName}
                onChange={(e) => setDictName(e.target.value)}
              />
            </div>
          </div>
        </>)}
      <div className="row">
        <div className="col-md-4 text-right"><label>{t("Key-Value Pairs")}</label></div>
        <div className="col-md-8 form-group">
          {entries.map((entry, idx) => (
            <div className="d-flex gap-3 p-0 m-0 mb-3" key={idx}>
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
              {(idx + 1) === entries.length ?
                <Button className="btn-default btn-sm" icon="fa-plus" title={t("Add Item")} handler={handleAddEntry} />
                : <Button className="btn-default btn-sm" icon="fa-minus" title={t("Delete Item")} handler={() => handleDeleteEntry(idx)} />}
            </div>
          ))}</div>
      </div >
      <div className="d-flex offset-md-4" >
        <Button className="btn-primary btn-sm" text="Add Dictionary" handler={handleSubmitDictionary} />
      </div>
    </div >
  );
};

export default DictionaryEditor;
