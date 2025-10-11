import * as React from "react";
import { useEffect, useState } from "react";

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

type User = {
  id: number;
  login: string;
  email: string;
  name: string;
  orgName: string;
};

const AccessGroupUsers = (props: Props) => {
  // List data
  const [listData, setListData] = useState<{ items: User[] }>({ items: [] });

  // Table data
  const [selectedUsers, setSelectedUsers] = useState<User[]>(props.state.users);

  useEffect(() => {
    getUserList();
  }, []);

  const getUserList = () => {
    const endpoint = "/rhn/manager/api/admin/access-group/users/" + props.state.orgId;
    return (
      Network.get(endpoint)
        .then((users) => {
          setListData((prevData) => ({
            ...prevData,
            items: users.filter((u) => !selectedUsers.find(({ id }) => u.id === id)),
          }));
        })
        // TODO: Handle errors
        .catch(props.errors)
    );
  };

  const updateUserList = (item) => {
    const selectedUser = listData.items.find((user) => user.login === item.login);
    if (selectedUser) {
      // Prevent duplicate users
      setListData((prevData) => ({
        ...prevData,
        items: listData.items.filter((name) => name.login !== item.login), // Update the items list
      }));

      setSelectedUsers((prevUsers) => {
        return [...prevUsers, selectedUser];
      });

      props.onChange(selectedUser, "add");
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
    }
  };

  return (
    <div>
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
      <Form
        initialValues={props.state.users}
        // TODO: Use onChange instead of validate to update access group details
        // onChange={updateUserList}
        validate={updateUserList}
        onSubmit={() => {}}
        divClass="col-md-12"
        formDirection="form-horizontal"
      >
        <Field
          name="login"
          label={t("Search & Add Users")}
          labelClass="col-md-12 text-start fw-bold fs-4 mb-3"
          divClass="col-md-6"
          options={listData.items.map((user) => {
            return { label: user.login, value: user.login };
          })}
          as={Field.Select}
          // TODO: Clear selected value from the picker once it's selected
          value={null}
          onChange={() => null}
        />
      </Form>
      <Table
        data={selectedUsers}
        identifier={(item) => item.id}
        initialSortColumnKey="login"
        emptyText={t("No Users selected.")}
      >
        <Column columnKey="login" comparator={Utils.sortByText} header={t("Username")} cell={(item) => item.login} />
        <Column columnKey="email" comparator={Utils.sortByText} header={t("Email")} cell={(item) => item.email} />
        <Column columnKey="name" comparator={Utils.sortByText} header={t("Real Name")} cell={(item) => item.name} />
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
