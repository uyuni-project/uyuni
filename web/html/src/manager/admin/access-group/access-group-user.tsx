import * as React from "react";
import { useEffect, useState } from "react";
import { Button } from "components/buttons";
import { DEPRECATED_Select, Form, Select } from "components/input";
import { Column } from "components/table/Column";
import { Table } from "components/table/Table";
import { Utils } from "utils/functions";
import * as Systems from "components/systems";

const dataTest = {
  "items": [
    { id: 1, username: "jdoe", email: "jdoe@example.com", orgId: "ORG123" },
    { id: 2, username: "asmith", email: "asmith@example.com", orgId: "ORG456" },
    { id: 3, username: "bwilliams", email: "bwilliams@example.com", orgId: "ORG789" },
    { id: 4, username: "cmtiller", email: "cmiller@example.com", orgId: "ORG101" },
    { id: 5, username: "john", email: "john@example.com", orgId: "ORG123" },
    { id: 6, username: "parker", email: "parker@example.com", orgId: "ORG456" },
    { id: 7, username: "williams", email: "williams@example.com", orgId: "ORG789" },
    { id: 8, username: "asmitha", email: "asmitha@example.com", orgId: "ORG101" },
  ],
  "total": "",
  "selectedIds": []
}

type Props = {
  state: any,
  onChange: Function,
  errors: any
};

const AccessGroupUsers = (props: Props) => {
  // List data
  const [listData, setListData] = useState(dataTest);
  const [search, setSearch] = useState({
    username: "",
  });

  // Table data
  const [selectedUsers, setSelectedUsers] = useState<
    { id: number; username: string; email: string; orgId: string }[]
  >([]);

  const updateUserList = (search) => {
    const selectedUser = listData.items.find((user) => user.username === search.username);
    if (selectedUser) {
      // Prevent duplicate users
      setListData((prevData) => ({
        ...prevData,
        items: listData.items.filter(name => name.username !== search.username),  // Update the items list
      }));

      setSelectedUsers((prevUsers) => {
        return [...prevUsers, selectedUser];
      });

      props.onChange(selectedUser, "add");

    } else {
      setSelectedUsers([]);
    }
  };

  const deleteUser = (username) => {
    const removeUser = selectedUsers.find((user) => user.username === username);
    if (removeUser) {
      setListData((prevData) => ({
        ...prevData,
        items: [removeUser, ...prevData.items],  // Update the items list
      }));
      setSelectedUsers((prevUsers) =>
        prevUsers.filter((user) => user.username !== username) //Remove the user form the list
      );
      props.onChange(removeUser, "remove");
      setSearch({ username: "" });
    } else {
      console.log('NO addUser')
    }
  }

  return (
    <div>
      <div className="row">
        <div className="col-md-4 d-flex">
          <strong className="me-3">Name:</strong>
          <div>{props.state.detailsproperties.name}</div>
        </div>
        <div className="col-md-8 d-flex">
          <strong className="me-3">Description:</strong>
          <div>{props.state.detailsproperties.description}</div>
        </div>
      </div>
      <div className="row mt-3">
        <div className="col-md-12"><strong className="me-3">Access Groups:</strong>
          {props.state.accessGroupsModel.accessGroup.map((item, key) => (
            <span className="label label-default me-3" key={key}>{item}</span>
          ))}
        </div>
      </div>
      <div className="row mt-3">
        <div className="col-md-12"><strong className="me-3">Namespace & Permissions:</strong>
          {props.state.accessGroupsModel.accessGroup.map((item, key) => (
            <span className="label label-default me-3" key={key}>{item}</span>
          ))}
        </div>
      </div>
      <hr></hr>
      <Form
        model={search}
        onChange={(newModel) => {
          setSearch(() => ({
            username: newModel.username,
          }));
        }}
        divClass="col-md-12"
        formDirection="form-horizontal"
      >
        <DEPRECATED_Select
          name="username"
          label={t("Search & Add Users")}
          labelClass="col-md-12 text-start fw-bold fs-4 mb-3"
          divClass="col-md-6"
          options={listData.items.map((user) => `${user.username}`)}
          onChange={() => updateUserList(search)}
        />
      </Form>
      <Table
        data={selectedUsers}
        identifier={(item) => item.id}
        initialSortColumnKey="server_name"
        emptyText={t("No Users.")}
      >
        <Column
          columnKey="server_name"
          comparator={Utils.sortByText}
          header={t("Name")}
          cell={(item) => item.username}
        />
        <Column
          columnKey="status_type"
          comparator={Utils.sortByText}
          header={t("Email ID")}
          cell={(item) => item.email}
        />
        <Column
          columnKey="totalErrataCount"
          comparator={Utils.sortByText}
          header={t("orgId")}
          cell={(item) => item.orgId}
        />

        <Column
          columnKey="outdated_packages"
          comparator={Utils.sortByText}
          header={t("Actions")}
          cell={(item) => <Button className="btn-default btn-sm" icon="fa-trash" handler={() => deleteUser(item.username)} />}
        />
      </Table>
    </div>
  );
};

export default AccessGroupUsers;
