import React, { useState } from "react";
import get from "lodash/get";
import set from "lodash/set";
import yaml from "js-yaml";
import { Panel } from "components/panels/Panel";
import { DropdownButton } from "components/buttons";
import { Field } from "components/formik/field";
import { Radio } from "components/input/radio/Radio";

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

// titles for collapse
const levelOneTitles = (obj, prefix = "") => {
  let paths: string[] = [];

  for (const key in obj) {
    const path = prefix ? `${prefix}.${key}` : key;

    paths.push(path);
  }
  return paths;
};

type Props = {
  data: Record<string, any>;
};

const AnsibleVarYamlEditor = (props: Props) => {
  const [data, setData] = useState(props.data);
  const [newVarInputs, setNewVarInputs] = useState({ radio: "one", });


  const generateId = (path) => {
    return `id_${path.split(".").join("_")}`;
  };

  function nestedLevelTitles(prefix) {
    const obj = get(data, prefix);
    let paths: string[] = [];

    // don't render seconnd level for array or Dictionary
    if (!obj || typeof obj !== 'object' || Array.isArray(obj) || isDictionary(obj)) return [];

    for (const key in obj) {
      const path = prefix ? `${prefix}.${key}` : key;
      const val = obj[key];

      paths.push(path);

      if (typeof val === "object" && val !== null && !Array.isArray(val) && !isDictionary(val)) {
        paths = paths.concat(nestedLevelTitles(path));
      }
    }
    // console.log('paths02', paths)
    return paths;
  }

  const handleChange = (path, value) => {
    const newData = JSON.parse(JSON.stringify(data));
    set(newData, path.replace(/\.(\d+)/g, "[$1]"), value);
    setData(newData);
  };

  const handleAddVariable = (path) => {
    console.log("Create new var at : ", path);
  };

  const renderEditor = (path) => {
    const value = get(data, path);

    if (typeof value === "string" || typeof value === "number") {
      return (
        <div className="row">
          <div className="col-md-4"></div>
          <div className="col-md-8">
            <input
              className="form-control"
              value={value}
              type="text"
              onChange={(e) => handleChange(
                path,
                e.target.value
              )}
            />
          </div>
        </div>
      );
    }

    if (Array.isArray(value)) {

      return value.map((item, index) => (
        // console.log('typeof value', typeof item);
        <div key={index} className="row mt-2" >
          <div className="col-md-4"></div>
          <div className="col-md-8">
            <input
              className="form-control"
              type="text"
              value={item}
              onChange={(e) => {
                const updated = [...value];
                updated[index] = e.target.value;
                handleChange(path, updated);
              }}
            />
          </div>
        </div >
      ));
    }

    // Dictionary variable — render each key and value - TODO
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
                  <input
                    className="form-control"
                    type="text"
                    value={v}
                    onChange={(e) =>
                      handleChange(
                        childPath,
                        e.target.value
                      )
                    }
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
                <a data-senna-off href="#" onClick={() => handleAddVariable(path)}>
                  {name.toLocaleLowerCase()}
                </a>
              ))}
            />
          </div>
        </div >
      );
    }

    if (typeof value == "boolean") {
      // variable is a boolean
      console.log("YESSSS", value)
      return (
        <div className="row">
          <div className="col-md-4"></div>
          <div className="col-md-8">
            <Radio
              name="beginner"
              inline={true}
              label={t("Level")}
              required
              labelClass="col-md-3"
              divClass="col-md-6"
              items={[
                { label: t("Beginner"), value: "beginner" },
                { label: t("Normal"), value: "normal" },
                { label: t("Expert"), value: "expert" },
              ]}
            />
            TESt{value}
          </div>
        </div>
      )
    }
    return null;
  };

  return (
    <>
      <div className="row">
        <p className="col-md-7">Set the value of existing variables to override previously defined variables.</p>
        <h3 className="col-md-4">Yaml Preview</h3>
      </div>
      <div className="variable-content">
        <div className="yaml-editor">
          {
            levelOneTitles(data).map((path) => (
              <Panel
                headingLevel="h5"
                collapseId={generateId(path)}
                title={path.split(".").join(" > ")}
                className="panel-trasnparent"
              > {renderEditor(path)}
                {nestedLevelTitles(path).map((p) => (
                  <Panel
                    headingLevel="h5"
                    collapseId={generateId(p)}
                    title={p.split(".").join(" > ")}
                    className="panel-trasnparent"
                    collapsClose={true}
                  >{renderEditor(p)}
                  </Panel>
                ))
                }
              </Panel>
            ))
          }

        </div>
        <div className="yaml-preview">
          <pre>
            {yaml.dump({ vars: data })}
          </pre>
        </div>
      </div>
    </>
  );
};

export default AnsibleVarYamlEditor;
