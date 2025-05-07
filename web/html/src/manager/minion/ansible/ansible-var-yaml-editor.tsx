import React, { useCallback, useEffect, useState } from "react";

import { useFormikContext } from "formik";
import yaml from "js-yaml";
import get from "lodash/get";

import { Button, DropdownButton } from "components/buttons";
import { Field, MultiField } from "components/formik/field";
import { Form } from "components/formik/Form";
import { Panel } from "components/panels/Panel";
import { MessagesContainer } from "components/toastr/toastr";

import BooleanEditor from "./variables/boolean-editor";
import DictionaryEditor from "./variables/dictionary-editor";
import ExtraVariabl from "./variables/extra-var";
import ListEditor from "./variables/list-editor";
import StringEditor from "./variables/string-editor";

const isDictionary = (obj) => {
  if (typeof obj !== "object" || obj === null || Array.isArray(obj)) return false;
  return Object.values(obj).every(
    (val) => typeof val === "string" || typeof val === "number" || typeof val === "boolean" || val === null
  );
};

const variablesList = ["List", "Dictionary", "String", "Boolean"];

type Props = {
  data: Record<string, any>;
  onDataChange: (values: Record<string, any>) => void;
  onExtraVarChange: (extravalues: string) => void;
};

const AnsibleVarYamlEditor = (props: Props) => {
  const { data, onDataChange, onExtraVarChange } = props;
  const [visibleInputPath, setVisibleInputPath] = useState(null);
  const [varType, setVarType] = useState(null);
  const [expandAllClicked, setExpandAllClicked] = useState(false);
  const [expandAllState, setExpandAllState] = useState(false);

  const generateId = (path) => `id_${path.split(".").join("_")}`;

  // Titles for collapse top level
  const levelOneTitles = (obj) => Object.keys(obj);

  // Nested level
  const nestedLevelTitles = (prefix, value) => {
    const current = get(value, prefix) as any;
    let paths: string[] = [];

    // don't render seconnd level for array or Dictionary
    if (!current || typeof current !== "object" || Array.isArray(current) || isDictionary(current)) return [];

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

  const YamlPreview = () => {
    const { values } = useFormikContext<Record<string, any>>();
    const [yamlOutput, setYamlOutput] = useState("");

    useEffect(() => {
      onDataChange?.(values);
      setYamlOutput(yaml.dump({ vars: values }, { quotingType: '"', forceQuotes: true }));
    }, [values]);

    return <pre>{yamlOutput}</pre>;
  };

  const handleVariable = useCallback((path, name) => {
    setVisibleInputPath(path);
    setVarType(name);
  }, []);

  const renderVariableDiv = useCallback(
    (path, setFieldValue) => {
      if (varType === "String" && visibleInputPath === path) {
        return <StringEditor path={path} setFieldValue={setFieldValue} onClose={() => setVarType(null)} />;
      }
      if (varType === "List" && visibleInputPath === path) {
        return <ListEditor path={path} setFieldValue={setFieldValue} onClose={() => setVarType(null)} />;
      }
      if (varType === "Dictionary" && visibleInputPath === path) {
        return <DictionaryEditor path={path} setFieldValue={setFieldValue} onClose={() => setVarType(null)} />;
      }
      if (varType === "Boolean" && visibleInputPath === path) {
        return <BooleanEditor path={path} setFieldValue={setFieldValue} />;
      }
    },
    [varType, visibleInputPath]
  );

  // Retrieve the value at the specified path within the YAML structure
  const renderEditor = (path, values, setFieldValue) => {
    // console.log('renderEditor')
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
              children={
                <Button
                  className="btn-default btn-sm"
                  handler={() => removeItem(path)}
                  title={t("Remove item")}
                  icon="fa-minus"
                />
              }
            />
          </div>
        </div>
      );
    }

    if (Array.isArray(value)) {
      return (
        <div className="row mt-2">
          <div className="col-md-4"></div>
          <div className="col-md-8">
            <MultiField name={path} defaultNewItemValue="" />
          </div>
        </div>
      );
    }

    // Dictionary variable — Add new key and value
    if (isDictionary(value)) {
      return (
        <>
          {Object.entries(value).map(([k]) => (
            <div key={k} className="row mt-2">
              <div className="col-md-4 control-label">
                <label>{k}</label>
              </div>
              <div className="col-md-8">
                <Field
                  name={`${path}.${k}`}
                  children={
                    <Button
                      className="btn-default btn-sm"
                      handler={() => removeItem(`${path}.${k}`)}
                      title={t("Remove item")}
                      icon="fa-minus"
                    />
                  }
                />
              </div>
            </div>
          ))}
          <DictionaryEditor
            path={path}
            setFieldValue={setFieldValue}
            onClose={() => setVarType(null)}
            value={value}
            edit={true}
          />
        </>
      );
    }

    if (typeof value === "object" && value !== null) {
      return (
        <div className="row pb-3">
          <div className="col-md-4">
            Click the Add Variable button and select a variable type from the list to add new variable to{" "}
            <strong>{path.split(".").pop()}</strong> object.
          </div>
          <div className="col-md-8">
            <DropdownButton
              text={t("Add Variable")}
              icon="fa-plus"
              title={t("Add a Variable")}
              className="btn-default"
              items={variablesList.map((name) => (
                // eslint-disable-next-line jsx-a11y/anchor-is-valid
                <a key={name} data-senna-off href="#" onClick={() => handleVariable(path, name)}>
                  {name.toLocaleLowerCase()}
                </a>
              ))}
            />
          </div>
          {renderVariableDiv(path, setFieldValue)}
        </div>
      );
    }

    if (typeof value == "boolean") {
      return (
        <div className="row">
          <div className="col-md-4"></div>
          <div className="col-md-8">
            <label className="radio col-md-4">
              <input type="radio" checked={value === true} onChange={() => setFieldValue(path, true)} /> {t("True")}
            </label>
            <label className="radio col-md-4">
              <input type="radio" checked={value === false} onChange={() => setFieldValue(path, false)} /> {t("False")}
            </label>
          </div>
        </div>
      );
    }
    return null;
  };

  return (
    <>
      <div className="row">
        <div className="col-md-7">
          <div className="d-flex justify-content-between align-items-center mb-3">
            Set the value of existing variables to override previously defined variables.
            <Button
              text={expandAllState ? "Collapse All" : "Expand All"}
              className="btn-default btn-sm"
              handler={() => {
                setExpandAllClicked(true);
                setExpandAllState((prev) => !prev);
              }}
            />
          </div>
        </div>
        <div className="col-md-4 d-flex align-items-center">
          <h4 className="m-0">Yaml Preview</h4>
        </div>
      </div>
      <div className="variable-content border-top">
        <Form initialValues={data} onSubmit={() => {}} enableReinitialize className="d-flex w-100">
          {({ values, setFieldValue }) => (
            <>
              <div className="yaml-editor col-md-7">
                {levelOneTitles(values).map((path) => (
                  <Panel
                    key={generateId(path)}
                    headingLevel="h5"
                    collapseId={generateId(path)}
                    title={path.split(".").join(" > ")}
                    className="panel-trasnparent"
                    collapsClose={expandAllClicked ? !expandAllState : false}
                  >
                    {renderEditor(path, values, setFieldValue)}
                    {nestedLevelTitles(path, values).map((p) => (
                      <Panel
                        key={generateId(p)}
                        headingLevel="h5"
                        collapseId={generateId(p)}
                        title={p.split(".").join(" > ")}
                        className="panel-trasnparent"
                        collapsClose={expandAllClicked ? !expandAllState : true}
                      >
                        {renderEditor(p, values, setFieldValue)}
                      </Panel>
                    ))}
                  </Panel>
                ))}
                <div>
                  <MessagesContainer />
                  <ExtraVariabl setExtraVars={onExtraVarChange} />
                </div>
              </div>
              <div className="yaml-preview col-md-4">
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
