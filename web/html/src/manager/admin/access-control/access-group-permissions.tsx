import * as React from "react";
import { useCallback, useEffect, useRef, useState } from "react";

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

  const handleChange = (item, type) => {
    const changes = {};

    const collectChanges = (currentItem, forceValue = null) => {
      const isParent = currentItem.children && currentItem.children.length > 0;

      if (isParent) {
        const newValue = forceValue !== null ? forceValue : getCheckState(currentItem, type) !== "checked";
        currentItem.children.forEach((child) => collectChanges(child, newValue));
      } else {
        const existingPermission = props.state.permissions[currentItem.namespace];
        const newPermission = {
          ...(existingPermission || currentItem),
          [type]: forceValue !== null ? forceValue : !(existingPermission && existingPermission[type]),
        };

        if (!newPermission.view && !newPermission.modify) {
          changes[currentItem.namespace] = undefined;
        } else {
          changes[currentItem.namespace] = newPermission;
        }
      }
    };

    collectChanges(item);

    if (Object.keys(changes).length > 0) {
      props.onChange(changes);
    }
  };

  useEffect(() => {
    let endpoint = "/rhn/manager/api/admin/access-group/namespaces";
    const hasCopy = props.state.accessGroups && props.state.accessGroups.length > 0;
    if (hasCopy) {
      endpoint += `?copyFrom=${props.state.accessGroups.join(",")}`;
    }

    Network.get(endpoint)
      .then((response) => {
        const namespacesToSet = response["namespaces"] || [];
        setNamespaces(namespacesToSet);

        if (hasCopy && response["toCopy"]) {
          const itemsToCopy = response["toCopy"];
          if (itemsToCopy.length > 0) {
            const initialChanges = {};
            itemsToCopy.forEach((item) => {
              initialChanges[item.namespace] = {
                ...item,
                view: item.accessMode.includes("R"),
                modify: item.accessMode.includes("W"),
              };
            });
            props.onChange(initialChanges);
          }
        }
      })
      .catch((error) => {
        console.error("Error fetching namespaces:", error);
      });
  }, []);

  const getCheckState = useCallback(
    (item, type) => {
      if (item.children && item.children.length > 0) {
        const childStates = item.children.map((child) => getCheckState(child, type));
        if (childStates.every((s) => s === "checked")) return "checked";
        if (childStates.every((s) => s === "unchecked")) return "unchecked";
        return "partially";
      }
      const permission = props.state.permissions[item.namespace];
      return permission && permission[type] ? "checked" : "unchecked";
    },
    [props.state.permissions]
  );

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
