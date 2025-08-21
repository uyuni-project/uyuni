import * as React from "react";
import { useCallback, useEffect, useRef, useState } from "react";

import { AccessGroupState } from "manager/admin/access-control/access-group";

import { Column } from "components/table/Column";
import { Table } from "components/table/Table";
import { MessagesContainer, showErrorToastr } from "components/toastr";

import Network from "utils/network";

type Props = {
  state: AccessGroupState;
  onChange: Function;
  errors: any;
};

const AccessGroupPermissions = (props: Props) => {
  const [namespaces, setNamespaces] = useState([]);
  const checkboxRefs = useRef({});

  const isItemDisabled = useCallback((item, type) => {
    const requiredAccessMode = type === "view" ? "R" : "W";

    if (!item.children || item.children.length === 0) {
      return !item.accessMode.includes(requiredAccessMode);
    }

    return item.children.every((child) => isItemDisabled(child, type));
  }, []);

  const getCheckState = useCallback(
    (item, type) => {
      if (item.children && item.children.length > 0) {
        const enabledChildren = item.children.filter((child) => !isItemDisabled(child, type));

        if (enabledChildren.length === 0) {
          return "unchecked";
        }

        const childStates = enabledChildren.map((child) => getCheckState(child, type));
        if (childStates.every((s) => s === "checked")) return "checked";
        if (childStates.every((s) => s === "unchecked")) return "unchecked";
        return "partially";
      }
      const permission = props.state.permissions[item.namespace];
      return permission && permission[type] ? "checked" : "unchecked";
    },
    [props.state.permissions, isItemDisabled]
  );

  const handleChange = (item, type) => {
    const changes = {};

    const collectChanges = (currentItem, forceValue: boolean | null = null) => {
      if (isItemDisabled(currentItem, type)) {
        return;
      }

      const isParent = currentItem.children && currentItem.children.length > 0;

      if (isParent) {
        const newValue = forceValue !== null ? forceValue : getCheckState(currentItem, type) !== "checked";
        currentItem.children.forEach((child) => {
          if (!isItemDisabled(child, type)) {
            collectChanges(child, newValue);
          }
        });
      } else {
        const existingPermission = props.state.permissions[currentItem.namespace];
        const newPermission = {
          ...existingPermission,
          ...currentItem,
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
    let endpoint = "/rhn/manager/api/admin/access-control/access-group/list_namespaces";
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
      .catch(() => {
        showErrorToastr(t("An unexpected error occurred while fetching namespaces."));
      });
  }, []);

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
  }, [namespaces, props.state.permissions, getCheckState]);

  return (
    <div>
      <MessagesContainer />
      {!props.state.id ? (
        <>
          <div className="d-flex">
            <div className="me-5">
              <strong className="me-1">{t("Name:")}</strong>
              {props.state.name}
            </div>
            <div className="me-5">
              <strong className="me-1">{t("Description:")}</strong>
              {props.state.description}
            </div>
            <div>
              <strong className="me-1">{t("Organization:")}</strong>
              {props.state.orgName}
            </div>
          </div>
          <hr></hr>
        </>
      ) : null}
      <p>{t("Review and modify the permissions for this custom group as needed.")}</p>
      <Table data={namespaces} identifier={(item) => `${item.namespace}-${item.isAPI ? "api" : "ui"}`} expandable>
        <Column
          columnKey="name"
          header={t("Name")}
          cell={(row, criteria, nestingLevel) => {
            if (nestingLevel) {
              return row.name;
            }
            return (
              <b>
                {row.name} {row.isAPI ? "[API]" : ""}
              </b>
            );
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
                disabled={isItemDisabled(item, "view")}
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
                disabled={isItemDisabled(item, "modify")}
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
