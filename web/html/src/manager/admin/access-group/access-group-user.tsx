import * as React from "react";
import { useEffect, useState } from "react";
import { Button } from "components/buttons";
import { DEPRECATED_Select, Form } from "components/input";
import { Column } from "components/table/Column";
import { Table } from "components/table/Table";

import { Utils } from "utils/functions";
import Network from "utils/network";

type Props = {
  state: any;
  onChange: Function;
  errors: any;
};

type User = {
  id: number;
  login: string;
  email: string;
  name: string;
  orgName: string;
}

const AccessGroupUsers = (props: Props) => {
  // List data
  const [listData, setListData] = useState<{items: User[]}>({items: []});
  const [search, setSearch] = useState({
    username: "",
  });

  // Table data
  const [selectedUsers, setSelectedUsers] = useState<User[]>([]);

  useEffect(() => {
    getUserList();
  }, []);

  const getUserList = () => {
    const endpoint = "/rhn/manager/api/admin/access-group/users";
    return Network.get(endpoint)
      .then((users) => {
        setListData((prevData) => ({...prevData, items: users}));
      })
      // TODO: Handle errors
      .catch(props.errors);
  }

  const updateUserList = (search) => {
    const selectedUser = listData.items.find((user) => user.login === search.username);
    if (selectedUser) {
      // Prevent duplicate users
      setListData((prevData) => ({
        ...prevData,
        items: listData.items.filter((name) => name.login !== search.username), // Update the items list
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
    const removeUser = selectedUsers.find((user) => user.login === username);
    if (removeUser) {
      setListData((prevData) => ({
        ...prevData,
        items: [removeUser, ...prevData.items], // Update the items list
      }));
      setSelectedUsers(
        (prevUsers) => prevUsers.filter((user) => user.login !== username) //Remove the user form the list
      );
      props.onChange(removeUser, "remove");
      setSearch({ username: "" });
    }
  };

  return (
    <div>
      <div className="d-flex">
        <div className="me-5">
          <strong className="me-1">Name:</strong>
          {props.state.detailsproperties.name}
        </div>
        <div>
          <strong className="me-1">Description:</strong>
          {props.state.detailsproperties.description}
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
          options={listData.items.map((user) => `${user.login}`)}
          onChange={() => updateUserList(search)}
        />
      </Form>
      <Table
        data={selectedUsers}
        identifier={(item) => item.id}
        initialSortColumnKey="login"
        emptyText={t("No Users selected.")}
      >
        <Column
          columnKey="login"
          comparator={Utils.sortByText}
          header={t("Username")}
          cell={(item) => item.login}
        />
        <Column
          columnKey="email"
          comparator={Utils.sortByText}
          header={t("Email")}
          cell={(item) => item.email}
        />
        <Column
          columnKey="name"
          comparator={Utils.sortByText}
          header={t("Real Name")}
          cell={(item) => item.name}
        />
        <Column
          columnKey="orgName"
          comparator={Utils.sortByText}
          header={t("Organization")}
          cell={(item) => item.orgName}
        />

        <Column
          columnKey="action"
          header={t("Actions")}
          cell={(item) => (
            <Button className="btn-default btn-sm" icon="fa-trash" handler={() => deleteUser(item.login)} />
          )}
        />
      </Table>
    </div>
  );
};

export default AccessGroupUsers;
