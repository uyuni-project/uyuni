import React, { useState } from "react";
import { Button } from "components/buttons";

const ListEditor = ({ path, setFieldValue, onClose, setVisibleInputPath }) => {
  const [pendingListKey, setPendingListKey] = useState("");
  const [pendingListItems, setPendingListItems] = useState([""]);

  const handleAddList = () => {
    if (pendingListKey) {
      setFieldValue(`${path}.${pendingListKey}`, pendingListItems);
      setPendingListKey("");
      setPendingListItems([""]);
      setVisibleInputPath(null);
    }
    onClose();
  };

  const handleDeleteEntry = (index) => {
    const updatedEntries = pendingListItems.filter((val, idx) => idx !== index);
    setPendingListItems(updatedEntries);
  }

  return (
    <div className="border-top mt-4 mb-4 pt-3">
      <div className="d-flex justify-content-between" >
        <h4>Add List</h4>
        <Button icon="fa-times" handler={onClose} />
      </div>
      <div className="row">
        <div className="col-md-4 text-right"><label>Name</label></div>
        <div className="col-md-8 form-group">
          <input
            className="form-control"
            placeholder="List key"
            value={pendingListKey}
            onChange={(e) => setPendingListKey(e.target.value)}
          />
        </div>
      </div>
      <div className="row">
        <div className="col-md-4 text-right"><label>Items</label></div>
        <div className="col-md-8 form-group">
          {pendingListItems.map((item, idx) => (
            <div className="d-flex p-0 gap-2 mb-2" key={`item-${idx}`}>
              <input
                key={idx}
                className="form-control mb-1"
                placeholder={`Item ${idx + 1}`}
                value={item}
                onChange={(e) => {
                  const updated = [...pendingListItems];
                  updated[idx] = e.target.value;
                  setPendingListItems(updated);
                }}
              />
              {(idx + 1) === pendingListItems.length ? <Button
                className="btn-sm btn-default"
                icon="fa-plus"
                title={t("Add Item")}
                handler={() => setPendingListItems([...pendingListItems, ""])}
              /> : <Button className="btn-default btn-sm" icon="fa-minus" title={t("Delete Item")} handler={() => handleDeleteEntry(idx)} />
              }
            </div>
          ))}
          {/* <Button
            className="btn-sm btn-default mt-1"
            icon="fa-plus"
            text={t("Add Item")}
            handler={() => setPendingListItems([...pendingListItems, ""])}
          /> */}
        </div>
      </div>
      <div className="row">
        <div className="col-md-4"></div>
        <div className="col-md-8 form-group">
          <Button
            className="btn btn-sm btn-primary"
            text={t("Add List")}
            handler={handleAddList}
          />
        </div>
      </div>
    </div >
  );
};

export default ListEditor;
