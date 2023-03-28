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

const typeMap = {
  dockerfile: t("Dockerfile"),
  kiwi: t("Kiwi"),
};

const msgMap = {
  not_found: t("Image profile cannot be found."),
  delete_success: t("Image profile has been deleted."),
  delete_success_p: t("Image profiles have been deleted."),
};

type Props = {};

type State = {
  messages: any;
  imageprofiles: any;
  selectedItems: any;
  selected?: any;
  tailoringFiles: any;
};

class TailoringFiles extends React.Component<PropsType, StateType> {
 constructor(props) {
     super(props);
     this.state = {
       messages: [],
       imageprofiles: [],
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
   handleDataStreamChange = (name, value) => {
     console.log(name)
     console.log(value)
     this.getProfiles(value);
   };
    getProfiles(type) {
       return Network.get("/rhn/manager/api/audit/profiles/list/" + type).then((data) => {
         // Preselect store after retrieval
         this.setState({
           imageTypes: data,
         });
         return data;
       });
     }
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

      console.log("2"+model) ;
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
                href="/rhn/manager/audit/scap/create-tailoring-file"
            />

          </div>
        );

        return (
          <span>
            <TopPanel
              title={t("Tailoring Files")}
              icon="spacewalk-icon-manage-configuration-files"
              helpUrl="reference/images/images-profiles.html"
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
                  columnKey="imageType"
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
                            href={"/rhn/manager/cm/imageprofiles/edit/" + row.id}
                          />
                          <ModalButton
                            className="btn-default btn-sm"
                            title={t("Delete")}
                            icon="fa-trash"
                            target="delete-modal"
                            item={row}
                            onClick={this.selectProfile}
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
                  {t("Are you sure you want to delete profile")}{" "}
                  <strong>{this.state.selected ? this.state.selected.label : ""}</strong>?
                </span>
              }
              item={this.state.selected}
              onConfirm={(item) => this.deleteProfiles([item.id])}
              onClosePopUp={() => this.selectProfile(undefined)}
            />
            <DeleteDialog
              id="delete-selected-modal"
              title={t("Delete Selected Profile(s)")}
              content={
                <span>
                  {DEPRECATED_unsafeEquals(this.state.selectedItems.length, 1)
                    ? t("Are you sure you want to delete the selected profile?")
                    : t(
                        "Are you sure you want to delete selected profiles? ({0} profiles selected)",
                        this.state.selectedItems.length
                      )}
                </span>
              }
              onConfirm={() => this.deleteProfiles(this.state.selectedItems)}
            />
          </span>
        );
      }
 }

export const renderer = () => {
  return SpaRenderer.renderNavigationReact( <TailoringFiles/>, document.getElementById("scap-tailoring-files"));
}