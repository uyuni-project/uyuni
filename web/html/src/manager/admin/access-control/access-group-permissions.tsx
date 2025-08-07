import * as React from "react";
import { useEffect, useRef, useState } from "react";

import { AccessGroupState } from "manager/admin/access-control/access-group";

import { Button } from "components/buttons";
import { Column } from "components/table/Column";
import { Table } from "components/table/Table";

import Network from "utils/network";

type Props = {
  state: AccessGroupState;
  onChange: Function;
  errors: any;
};

const AccessGroupPermissions = (props: Props) => {
  const [namespaces, setNamespaces] = useState([]);
  const checkboxRefs = useRef({});

  const handleChange = (item, type, forceValue = null) => {
    if (item.children.length > 0) {
      const newValue = forceValue !== null ? forceValue : getCheckState(item, type) !== "checked";
      item.children.forEach((child) => {
        if (child[type] !== newValue) {
          handleChange(child, type, newValue);
        }
      });
    } else {
      props.onChange(item, type);
    }
  };

  useEffect(() => {
    Network.get("/rhn/manager/api/admin/access-group/namespaces")
      .then((response) => {
        setNamespaces(response);
      })
      .catch((error) => {
        // TODO: Handle errors properly
        console.error("Error fetching namespaces:", error);
      });
  }, []);

  const getCheckState = (item, type) => {
    if (item.children.length > 0) {
      const childStates = item.children.map((child) => getCheckState(child, type));
      if (childStates.every((s) => s === "checked")) return "checked";
      if (childStates.every((s) => s === "unchecked")) return "unchecked";
      return "partially";
    }
    const permission = props.state.permissions.find((p) => p.namespace === item.namespace);
    const value = permission ? permission[type] : item[type];
    return value ? "checked" : "unchecked";
  };

  useEffect(() => {
    namespaces.forEach((item) => {
      const updateRefsRecursively = (node) => {
        const state = getCheckState(node, "modify");
        const ref = checkboxRefs.current[node.namespace];
        if (ref) {
          ref.indeterminate = state === "partially";
        }
        if (node.children) {
          node.children.forEach(updateRefsRecursively);
        }
      };
      updateRefsRecursively(item);
    });
  }, [namespaces, props.state.permissions]);

  return (
    <div>
      {!props.state.id ? (
        <>
          <div className="d-flex">
            <div className="me-5">
              <strong className="me-1">Name:</strong>
              {props.state.name}
            </div>
            <div className="me-5">
              <strong className="me-1">Description:</strong>
              {props.state.description}
            </div>
            <div>
              <strong className="me-1">Organization:</strong>
              {props.state.orgName}
            </div>
          </div>
          <hr></hr>
        </>
      ) : null}
      <p>{t("Review and modify the permissions for this custom group as needed.")}</p>
      <div className="d-block mb-3">
        <Button className="btn-primary pull-right" text="Add Permissions" handler={() => { }} />
      </div>
      <Table data={namespaces} identifier={(item) => `${item.namespace}-${item.isAPI ? "api" : "ui"}`} expandable>
        <Column
          columnKey="name"
          header={t("Name")}
          cell={(row, criteria, nestingLevel) => {
            if (nestingLevel) {
              return row.name;
            }
            return <b>{row.name}</b>;
          }}
          width="30%"
        />
        <Column
          columnKey="description"
          header={t("Description")}
          cell={(row) => {
            return row.description;
          }}
          width="50%"
        />
        <Column
          columnKey="view"
          header={t("View")}
          cell={(item) => {
            const state = getCheckState(item, "view");
            return (
              <input
                key={item.namespace}
                name="view"
                type="checkbox"
                checked={state === "checked"}
                ref={(el) => {
                  if (el) el.indeterminate = state === "partially";
                }}
                disabled={item.children.length === 0 && !item.accessMode.includes("R")}
                onChange={() => handleChange(item, "view")}
              />
            );
          }}
          width="10%"
        />
        <Column
          columnKey="modify"
          header={t("Modify")}
          cell={(item) => {
            const state = getCheckState(item, "modify");
            return (
              <input
                key={item.namespace}
                name="modify"
                type="checkbox"
                checked={state === "checked"}
                ref={(el) => {
                  if (el) {
                    checkboxRefs.current[item.namespace] = el;
                    el.indeterminate = state === "partially";
                  }
                }}
                disabled={item.children.length === 0 && !item.accessMode.includes("W")}
                onChange={() => handleChange(item, "modify")}
              />
            );
          }}
          width="10%"
        />
      </Table>
    </div>
  );
};

export default AccessGroupPermissions;
