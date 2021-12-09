import * as React from "react";
import { useState, useEffect } from "react";
import { Panel } from "components/panels/Panel";
import { Button } from "components/buttons";
import { Table } from "components/table/Table";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { SystemLink } from "components/links";
import { IconTag as Icon } from "components/icontag";
import { Utils } from "utils/functions";
import { withErrorMessages } from "../api/use-clusters-api";

import { MessageType } from "components/messages";
import { ErrorMessagesType, ServerType } from "../api/use-clusters-api";

type Props = {
  title: string;
  selectedServers: Array<ServerType> | null | undefined;
  onNext: (arg0: Array<ServerType>) => void;
  onPrev?: () => void;
  setMessages: (messages: MessageType[]) => void;
  fetchServers: () => Promise<Array<ServerType>>;
  multiple?: boolean;
};

const SystemMessages = (props: { messages: MessageType[] }) => {
  return (
    <ul style={{ listStyle: "none", paddingLeft: "0px", margin: "0px" }}>
      {props.messages.map((msg) => (
        <li>
          <Icon type="system-warn" className="fa-1-5x" />
          {msg.text}
        </li>
      ))}
    </ul>
  );
};

const SelectServer = (props: Props) => {
  const [selections, setSelections] = useState<Set<number>>(
    props.selectedServers ? new Set(props.selectedServers.map((srv) => srv.id)) : new Set()
  );
  const [servers, setServers] = useState<Array<ServerType>>([]);
  const [fetching, setFetching] = useState<boolean>(false);

  useEffect(() => {
    setFetching(true);
    props
      .fetchServers()
      .then((data) => {
        setServers(data);
        setSelections(props.selectedServers ? new Set(props.selectedServers.map((srv) => srv.id)) : new Set());
      })
      .catch((error: ErrorMessagesType) => {
        props.setMessages(error.messages);
      })
      .finally(() => {
        setFetching(false);
      });
  }, []);

  const filterFunc = (row, criteria) => {
    const keysToSearch = ["name"];
    if (criteria) {
      return keysToSearch
        .map((key) => row[key])
        .join()
        .toLowerCase()
        .includes(criteria.toLowerCase());
    }
    return true;
  };

  const selectServers = (selections: Set<number>) => {
    const selectedServers = servers.filter((srv) => selections.has(srv.id));
    props.onNext(selectedServers);
  };

  const onSelectServer = (event, id) => {
    let newSelections;
    if (props.multiple) {
      newSelections = new Set(selections);
      if (event.target.checked) {
        newSelections.add(id);
      } else {
        newSelections.delete(id);
      }
    } else {
      newSelections = new Set([id]);
    }
    setSelections(newSelections);
  };

  return (
    <Panel
      headingLevel="h4"
      title={props.title}
      footer={
        <div className="btn-group">
          {props.onPrev ? (
            <Button
              id="btn-prev"
              text={t("Back")}
              className="btn-default"
              icon="fa-arrow-left"
              handler={() => props.onPrev?.()}
            />
          ) : null}
          <Button
            id="btn-next"
            disabled={selections.size === 0}
            text={t("Next")}
            className="btn-success"
            icon="fa-arrow-right"
            handler={() => selectServers(selections)}
          />
        </div>
      }
    >
      <Table
        data={servers}
        loading={fetching}
        identifier={(row) => row.id}
        initialSortColumnKey="name"
        searchField={<SearchField filter={filterFunc} placeholder={t("Filter by any value")} />}
      >
        <Column
          columnKey="select"
          header={""}
          cell={(row: ServerType) =>
            props.multiple ? (
              <input
                type="checkbox"
                value={row.id}
                checked={selections.has(row.id)}
                onChange={(ev: React.SyntheticEvent<HTMLInputElement>) => onSelectServer(ev, row.id)}
              />
            ) : (
              <input
                type="radio"
                value={row.id}
                checked={selections.has(row.id)}
                onChange={(ev: React.SyntheticEvent<HTMLInputElement>) => onSelectServer(ev, row.id)}
              />
            )
          }
        />
        <Column
          columnKey="name"
          width="20%"
          comparator={Utils.sortByText}
          header={t("Name")}
          cell={(row: ServerType) => <SystemLink id={row.id}>{row.name}</SystemLink>}
        />
        <Column
          columnKey="messages"
          width="77%"
          cell={(row: ServerType) => (row.messages ? <SystemMessages messages={row.messages} /> : null)}
        />
      </Table>
    </Panel>
  );
};

export default withErrorMessages(SelectServer);
