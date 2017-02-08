'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const {AsyncButton, LinkButton, Button} = require("../components/buttons");
const Panel = require("../components/panel").Panel;
const Network = require("../utils/network");
const Functions = require("../utils/functions");
const Utils = Functions.Utils;
const {Table, Column, SearchField} = require("../components/table");
const Messages = require("../components/messages").Messages;
const DeleteDialog = require("../components/dialogs").DeleteDialog;
const ModalButton = require("../components/dialogs").ModalButton;

const msgMap = {
  "not_found": t("Image store cannot be found."),
  "delete_success": t("Image store has been deleted.")
};

class ImageStores extends React.Component {

  constructor(props) {
    super();
    ["reloadData", "selectStore", "deleteStore"]
        .forEach(method => this[method] = this[method].bind(this));
    this.state = {
      messages: [],
      imagestores: []
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
  }

  selectStore(row) {
    this.setState({
        selected: row
    });
  }

  deleteStore(row) {
    const id = row.id;
    return Network.del("/rhn/manager/api/cm/imagestores/" + id).promise.then(data => {
        if (data.success) {
            this.setState({
                messages: <Messages items={data.messages.map(msg => {
                    return {severity: "success", text: msgMap[msg]};
                })}/>,
                imagestores: this.state.imagestores.filter(store => store.id !== id)
            });
        } else {
            this.setState({
                messages: <Messages items={state.messages.map(msg => {
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
      <AsyncButton id="reload" icon="refresh" name={t("Refresh")} text action={this.reloadData} />
      { isAdmin &&
          <LinkButton id="create" icon="fa-plus" className="btn-default" title={t("Create")} text={t("Create")} href="/rhn/manager/cm/imagestores/create" />
      }
    </div>;

    return (
      <span>
        <Panel title="Image Stores" icon="fa-list" button={ panelButtons }>
          {this.state.messages}
          <Table
              data={this.state.imagestores}
              identifier={imagestore => imagestore.id}
              initialSortColumnKey="id"
              initialItemsPerPage={userPrefPageSize}
              searchField={
                  <SearchField filter={this.searchData} criteria={""} />
              }>
            <Column
              columnKey="label"
              width="50%"
              comparator={Utils.sortByText}
              header={t('Label')}
              cell={ (row, criteria) => row.label }
            />
            <Column
              columnKey="type"
              width="35%"
              comparator={Utils.sortByText}
              header={t('Type')}
              cell={ (row, criteria) => row.type }
            />
            { isAdmin &&
              <Column
                width="15%"
                columnClass="text-right"
                headerClass="text-right"
                header={t('Actions')}
                cell={ (row, criteria) => {
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
        </Panel>

        <DeleteDialog id="delete-modal"
          title={t("Delete Store")}
          content={<span>{t("Are you sure you want to delete store")} <strong>{this.state.selected ? this.state.selected.label : ''}</strong>?</span>}
          item={this.state.selected}
          onConfirm={this.deleteStore}
          onClosePopUp={() => this.selectStore(undefined)}
        />
      </span>
    );
  }
}

ReactDOM.render(
  <ImageStores />,
  document.getElementById('image-stores')
)
