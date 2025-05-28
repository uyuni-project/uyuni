import React, { useCallback, useEffect, useState } from "react";

import { useFormikContext, useField, FieldProps } from "formik";
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
import { PlainObjectEditor } from "./variables/AnsibleTreeEditor";
import styles from "./Ansible.module.scss";


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

  const YamlPreview = ({ values }: { values: Record<string, any> }) => {
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
    (path) => {
      if (varType === "String" && visibleInputPath === path) {
        return <Field name={path} as={StringEditor} onClose={() => setVarType(null)} className="row p-0"/>;
      }
      if (varType === "List" && visibleInputPath === path) {
        return <Field name={path} as={ListEditor} onClose={() => setVarType(null)} className="row p-0"/>;
      }
      if (varType === "Dictionary" && visibleInputPath === path) {
        return <Field name={path} as={DictionaryEditor} onClose={() => setVarType(null)} className="row p-0"/>;
      }
      if (varType === "Boolean" && visibleInputPath === path) {
        return <Field name={path} as={BooleanEditor} onClose={() => setVarType(null)} className="row p-0"/>;
      }
    },
    [varType, visibleInputPath]
  );

  const RenderVariableField = ({ field, form }: FieldProps<any>) => {
    const { name, value } = field;

    const removeItem = () => {
      form.setFieldValue(name, undefined);
    };
  
    if (typeof value === "string" || typeof value === "number") {
      return (
        <div className="row w-100">
          <div className="col-md-4"></div>
          <div className="col-md-8">
           <Field
              name={name}
            />
          </div>
        </div>
      );
    } else if (Array.isArray(value)) {
      return (
        <div className="row mt-2 w-100">
          <div className="col-md-4"></div>
          <div className="col-md-8">
            <MultiField name={name} defaultNewItemValue="" />
          </div>
        </div>
      );
    } else if (isDictionary(value)) {
        return (
          <div className="d-block w-100">
            {Object.entries(value).map(([k]) => (
              <div key={k} className="row mt-2">
                <div className="col-md-4 control-label">
                  <label>{k}</label>
                </div>
                <div className="col-md-8">
                  <Field
                    name={`${name}.${k}`}
                    children={
                      <Button
                        className="btn-default btn-sm"
                        handler={() => removeItem(`${name}.${k}`)}
                        title={t("Remove item")}
                        icon="fa-minus"
                      />
                    }
                  />
                </div>
              </div>
            ))}
            <Field name={name} className="m-0" as={DictionaryEditor} edit={true} />
          </div>
        );
      } else if (typeof value === "object" && value !== null) {
        return (
          <div className="row pb-3">
            <div className="col-md-4">
              Click the Add Variable button and select a variable type from the list to add new variable to{" "}
              <strong>{name}</strong> object.
            </div>
            <div className="col-md-8">
              <DropdownButton
                text={t("Add Variable")}
                icon="fa-plus"
                title={t("Add a Variable")}
                className="btn-default"
                items={variablesList.map((varType) => (
                  // eslint-disable-next-line jsx-a11y/anchor-is-valid
                  <a key={varType} data-senna-off href="#" onClick={() => handleVariable(name, varType)}>
                    {varType.toLocaleLowerCase()}
                  </a>
                ))}
              />
            </div>
            {renderVariableDiv(name)}
          </div>
        );
      } else if (typeof value == "boolean") {
        return (
          <div className="row w-100">
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
      <div className={styles.variableContent}>
        <Form initialValues={data} onSubmit={() => {}} enableReinitialize className="d-flex w-100">
          {({ values }) => (
            <>
              <div className={`${styles.yamlEditor} col-md-7`}>
                {levelOneTitles(values).map((path) => (
                  <Panel
                    key={generateId(path)}
                    headingLevel="h5"
                    collapseId={generateId(path)}
                    title={path.split(".").join(" > ")}
                    className="panel-trasnparent"
                    collapsClose={expandAllClicked ? !expandAllState : false}
                  >
                    <Field name={path} component={RenderVariableField} />
                    {nestedLevelTitles(path, values).map((p) => (
                      <Panel
                        key={generateId(p)}
                        headingLevel="h5"
                        collapseId={generateId(p)}
                        title={p.split(".").join(" > ")}
                        className="panel-trasnparent"
                        collapsClose={expandAllClicked ? !expandAllState : true}
                      >
                        <Field name={p} component={RenderVariableField} />
                      </Panel>
                    ))}
                  </Panel>
                ))}
                <div>
                  <MessagesContainer containerId="extra-var" />
                  <ExtraVariabl setExtraVars={onExtraVarChange} />
                </div>
              </div>
              <div className={`${styles.yamlPreview} col-md-4`}>
                <YamlPreview values={values}   />
              </div>
            </>
          )}
        </Form>
      </div>
    </>
  );
};

export default AnsibleVarYamlEditor;
