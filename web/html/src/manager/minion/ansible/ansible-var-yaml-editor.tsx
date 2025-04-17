import React, { useState, useEffect, useCallback } from "react";
import get from "lodash/get";
import unset from "lodash/unset";
import yaml from "js-yaml";
import { Panel } from "components/panels/Panel";
import { DropdownButton, Button } from "components/buttons";
import { useFormikContext } from "formik";
import { Field, MultiField } from "components/formik/field";
import { Form, OnSubmit } from "components/formik/Form";
import { StringVariableEditor } from "./string-variable-editor"

const isDictionary = (obj) => {
  if (typeof obj !== "object" || obj === null || Array.isArray(obj)) return false;
  return Object.values(obj).every(
    (val) =>
      typeof val === "string" ||
      typeof val === "number" ||
      typeof val === "boolean" ||
      val === null
  );
};

const variablesList = ["List", "Dictionary", "String", "Boolean"];

type Props = {
  data: Record<string, any>;
};



const AnsibleVarYamlEditor = (props: Props) => {
  const [visibleInputPath, setVisibleInputPath] = useState(null);
  const [varType, setVarType] = useState(null);
  const [pendingListKey, setPendingListKey] = useState(null);
  const [pendingListItems, setPendingListItems] = useState([""]);

  const generateId = (path) => `id_${path.split(".").join("_")}`;

  // titles for collapse
  const levelOneTitles = (obj) => Object.keys(obj);


  const nestedLevelTitles = (prefix, value) => {
    const current = get(value, prefix);
    let paths = [];

    // don't render seconnd level for array or Dictionary
    if (!current || typeof current !== 'object' || Array.isArray(current) || isDictionary(current)) return [];

    for (const key in current) {
      const path = `${prefix}.${key}`;
      const val = current[key];
      paths.push(path);

      if (typeof val === "object" && val !== null && !Array.isArray(val) && !isDictionary(val)) {
        paths = paths.concat(nestedLevelTitles(path, value));
      }
    }
    // console.log(paths)
    return paths;
  };

  const KeyValueEditor = ({ path, value, onDelete, setFieldValue }) => {
    console.log("KeyValueEditor");
    const [newKey, setNewKey] = useState("");
    const [newValue, setNewValue] = useState("");
    const [showInputs, setShowInputs] = useState(false);
    const [showFocus, setShowFocus] = useState(false);

    const handleAdd = () => {
      if (!newKey.trim()) return;

      let val = newValue;
      if (newValue === "true") val = true;
      else if (newValue === "false") val = false;
      setFieldValue(`${path}.${newKey}`, val);
      setNewKey("");
      setNewValue("");
      setShowInputs(false);
      setShowFocus(true)
    };

    const handleValKeyDown = (e) => {
      if (e.key === "Enter") {
        e.preventDefault();
        handleAdd();
      }
    };

    return (
      <>
        {Object.entries(value).map(([k]) => (
          <div key={k} className="row mt-2">
            <div className="col-md-4 text-right"><label>{k}</label></div>
            <div className="col-md-8">
              <Field autoFocus={showFocus} name={`${path}.${k}`} children={<Button
                className="btn-default btn-sm"
                handler={() => onDelete(`${path}.${k}`)}
                title={t("Remove item")}
                icon="fa-minus"
              />} />
            </div>
          </div>
        ))}
        <div>
          {showInputs && (
            <div className="row mt-2">
              <div className="col-md-4"></div>
              <div className="col-md-8">
                <input
                  className="form-control"
                  placeholder="New key"
                  value={newKey}
                  onChange={(e) => setNewKey(e.target.value)}
                  onBlur={handleAdd}
                  onKeyDown={handleValKeyDown}
                />
              </div>
            </div>)}
          <div className="row mt-2">
            <div className="col-md-4"></div>
            <div className="col-md-8">
              {!showInputs && (
                <Button
                  className=" btn-default btn-sm mt-2"
                  icon="fa-plus"
                  text={t("Add new key")}
                  handler={() => setShowInputs(true)}
                />
              )}
            </div>
          </div>
        </div>
      </>
    )
  };

  const YamlPreview = () => {
    const { values } = useFormikContext();
    const [yamlOutput, setYamlOutput] = useState("");

    useEffect(() => {
      setYamlOutput(yaml.dump({ vars: values }, { quotingType: '"', forceQuotes: true }));
    }, [values]);

    return <pre>{yamlOutput}</pre>;
  };

  const handleVariable = useCallback((path, name, setFieldValue) => {
    setVisibleInputPath(path);
    setVarType(name);

  }, []);

  const renderVariableDiv = useCallback((path, setFieldValue) => {
    const [newKeyString, setNewKeyString] = useState({});
    const [newValueString, setNewValueString] = useState({});

    if (varType === "String" && visibleInputPath === path) {
      return (
        <div className="row ">
          <div>String</div>
          <div className="form-group ">
            <input
              className="form-control"
              placeholder="New variable key"
              value={newKeyString[path] || ""}
              onChange={(e) => setNewKeyString({ [path]: e.target.value })}
            />
          </div>
          <div className="form-group ">
            <input
              className="form-control mt-2"
              placeholder="New variable value"
              value={newValueString[path] || ""}
              onChange={(e) => setNewValueString({ [path]: e.target.value })}
            />
          </div>
          <div className="form-group ">
            <Button
              text={t("Add")}
              icon="fa-plus"
              className="btn btn-sm btn-primary mt-2"
              handler={() => {
                const key = newKeyString[path]?.trim();
                const val = newValueString[path]?.trim();
                if (key) {
                  setFieldValue(`${path}.${key}`, val || "");
                  setNewKeyString({ [path]: "" });
                  setNewValueString({ [path]: "" });
                }
              }}
            />
          </div>
        </div >)
    }

    if (varType === "List" && visibleInputPath === path) {
      const handleAddList = () => {
        if (pendingListKey) {
          setFieldValue(`${path}.${pendingListKey}`, pendingListItems);
          setPendingListKey("");
          setPendingListItems([""]);
          setVarType(null);
          setVisibleInputPath(null);
        }
      };

      return (
        <div className="row">
          <div className="form-group col-md-4">
            <input
              className="form-control"
              placeholder="List key"
              value={pendingListKey}
              onChange={(e) => setPendingListKey(e.target.value)}
            />
          </div>
          <div className="form-group col-md-6">
            {pendingListItems.map((item, idx) => (
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
            ))}
            <Button
              className="btn-sm btn-default mt-1"
              icon="fa-plus"
              text={t("Add Item")}
              handler={() => setPendingListItems([...pendingListItems, ""])}
            />
          </div>
          <div className="form-group col-md-2">
            <Button
              className="btn btn-sm btn-primary"
              text={t("Add List")}
              handler={handleAddList}
            />
          </div>
        </div>
      );
    }


    if (varType === "Dictionary" && visibleInputPath === path) {
      return (
        <div className="row align-items-center">
          <h3>Dictionary</h3>
        </div>)
    }
    if (varType === "Boolean" && visibleInputPath === path) {
      return (
        <div className="row align-items-center">
          <h3>Boolean</h3>
        </div>)
    }
  }, [varType, visibleInputPath, pendingListKey, pendingListItems]);

  const renderEditor = (path, values, setFieldValue) => {
    console.log('renderEditor')
    const value = get(values, path);

    const removeItem = (targetPath) => {
      setFieldValue(targetPath, undefined);
    };

    if (typeof value === "string" || typeof value === "number") {
      return (
        <div className="row">
          <div className="col-md-4"></div>
          <div className="col-md-8">
            <Field
              name={path}
              children={<Button
                className="btn-default btn-sm"
                handler={() => removeItem(path)}
                title={t("Remove item")}
                icon="fa-minus"
              />}
            />
          </div>
        </div>
      );
    }

    if (Array.isArray(value)) {
      return (
        <div className="row mt-2" >
          <div className="col-md-4"></div>
          <div className="col-md-8">
            <MultiField name={path} defaultNewItemValue="" />
          </div>
        </div >
      );
    }

    // Dictionary variable — Add new var - TODO
    if (isDictionary(value)) {
      return (
        <>
          <KeyValueEditor path={path} value={value} onDelete={removeItem} setFieldValue={setFieldValue} />
        </>
      );
    }

    if (typeof value === "object" && value !== null) {
      return (
        <div className="row">
          <div className="col-md-4">Click the Add Variable button and select a
            variable type from the list to add new variable to <strong>{path.split(".").pop()}</strong> object.</div>
          <div className="col-md-8">
            <DropdownButton
              text={t("Add Variable")}
              icon="fa-plus"
              title={t("Add a Variable")}
              className="btn-default"
              items={variablesList.map((name) => (
                <a data-senna-off href="#" onClick={() => handleVariable(path, name)}>
                  {name.toLocaleLowerCase()}
                </a>
              ))}
            />
            <div>{renderVariableDiv(path, setFieldValue)}
            </div>
          </div>
        </div >
      );
    }

    if (typeof value == "boolean") {
      return (
        <div className="row">
          <div className="col-md-4"></div>
          <div className="col-md-8">
            <label className="radio col-md-4">
              <input
                type="radio"
                checked={value === true}
                onChange={() => setFieldValue(path, true)}
              /> {t("True")}
            </label>
            <label className="radio col-md-4">
              <input
                type="radio"
                checked={value === false}
                onChange={() => setFieldValue(path, false)}
              /> {t("False")}
            </label>
          </div>
        </div>
      )
    }
    return null;
  };

  const handleSubmit = (values) => {
    console.log("Submit:", values);
  };

  return (
    <>
      <div className="row">
        <p className="col-md-7">Set the value of existing variables to override previously defined variables.</p>
        <h3 className="col-md-4">Yaml Preview</h3>
      </div>

      <div className="variable-content">
        <Form initialValues={props.data} onSubmit={handleSubmit} enableReinitialize className="d-flex w-100">
          {({ values, setFieldValue }) => (
            <>
              <div className="yaml-editor">
                {levelOneTitles(values).map((path) => (
                  <Panel
                    headingLevel="h5"
                    collapseId={generateId(path)}
                    title={path.split(".").join(" > ")}
                    className="panel-trasnparent"
                  > {renderEditor(path, values, setFieldValue)}
                    {nestedLevelTitles(path, values).map((p) => (
                      <Panel
                        headingLevel="h5"
                        collapseId={generateId(p)}
                        title={p.split(".").join(" > ")}
                        className="panel-trasnparent"
                        collapsClose={true}
                      >
                        {renderEditor(p, values, setFieldValue)}
                      </Panel>
                    ))
                    }
                  </Panel>
                ))}
              </div>
              <div className="yaml-preview">
                <YamlPreview />
              </div>
            </>
          )}
        </Form>
      </div>
    </>
  );
};

export default AnsibleVarYamlEditor;
