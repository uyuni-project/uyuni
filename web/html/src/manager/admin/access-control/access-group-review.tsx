import * as React from "react";
import { useEffect, useState, useCallback } from "react";

import { Button } from "components/buttons";
import { Form } from "components/formik";
import { Field } from "components/formik/field";
import { Column } from "components/table/Column";
import { Table } from "components/table/Table";

import { Utils } from "utils/functions";
import Network from "utils/network";

type Props = {
  state: any;
  onChange: Function;
  errors: any;
};

const AccessGroupReview = (props: Props) => {
  // List data
  const [listData, setListData] = useState<{ items: User[] }>({ items: [] });
  const [namespaces, setNamespaces] = useState([]);

  console.log("namespaces", namespaces);
  
  const isItemDisabled = useCallback((item, type) => {
      const requiredAccessMode = type === "view" ? "R" : "W";
  
      if (!item.children || item.children.length === 0) {
        return !item.accessMode.includes(requiredAccessMode);
      }
  
      return item.children.every((child) => isItemDisabled(child, type));
    }, []);
   const loadNamespaces = () => {
    let endpoint = "/rhn/manager/api/admin/access-control/access-group/list_namespaces";

    const hasCopy = props.state.accessGroups && props.state.accessGroups.length > 0;
    if (hasCopy) {
      endpoint += `?copyFrom=${props.state.accessGroups.join(",")}`;
    }

    Network.get(endpoint).then((response) => {
      const namespacesToSet = response["namespaces"] || [];
      setNamespaces(namespacesToSet);
    });
  };

   useEffect(() => {
    loadNamespaces();
  }, []);

  const getSelectedNamespace = (items, selectedModes = ["view", "modify"]) => {
  return items
    .map((item) => {
      const children = item.children
        ? getSelectedNamespace(item.children, selectedModes)
        : [];

      const itemPermissions = props.state.permissions[item.namespace];
      const isSelected = selectedModes.some((mode) => itemPermissions?.[mode]);
      const hasSelectedChildren = children.length > 0;

      if (isSelected || hasSelectedChildren) {
        return { ...item, children };
      }
      return null;
    })
    .filter(Boolean);
};

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

 const selectedPermissionsTree = getSelectedNamespace(namespaces);

  // DEBUG
  console.log("selectedPermissionsTree", selectedPermissionsTree);
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
          <hr></hr>{" "}
        </>
      ) : null }

        <Table
        data={selectedPermissionsTree}
        identifier={(item) => item.namespace}
        expandable
        emptyText={t("No permissions selected.")}
      >
        <Column
          columnKey="name"
          header={t("Name")}
          cell={(row) => row.name}
        />
        <Column
          columnKey="description"
          header={t("Description")}
          cell={(row) => row.description}
        />
        <Column
          columnKey="view"
          header={t("View")}
          cell={(item) => {
            const state = getCheckState(item, "view");
            return (
              <>{isItemDisabled(item, "view") ? <span>-</span> : <span>check</span>}</>
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
                     <>{isItemDisabled(item, "modify") ? <span>-</span> : <span>check</span>}</>
                    );
                  }}
                  width="10%"
                />
      </Table>

    </div>
  )
};

export default AccessGroupReview;
