import React, { useState } from "react";
import { Button } from "components/buttons";

type Props = {
  path: string;
  setFieldValue: (key: string, value: any) => void;
  onClose?: () => void;
};

const StringEditor = (props: Props) => {
  const {
    path,
    setFieldValue,
    onClose
  } = props;

  const [newKey, setNewKey] = useState("");
  const [newValue, setNewValue] = useState("");

  const handleAdd = () => {
    if (!newKey.trim()) return;

    let val = newValue;
    setFieldValue(`${path}.${newKey}`, val);
    setNewKey("");
    setNewValue("");
  };

  // return (
  //   <>
  //     {showInputs && (
  //       <div className="border-top mt-4 mb-4">
  //         <div className="d-block" >
  //           {showForm && (<h4 className="pull-left">Add String</h4>)}
  //           <Button className="pull-right" icon="fa-times" handler={() => !showForm ? setShowInputs(false) : onClose()} />
  //         </div>
  //         <div className="row">
  //           <div className="col-md-4 text-right">
  //             <label>
  //               {!showForm ? t("Key and value") : t("Name and value")}</label>
  //           </div>
  //           <div className="col-md-8 form-group">
  //             <div className="d-flex p-0 m-0 mb-3" >
  //               <div className="w-50 me-2">
  //                 <input
  //                   className="form-control"
  //                   placeholder="Key"
  //                   value={newKey}
  //                   onChange={(e) => setNewKey(e.target.value)}
  //                 />
  //               </div>
  //               <div className="w-50 me-2">
  //                 <input
  //                   className="form-control"
  //                   placeholder="Value"
  //                   value={newValue}
  //                   onChange={(e) => setNewValue(e.target.value)}
  //                 />
  //               </div>
  //             </div>
  //             <Button
  //               className=" btn-primary btn-sm mt-2"
  //               text={!showForm ? t("Add") : t("Add String")}
  //               handler={handleAdd}
  //             />
  //           </div>
  //         </div>
  //       </div>)}
  //     <div className="row mt-2">
  //       <div className="col-md-4"></div>
  //       <div className="col-md-8">
  //         {(!showInputs && newItembutton) && (
  //           <Button
  //             className=" btn-default btn-sm mt-2"
  //             icon="fa-plus"
  //             text={t("Add Item")}
  //             handler={() => setShowInputs(true)}
  //           />
  //         )}
  //       </div>
  //     </div>
  //   </>
  // )
  return (
    <>
      {
        <div className="border-top mt-4 mb-4 pt-3">
          <div className="d-block" >
            <h4 className="pull-left">Add String</h4>
            <Button className="pull-right" icon="fa-times" handler={() => onClose()} />
          </div>
          <div className="row">
            <div className="col-md-4 text-right">
              <label>{t("Name and value")}</label>
            </div>
            <div className="col-md-8 form-group">
              <div className="d-flex p-0 m-0 mb-3" >
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
              <Button
                className=" btn-primary btn-sm mt-2"
                text={t("Add String")}
                handler={handleAdd}
              />
            </div>
          </div>
        </div>}
    </>)
};

export default StringEditor;
