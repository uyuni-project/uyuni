import * as React from "react";
import SpaRenderer from "core/spa/spa-renderer";
import { Messages, Utils } from "components/messages";
import { Panel } from "components/panels/Panel";
import Network from "utils/network";
import { Button, SubmitButton } from "components/buttons";
import { Form } from "components/input/Form";
import { FormGroup } from "components/input/FormGroup";
import { Label } from "components/input/Label";
import { Select } from "components/input/Select";
import { Text } from "components/input/Text";
import { TopPanel } from "components/panels/TopPanel";
import { InnerPanel } from "components/panels/InnerPanel";
import { ActionSchedule } from "components/action-schedule";
import { localizedMoment } from "utils";
import { AsyncButton, LinkButton } from "components/buttons";
import { SearchField } from "components/table/SearchField";
import { Column } from "components/table/Column";
import { Table } from "components/table/Table";
import { DeleteDialog } from "components/dialog/DeleteDialog";
import { DEPRECATED_unsafeEquals } from "utils/legacy";
import { ModalButton } from "components/dialog/ModalButton";


const msgMap = {
  not_found: t("Tailoring file cannot be found."),
  delete_success: t("Tailoring file has been deleted."),
  delete_success_p: t("Tailoring files have been deleted."),
};

type Props = {};

type State = {
  messages: any;
  selectedItems: any;
  selected?: any;
  tailoringFiles: any;
};

class TailoringFiles extends React.Component<PropsType, StateType> {
 constructor(props) {
     super(props);
     this.state = {
       messages: [],
       selectedItems: [],
       tailoringFiles: [],
     };
   }
   onDateTimeChanged = (date) => {
       this.setState({ earliest: date });
   };
    onFormChange = (model) => {
       this.setState({
         model: model,
       });
     };
    selectTailoringFile = (row) => {
       this.setState({
         selected: row,
       });
     };
   deleteTailoringFiles= (idList) => {
       console.log("idList");
       console.log(idList);
       return Network.post("/rhn/manager/api/audit/scap/tailoring-file/delete", idList).then((data) => {
         if (data.success) {
           this.setState({
             messages: (
               <Messages
                 items={[
                   {
                     severity: "success",
                     text: msgMap[idList.length > 1 ? "delete_success_p" : "delete_success"],
                   },
                 ]}
               />
             ),
             tailoringFiles: this.state.tailoringFiles.filter((tailoringFile) => !idList.includes(tailoringFile.id)),
             selectedItems: this.state.selectedItems.filter((item) => !idList.includes(item)),
           });
         } else {
           this.setState({
             messages: (
               <Messages
                 items={data.messages.map((msg) => {
                   return { severity: "error", text: msgMap[msg] };
                 })}
               />
             ),
           });
         }
       });
     };

    handleSelectItems = (items) => {
        this.setState({
          selectedItems: items,
        });
      };

   renderButtons() {
       var buttons = [
          <SubmitButton
            key="create-btn"
            id="create-btn"
            className="btn-success"
            icon="fa-plus"
            text={t("Create")}
            disabled={this.state.isInvalid}
          />,
       ];
       return buttons;
     }

  onCreate = (model) => {

      return Network.post("/rhn/manager/api/audit/schedule/create",  {
        ids: window.minions?.map((m) => m.id),
          earliest: this.state.earliest,
          model,
        }).then((data) => {
            if (data.success) {
              Utils.urlBounce("/rhn/systems/Overview.do");
            } else {
              this.setState({
                messages: (
                  <Messages
                    items={data.messages.map((msg) => {
                      return { severity: "error", text: msgMap[msg] };
                    })}
                  />
                ),
              });
            }
          });
      };

  render () {
     const panelButtons = (
          <div className="pull-right btn-group">
            {window.isAdmin && this.state.selectedItems.length > 0 && (
              <ModalButton
                id="delete-selected"
                icon="fa-trash"
                className="btn-default"
                text={t("Delete")}
                title={t("Delete selected")}
                target="delete-selected-modal"
              />
            )}
            <AsyncButton id="reload" icon="fa-refresh" text={t("Refresh")} action={this.reloadData} />
            <LinkButton
                id="create"
                icon="fa-plus"
                className="btn-default"
                title={t("Create")}
                text={t("Create")}
                href="/rhn/manager/audit/scap/tailoring-file/create"
            />

          </div>
        );

        return (
          <span>
            <TopPanel
              title={t("Tailoring Files")}
              icon="spacewalk-icon-manage-configuration-files"
              button={panelButtons}
            >
              {this.state.messages}
              <Table
                data={window.tailoringFiles}
                identifier={(tailoringFile) => tailoringFile.id}
                initialSortColumnKey="id"
                searchField={<SearchField filter={this.searchData} />}
                selectable
                selectedItems={this.state.selectedItems}
                onSelect={this.handleSelectItems}
              >
                <Column
                  columnKey="label"
                  width="35%"
                  comparator={Utils.sortByText}
                  header={t("Label")}
                  cell={(row) => row.name}
                />
                <Column
                  columnKey="fileName"
                  width="45%"
                  comparator={Utils.sortByText}
                  header={t("Tailoring File Name")}
                  cell={(row) =>  row.fileName}
                />
                {true && (
                  <Column
                    width="15%"
                    header={t("Actions")}
                    columnClass="text-right"
                    headerClass="text-right"
                    cell={(row) => {
                      return (
                        <div className="btn-group">
                          <LinkButton
                            className="btn-default btn-sm"
                            title={t("Edit")}
                            icon="fa-edit"
                            href={"/rhn/manager/audit/scap/tailoring-file/edit/" + row.id}
                          />
                          <ModalButton
                            className="btn-default btn-sm"
                            title={t("Delete")}
                            icon="fa-trash"
                            target="delete-modal"
                            item={row}
                            onClick={this.selectTailoringFile}
                          />
                        </div>
                      );
                    }}
                  />
                )}
              </Table>
            </TopPanel>

            <DeleteDialog
              id="delete-modal"
              title={t("Delete Profile")}
              content={
                <span>
                  {t("Are you sure you want to delete Tailoring file")}{" "}
                  <strong>{this.state.selected ? this.state.selected.name: "dd"}</strong>?
                </span>
              }
              item={this.state.selected}
              onConfirm={(item) => this.deleteTailoringFiles([item.id])}
              onClosePopUp={() => this.selectTailoringFile(undefined)}
            />
            <DeleteDialog
              id="delete-selected-modal"
              title={t("Delete Selected Tailoring file(s)")}
              content={
                <span>
                  {DEPRECATED_unsafeEquals(this.state.selectedItems.length, 1)
                    ? t("Are you sure you want to delete the selected Tailoring file?")
                    : t(
                        "Are you sure you want to delete selected Tailoring files? ({0} Tailoring files selected)",
                        this.state.selectedItems.length
                      )}
                </span>
              }
              onConfirm={() => this.deleteTailoringFiles(this.state.selectedItems)}
            />
          </span>
        );
      }
 }

export const renderer = () => {
  return SpaRenderer.renderNavigationReact( <TailoringFiles/>, document.getElementById("scap-tailoring-files"));
}