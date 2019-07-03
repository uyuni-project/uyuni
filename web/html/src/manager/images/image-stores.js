/* eslint-disable */
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const {AsyncButton, LinkButton} = require("components/buttons");
const { TopPanel } = require('components/panels/TopPanel');
const Network = require("utils/network");
const Functions = require("utils/functions");
const Utils = Functions.Utils;
const {Table, Column, SearchField} = require("components/table");
const Messages = require("components/messages").Messages;
const DeleteDialog = require("components/dialog/DeleteDialog").DeleteDialog;
const ModalButton = require("components/dialog/ModalButton").ModalButton;

/* global isAdmin */

const msgMap = {
  "not_found": t("Image store cannot be found."),
  "delete_success": t("Image store has been deleted."),
  "delete_success_p": t("Image stores have been deleted.")
};

const typeMap = {
  "registry": "Registry",
  "os_image": "OS Image"
};

class ImageStores extends React.Component {

  constructor(props) {
    super(props);
    ["reloadData", "handleSelectItems", "selectStore", "deleteStores"]
      .forEach(method => this[method] = this[method].bind(this));
    this.state = {
      messages: [],
      imagestores: [],
      selectedItems: []
    };
    this.reloadData();
  }

  searchData(row, criteria) {
    if (criteria) {
      return row.label.toLocaleLowerCase().includes(criteria.toLocaleLowerCase())

    }
    return true;
  }

  reloadData() {
    Network.get("/rhn/manager/api/cm/imagestores").promise.then(data => {
      this.setState({
        imagestores: data
      });
    });
    this.clearMessages();
  }

  clearMessages() {
    this.setState({
      messages: undefined
    });
  }

  handleSelectItems(items) {
    this.setState({
      selectedItems: items
    });
  }

  selectStore(row) {
    this.setState({
      selected: row
    });
  }

  deleteStores(idList) {
    return Network.post("/rhn/manager/api/cm/imagestores/delete",
      JSON.stringify(idList), "application/json").promise.then(data => {
      if (data.success) {
        this.setState({
          messages: <Messages items={[{severity: "success", text: msgMap[idList.length > 1 ? "delete_success_p" : "delete_success"]}]}/>,
          imagestores: this.state.imagestores.filter(store => !idList.includes(store.id)),
          selectedItems: this.state.selectedItems.filter(item => !idList.includes(item))
        });
      } else {
        this.setState({
          messages: <Messages items={data.messages.map(msg => {
            return {severity: "error", text: msgMap[msg]};
          })}/>
        });
      }
    }).promise;
  }

  isFiltered(criteria) {
    return criteria && criteria.length > 0;
  }

  render() {
    const panelButtons = <div className="pull-right btn-group">
      { isAdmin && this.state.selectedItems.length > 0 &&
          <ModalButton id="delete-selected" icon="fa-trash" className="btn-default" text={t("Delete")}
            title={t("Delete selected")} target="delete-selected-modal"/>
      }
      <AsyncButton id="reload" icon="fa-refresh" text={t("Refresh")} action={this.reloadData} />
      { isAdmin &&
          <LinkButton id="create" icon="fa-plus" className="btn-default" title={t("Create")} text={t("Create")} href="/rhn/manager/cm/imagestores/create" />
      }
    </div>;

    return (
      <span>
        <TopPanel title={t("Image Stores")} icon="fa-list" helpUrl="/docs/reference/images/images-stores.html" button={ panelButtons }>
          {this.state.messages}
          <Table
            data={this.state.imagestores}
            identifier={imagestore => imagestore.id}
            initialSortColumnKey="id"
            initialItemsPerPage={userPrefPageSize}
            searchField={
              <SearchField filter={this.searchData} criteria={""} />
            }
            selectable
            selectedItems={this.state.selectedItems}
            onSelect={this.handleSelectItems}>
            <Column
              columnKey="label"
              width="50%"
              comparator={Utils.sortByText}
              header={t('Label')}
              cell={ (row) => row.label }
            />
            <Column
              columnKey="type"
              width="35%"
              comparator={Utils.sortByText}
              header={t('Type')}
              cell={ (row) => typeMap[row.type] }
            />
            { isAdmin &&
              <Column
                width="15%"
                columnClass="text-right"
                headerClass="text-right"
                header={t('Actions')}
                cell={ (row) => {
                  return <div className="btn-group">
                    <LinkButton
                      className="btn-default btn-sm"
                      title={t("Edit")}
                      icon="fa-edit"
                      href = {"/rhn/manager/cm/imagestores/edit/" + row.id}
                    />
                    <ModalButton
                      className="btn-default btn-sm"
                      title={t("Delete")}
                      icon="fa-trash"
                      target="delete-modal"
                      item={row}
                      onClick={this.selectStore}
                    />
                  </div>;
                }}
              />
            }
          </Table>
        </TopPanel>
        <DeleteDialog id="delete-modal"
          title={t("Delete Store")}
          content={<span>{t("Are you sure you want to delete store")} <strong>{this.state.selected ? this.state.selected.label : ''}</strong>?</span>}
          item={this.state.selected}
          onConfirm={(item) => this.deleteStores([item.id])}
          onClosePopUp={() => this.selectStore(undefined)}
        />
        <DeleteDialog id="delete-selected-modal"
          title={t("Delete Selected Store(s)")}
          content={
            <span>
              {this.state.selectedItems.length == 1 ? t("Are you sure you want to delete the selected store?") : t("Are you sure you want to delete selected stores? ({0} stores selected)", this.state.selectedItems.length)}
            </span>
          }
          onConfirm={() => this.deleteStores(this.state.selectedItems)}
        />
      </span>
    );
  }
}

ReactDOM.render(
  <ImageStores />,
  document.getElementById('image-stores')
)
