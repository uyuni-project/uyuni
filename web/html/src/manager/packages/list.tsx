import * as React from "react";
import { useRef } from "react";

import { AsyncButton } from "components/buttons";
import { ActionConfirm } from "components/dialog/ActionConfirm";
import { IconTag } from "components/icontag";
import { DEPRECATED_Select, Form, Radio } from "components/input";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table, TableRef } from "components/table/Table";
import { MessagesContainer, showSuccessToastr } from "components/toastr/toastr";

import { Utils } from "utils/functions";
import { DEPRECATED_unsafeEquals } from "utils/legacy";
import Network from "utils/network";

type Props = {
  /** Locale of the help links */
  docsLocale: string;
  /** List of selected package ids */
  selected: string[];
  /** The entry to select in the channel field */
  selectedChannel: string | null;
};

export function PackageList(props: Props) {
  const [open, setOpen] = React.useState(false);
  const [selectedPackages, setSelectedPackages] = React.useState<string[]>(props.selected);
  const [formModel, setFormModel] = React.useState<object>({ binary: "binary", channel: props.selectedChannel });
  const [channels, setChannels] = React.useState([]);
  const tableRef = useRef<TableRef>(null);

  React.useEffect(() => {
    let ignore = false;
    Network.get("/rhn/manager/api/channels/owned")
      .then((resp) => {
        if (!ignore) {
          setChannels(
            resp.data.map((c) => {
              return {
                value: `channel/${c["id"]}`,
                label: c["name"],
                hasParent: !DEPRECATED_unsafeEquals(c["parentId"], null),
              };
            })
          );
        }
      })
      .catch(Network.showResponseErrorToastr);
    return () => {
      ignore = true;
    };
  }, []);

  const handleSelectedPackages = (items: string[]) => {
    setSelectedPackages(items);
  };

  const deleteSelected = () => {
    Network.post("/rhn/manager/api/packages/delete", selectedPackages)
      .then(() => {
        setSelectedPackages([]);
        tableRef.current?.refresh();
        showSuccessToastr("Selected package(s) removed");
      })
      .catch(Network.showResponseErrorToastr);
  };

  const deleteButton = [
    <AsyncButton
      key="delete-btn"
      defaultType="btn-danger"
      text={t("Delete")}
      icon="fa-trash"
      action={() => setOpen(true)}
      disabled={selectedPackages.length === 0}
    />,
  ];

  const selectOptions = [
    { value: "orphans", label: t("Packages in no channel") },
    { value: "all", label: t("All managed packages") },
  ].concat(channels);

  return (
    <>
      <MessagesContainer />
      <h1>
        <IconTag type="header-package" />
        {t(" Package Management ")}
        <a
          href={`/docs/${props.docsLocale}/reference/software/manage-packages.html`}
          target="_blank"
          rel="noopener noreferrer"
        >
          <IconTag type="header-help" />
        </a>
      </h1>

      <Form model={formModel} onChange={(model: object) => setFormModel({ ...model })}>
        <DEPRECATED_Select
          name="channel"
          label={t("Channel")}
          required
          options={selectOptions}
          labelClass="col-lg-3"
          divClass="col-lg-6"
          formatOptionLabel={(option, { context }) => {
            const prefix = option.hasParent && context === "menu" ? "⤷" : "";
            return `${prefix}${option.label}`;
          }}
        />
        <Radio
          name="binary"
          inline={true}
          divClass="col-lg-offset-3 offset-lg-3 col-lg-6"
          items={[
            { label: t("Binary packages"), value: "binary" },
            { label: t("Source packages"), value: "source" },
          ]}
        />
      </Form>
      <ActionConfirm
        id="confirmModal"
        type="remove"
        name={t("Delete")}
        itemName={t("package")}
        icon="fa-trash"
        selected={selectedPackages}
        onConfirm={deleteSelected}
        canForce={false}
        isOpen={open}
        onClose={() => setOpen(false)}
      />

      {!DEPRECATED_unsafeEquals(formModel["channel"], null) && formModel["channel"] !== "" && (
        <Table
          ref={tableRef}
          data={`/rhn/manager/api/packages/list/${formModel["binary"]}/${formModel["channel"]}`}
          identifier={(item) => item.id}
          initialSortColumnKey="nvrea"
          selectable={(item) => Object.prototype.hasOwnProperty.call(item, "id")}
          selectedItems={selectedPackages}
          onSelect={handleSelectedPackages}
          searchField={<SearchField placeholder={t("Filter by package name")} />}
          defaultSearchField={"nvrea"}
          emptyText={t("No Packages.")}
          titleButtons={deleteButton}
        >
          <Column
            columnKey="nvrea"
            comparator={Utils.sortByText}
            header={t("Package")}
            cell={(item) => <a href={`/rhn/software/packages/Details.do?pid=${item.id}`}>{item.nvrea}</a>}
          />
          <Column
            columnKey="channels"
            comparator={Utils.sortByText}
            header={t("Channels")}
            sortable={false}
            cell={(item) =>
              item.packageChannels.map((c) => (
                <>
                  {c.name}
                  <br />
                </>
              ))
            }
          />
          <Column
            columnKey="provider"
            comparator={Utils.sortByText}
            header={t("Content Provider")}
            sortable={false}
            cell={(item) => item.provider}
          />
        </Table>
      )}
    </>
  );
}
