import React, { useState, useEffect } from "react";
import get from "lodash/get";

import yaml from "js-yaml";
import { Panel } from "components/panels/Panel";
import { DropdownButton, Button } from "components/buttons";
import { useFormikContext } from "formik";
import { Field, MultiField } from "components/formik/field";
import { Form, OnSubmit } from "components/formik/Form";


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
  const [newKeyInput, setNewKeyInput] = useState({});
  const [newValueInput, setNewValueInput] = useState({});
  const [visibleInputPath, setVisibleInputPath] = useState(null);
  const [varType, setVarType] = useState(null);

  const generateId = (path) => `id_${path.split(".").join("_")}`;

  // titles for collapse
  const levelOneTitles = (obj) => Object.keys(obj);

  function nestedLevelTitles(prefix, value) {
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
  }

  const YamlPreview = () => {
    const { values } = useFormikContext();
    const [yamlOutput, setYamlOutput] = useState("");

    useEffect(() => {
      setYamlOutput(yaml.dump({ vars: values }, { quotingType: '"', forceQuotes: true }));
    }, [values]);

    return <pre>{yamlOutput}</pre>;
  };

  const handleVariable = (path, name) => {
    setVisibleInputPath(path);
    setVarType(name);
  }

  const renderVariableDiv = (path, setFieldValue) => {
    console.log('renderVariableDiv', path)
    if (varType === "String" && visibleInputPath === path) {
      return (
        <div className="row ">
          <div>String</div>
          <div className="form-group ">
            <input
              className="form-control"
              placeholder="New variable key"
              value={newKeyInput[path] || ""}
              onChange={(e) => setNewKeyInput({ [path]: e.target.value })}
            />
          </div>
          <div className="form-group ">
            <input
              className="form-control mt-2"
              placeholder="New variable value"
              value={newValueInput[path] || ""}
              onChange={(e) => setNewValueInput({ [path]: e.target.value })}
            />
          </div>
          <div className="form-group ">
            <Button
              text="Add"
              icon="fa-plus"
              className="btn btn-sm btn-primary mt-2"
              handler={() => {
                const key = newKeyInput[path]?.trim();
                const val = newValueInput[path]?.trim();
                if (key) {
                  setFieldValue(`${path}.${key}`, val || "");
                  setNewKeyInput({ [path]: "" });
                  setNewValueInput({ [path]: "" });
                }
              }}
            />
          </div>
        </div >)
    }
    if (varType === "List" && visibleInputPath === path) {
      return (
        <div className="row align-items-center">
          <h3>List</h3>
        </div>)
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
  }

  const renderEditor = (path, values, setFieldValue) => {
    console.log('renderEditor')
    const value = get(values, path);

    if (typeof value === "string" || typeof value === "number") {
      console.log('renderEditor :', value)
      return (
        <div className="row">
          <div className="col-md-4"></div>
          <div className="col-md-8">
            <Field
              name={path}
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
          {Object.entries(value).map(([k, v]) => {
            const childPath = `${path}.${k}`;
            return (
              <div key={childPath} className="row mt-2">
                <div className="col-md-4"></div>
                <div className="col-md-8">
                  <label>{k}</label>
                  <Field
                    name={childPath}
                  />
                </div>
              </div>
            );
          })}
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
