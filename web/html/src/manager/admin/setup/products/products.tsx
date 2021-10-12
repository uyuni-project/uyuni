import { searchCriteriaInExtension } from "./products.utils";

import { SectionToolbar } from "components/section-toolbar/section-toolbar";
import * as React from "react";
import Network from "utils/network";
import { Messages, MessageType } from "components/messages";
import { Utils as MessagesUtils } from "components/messages";
import { CustomDataHandler } from "components/table/CustomDataHandler";
import { SearchField } from "components/table/SearchField";
import { ModalLink } from "components/dialog/ModalLink";
import { Button, AsyncButton } from "components/buttons";
import { SCCDialog } from "./products-scc-dialog";
import { PopUp } from "components/popup";
import { CustomDiv } from "components/custom-objects";
import { Toggler } from "components/toggler";
import { HelpLink } from "components/utils/HelpLink";
import { ChannelLink } from "components/links";
import SpaRenderer from "core/spa/spa-renderer";
import { DEPRECATED_unsafeEquals } from "utils/legacy";

declare global {
  interface Window {
    // See java/build/classes/com/suse/manager/webui/templates/products/show.jade
    issMaster_flag_from_backend?: any;
    refreshNeeded_flag_from_backend?: any;
    refreshRunning_flag_from_backend?: any;
    scc_refresh_file_locked_status?: any;
    noToolsChannelSubscription_flag_from_backend?: any;
  }
}

type Instance = JQuery;
type StaticProperties = {};
type Select2 = ((...args: any[]) => Instance) & StaticProperties;

declare global {
  interface JQuery {
    select2: Select2;
  }
}

const msgMap = {
  // Nothing for now
};

const _DATA_ROOT_ID = "baseProducts";

const _SETUP_WIZARD_STEPS = [
  {
    id: "wizard-step-proxy",
    label: "HTTP Proxy",
    url: "/rhn/admin/setup/ProxySettings.do",
    active: false,
  },
  {
    id: "wizard-step-credentials",
    label: "Organization Credentials",
    url: "/rhn/admin/setup/MirrorCredentials.do",
    active: false,
  },
  {
    id: "wizard-step-suse-products",
    label: "Products",
    url: window.location.href.split(/\?|#/)[0],
    active: true,
  },
];

const _PRODUCT_STATUS = {
  installed: "INSTALLED",
  available: "AVAILABLE",
  unavailable: "UNAVAILABLE",
};

const _CHANNEL_STATUS = {
  notSynced: "NOT_MIRRORED",
  syncing: "IN_PROGRESS",
  synced: "FINISHED",
  failed: "FAILED",
};

const _COLS = {
  selector: { width: 2, um: "em" },
  showSubList: { width: 2, um: "em" },
  description: { width: "auto", um: "" },
  arch: { width: 6, um: "em" },
  channels: { width: 7, um: "em" },
  mix: { width: 13, um: "em" },
};

function reloadData() {
  return Network.get("/rhn/manager/api/admin/products");
}

/**
 * Generate the page wrapper, tabs, scc-popup,
 * and everything around the product list except the list
 */
class ProductsPageWrapper extends React.Component {
  state = {
    issMaster: window.issMaster_flag_from_backend,
    refreshNeeded: window.refreshNeeded_flag_from_backend,
    refreshRunning: window.refreshRunning_flag_from_backend || window.scc_refresh_file_locked_status,
    noToolsChannelSubscription: window.noToolsChannelSubscription_flag_from_backend,
    serverData: [] as any[],
    errors: [] as MessageType[],
    loading: true,
    selectedItems: [] as any[],
    sccSyncRunning: false,
    addingProducts: false,
    scheduledItems: [] as any[],
    scheduleResyncItems: [] as any[],
  };

  UNSAFE_componentWillMount() {
    if (!this.state.refreshRunning) {
      this.refreshServerData();
    }
  }

  forceStartSccSync = () => {
    // trigger the refresh at the first page load if
    return (
      window.refreshNeeded_flag_from_backend &&
      window.issMaster_flag_from_backend &&
      !window.refreshRunning_flag_from_backend
    );
  };

  refreshServerData = () => {
    this.setState({ loading: true });
    var currentObject = this;
    let resultMessages: MessageType[] = [];
    if (currentObject.state.noToolsChannelSubscription && currentObject.state.issMaster) {
      resultMessages = MessagesUtils.warning(
        t("No SUSE Manager Server Subscription available. Products requiring Client Tools Channel will not be shown.")
      );
    }
    reloadData()
      .then((data) => {
        currentObject.setState({
          serverData: data[_DATA_ROOT_ID],
          errors: resultMessages,
          loading: false,
          selectedItems: [],
          scheduleResyncItems: [],
          scheduledItems: [],
        });
      })
      .catch(this.handleResponseError);
  };

  handleSelectedItems = (items) => {
    let arr = this.state.selectedItems;
    // add all items those are not yet in the existsing set
    arr = arr.concat(items.filter((i) => !arr.map((a) => a.identifier).includes(i.identifier)));
    this.setState({ selectedItems: arr });
  };

  handleUnselectedItems = (items) => {
    let arr = this.state.selectedItems;
    // keep all items in the existsing set those are not in the unselected items
    arr = arr.filter((a) => !items.map((i) => i.identifier).includes(a.identifier));
    this.setState({ selectedItems: arr });
  };

  clearSelection = () => {
    this.setState({ selectedItems: [] });
  };

  updateSccSyncRunning = (sccSyncStatus) => {
    // if it was running and now it's finished
    if (this.state.sccSyncRunning && !sccSyncStatus) {
      this.refreshServerData(); // reload data
      this.setState({ errors: [] });
    }

    if (sccSyncStatus) {
      this.setState({ errors: MessagesUtils.info(t("The product catalog refresh is running...")) });
    }
    this.setState({ sccSyncRunning: sccSyncStatus });
  };

  submit = () => {
    const currentObject = this;
    currentObject.setState({ addingProducts: true });
    Network.post(
      "/rhn/manager/admin/setup/products",
      currentObject.state.selectedItems.map((i) => i.identifier)
    )
      .then((data) => {
        // returned data format is { productId : isFailedFlag }
        let failedProducts = currentObject.state.selectedItems.filter((i) => data[i.identifier]);
        let resultMessages: MessageType[] | null = null;
        if (failedProducts.length === 0) {
          resultMessages = MessagesUtils.success("Selected channels/products were scheduled successfully for syncing.");
        } else {
          resultMessages = MessagesUtils.warning(
            "The following product installations failed: '" +
              failedProducts.reduce((a, b) => a.label + ", " + b.label) +
              "'. Please check log files."
          );
        }
        currentObject.setState({
          errors: resultMessages,
          selectedItems: [],
          addingProducts: false,
        });
        this.refreshServerData();
      })
      .catch(currentObject.handleResponseError);
  };

  resyncProduct = (id, name) => {
    const currentObject = this;
    currentObject.state.scheduledItems.concat([id]);
    var scheduleResyncItemsNew = currentObject.state.scheduleResyncItems.concat([id]);
    currentObject.setState({ scheduleResyncItems: scheduleResyncItemsNew });
    Network.post("/rhn/manager/admin/setup/products", [id])
      .then((data) => {
        if (!data[id]) {
          currentObject.setState({
            errors: MessagesUtils.success("The product '" + name + "' sync has been scheduled successfully"),
            scheduleResyncItems: scheduleResyncItemsNew.filter((i) => !DEPRECATED_unsafeEquals(i, id)),
            scheduledItems: currentObject.state.scheduledItems.concat([id]),
          });
        } else {
          currentObject.setState({
            errors: MessagesUtils.warning(
              "The product '" + name + "' sync was not scheduled correctly. Please check server log files."
            ),
            scheduleResyncItems: scheduleResyncItemsNew.filter((i) => !DEPRECATED_unsafeEquals(i, id)),
          });
        }
      })
      .catch(currentObject.handleResponseError);
  };

  handleResponseError = (jqXHR: JQueryXHR, arg = "") => {
    const msg = Network.responseErrorMessage(jqXHR, (status, msg) => (msgMap[msg] ? t(msgMap[msg], arg) : null));
    this.setState({ errors: this.state.errors.concat(msg) });
  };

  render() {
    const title = (
      <div className="spacewalk-toolbar-h1">
        <h1>
          <i className="fa fa-cogs"></i>
          &nbsp;
          {t("Setup Wizard")}
          &nbsp;
          <HelpLink url="reference/admin/setup-wizard.html" />
        </h1>
      </div>
    );

    const tabs = (
      <div className="spacewalk-content-nav">
        <ul className="nav nav-tabs">
          {_SETUP_WIZARD_STEPS.map((step) => (
            <li key={step.id} className={step.active ? "active" : ""}>
              <a className="js-spa" href={step.url}>
                {t(step.label)}
              </a>
            </li>
          ))}
        </ul>
      </div>
    );

    let pageContent;
    if (this.state.refreshRunning) {
      pageContent = (
        <div className="alert alert-warning" role="alert">
          {t("A refresh of the product data is currently running in the background. Please try again later.")}
        </div>
      );
    } else if (this.state.issMaster) {
      const submitButtonTitle = this.state.sccSyncRunning
        ? t("The product catalog is still refreshing, please wait.")
        : this.state.selectedItems.length === 0
        ? t("Select some product first.")
        : undefined;
      const addProductButton =
        this.state.sccSyncRunning || this.state.selectedItems.length === 0 || this.state.addingProducts ? (
          <Button
            id="addProducts"
            icon={this.state.addingProducts ? "fa-plus-circle fa-spin" : "fa-plus"}
            className="btn-default text-muted"
            title={submitButtonTitle}
            text={t("Add products")}
          />
        ) : (
          <Button
            id="addProducts"
            icon="fa-plus"
            className={"btn-success"}
            text={
              t("Add products") +
              (this.state.selectedItems.length > 0 ? " (" + this.state.selectedItems.length + ")" : "")
            }
            handler={this.submit}
          />
        );

      pageContent = (
        <div className="row" id="suse-products">
          <div className="col-sm-9">
            <Messages items={this.state.errors} />
            <div>
              <SectionToolbar>
                <div className="action-button-wrapper">
                  <div className="btn-group">
                    <Button
                      id="clearSelection"
                      icon="fa-eraser"
                      className={"btn-default " + (this.state.selectedItems.length === 0 ? "text-muted" : "")}
                      title={t("Clear products selection")}
                      text={t("Clear")}
                      handler={this.clearSelection}
                    />
                    {addProductButton}
                  </div>
                </div>
              </SectionToolbar>
              <Products
                data={this.state.serverData}
                loading={this.state.loading}
                readOnlyMode={this.state.sccSyncRunning}
                handleSelectedItems={this.handleSelectedItems}
                handleUnselectedItems={this.handleUnselectedItems}
                selectedItems={this.state.selectedItems}
                resyncProduct={this.resyncProduct}
                scheduledItems={this.state.scheduledItems}
                scheduleResyncItems={this.state.scheduleResyncItems}
              />
            </div>
          </div>
          <div className="col-sm-3 hidden-xs" id="wizard-faq">
            <SCCDialog
              forceStart={this.forceStartSccSync()}
              updateSccSyncRunning={(sccSyncStatus) => this.updateSccSyncRunning(sccSyncStatus)}
            />
            <hr />
            {this.state.selectedItems.length > 0 ? (
              <div className="text-left">
                <h4>Selected products</h4>
                <ul>
                  {this.state.selectedItems.map((i) => (
                    <li key={i.identifier}>
                      {i.label} [{i.arch}]
                    </li>
                  ))}
                </ul>
              </div>
            ) : null}
            <hr />
            <h4>{t("Why aren't all products displayed in the list?")}</h4>
            <p>
              {t(
                "The products displayed on this list are directly linked to your Organization credentials (Mirror credentials) as well as your SUSE subscriptions."
              )}
            </p>
            <p>
              {t(
                "If you believe there are products missing, make sure you have added the correct Organization credentials in the previous wizard step."
              )}
            </p>
          </div>
        </div>
      );
    } else {
      pageContent = (
        <div className="alert alert-warning" role="alert">
          {t(
            "This server is configured as an Inter-Server Synchronisation (ISS) slave. Products can only be managed on the ISS master."
          )}
        </div>
      );
    }

    const prevStyle = { marginLeft: "10px", verticalAlign: "middle" };
    const activeStep = _SETUP_WIZARD_STEPS.find((step) => step.active);
    const currentStepIndex = activeStep ? _SETUP_WIZARD_STEPS.indexOf(activeStep) : -1;
    const footer = (
      <div className="panel-footer">
        <div className="btn-group">
          {currentStepIndex > 1 ? (
            <a className="btn btn-default" href={_SETUP_WIZARD_STEPS[currentStepIndex - 1].url}>
              <i className="fa fa-arrow-left"></i>
              {t("Prev")}
            </a>
          ) : null}
          {currentStepIndex < _SETUP_WIZARD_STEPS.length - 1 ? (
            <a className="btn btn-success" href={_SETUP_WIZARD_STEPS[currentStepIndex + 1].url}>
              <i className="fa fa-arrow-right"></i>
              {t("Next")}
            </a>
          ) : null}
        </div>
        <span style={prevStyle}>
          {currentStepIndex + 1}&nbsp;{t("of")}&nbsp;{_SETUP_WIZARD_STEPS.length}
        </span>
      </div>
    );

    return (
      <div className="responsive-wizard">
        {title}
        {tabs}
        <div className="panel panel-default" id="products-content">
          <div className="panel-body">{pageContent}</div>
        </div>
        {footer}
      </div>
    );
  }
}

type ProductsProps = {
  data: any[];
  loading?: boolean;
  readOnlyMode?: boolean;
  resyncProduct: any;
  scheduledItems: any;
  scheduleResyncItems: any;
  selectedItems: any[];
  handleSelectedItems: (...args: any[]) => any;
  handleUnselectedItems: (...args: any[]) => any;
};

/**
 * Show the products data
 */
class Products extends React.Component<ProductsProps> {
  state = {
    popupItem: null,
    archCriteria: [] as any[],
    visibleSubList: [] as any[],
  };

  componentDidMount() {
    const currentObject = this;

    //HACK: usage of JQuery here is needed to apply the select2js plugin
    jQuery("select#product-arch-filter.apply-select2js-on-this").each(function () {
      var select = jQuery(this);
      // apply select2js only one time
      if (!select.hasClass("select2js-applied")) {
        select.addClass("select2js-applied");

        var select2js = select.select2({ placeholder: t("Filter by architecture") });
        select2js.on("change", function (event) {
          currentObject.handleFilterArchChange(select.val() || []);
        });
      }
    });
  }

  getDistinctArchsFromData = (data) => {
    var archs: any[] = [];
    Object.keys(data)
      .map((id) => data[id])
      .forEach(function (x) {
        if (!archs.includes(x.arch)) archs.push(x.arch);
      });
    return archs;
  };

  handleFilterArchChange = (archs) => {
    this.setState({ archCriteria: archs });
  };

  filterDataByArch = (data: any[]) => {
    if (this.state.archCriteria.length > 0) {
      return data.filter((p) => this.state.archCriteria.includes(p.arch));
    }
    return data;
  };

  handleSelectedItems = (items) => {
    this.props.handleSelectedItems(items);
  };

  compareProducts = (prod1, prod2) => {
    const suseStr = "SUSE";
    const isProd1Suse = prod1.label.startsWith(suseStr);
    const isProd2Suse = prod2.label.startsWith(suseStr);

    // if one of the products is SUSE, it's lower in the ordering
    if (isProd1Suse && !isProd2Suse) {
      return -1;
    }
    if (!isProd1Suse && isProd2Suse) {
      return 1;
    }

    // otherwise use the label-based ordering
    return prod1.label.toLowerCase().localeCompare(prod2.label.toLowerCase());
  };

  buildRows = (message) => {
    return Object.keys(message).map((id) => message[id]);
  };

  showChannelsfor = (item) => {
    this.setState({ popupItem: item });
  };

  handleVisibleSublist = (id) => {
    let arr = this.state.visibleSubList;
    if (arr.includes(id)) {
      arr = arr.filter((i) => i !== id);
    } else {
      arr = arr.concat([id]);
    }
    this.setState({ visibleSubList: arr });
  };

  render() {
    const archFilter = (
      <div className="multiple-select-wrapper">
        <select
          id="product-arch-filter"
          name="product-arch-filter"
          className="form-control d-inline-block apply-select2js-on-this"
          multiple={true}
        >
          {this.getDistinctArchsFromData(this.props.data).map((a, i) => (
            <option key={a + i} value={a}>
              {a}
            </option>
          ))}
        </select>
      </div>
    );
    return (
      <div>
        <CustomDataHandler
          data={this.buildRows(this.filterDataByArch([...this.props.data]).sort(this.compareProducts))}
          identifier={(raw) => raw.identifier}
          initialItemsPerPage={window.userPrefPageSize}
          loading={this.props.loading}
          additionalFilters={[archFilter]}
          searchField={
            <SearchField
              filter={searchCriteriaInExtension}
              placeholder={t("Filter by product Description")}
              name="product-description-filter"
            />
          }
        >
          <CheckList
            data={(d) => d}
            bypassProps={{
              nestedKey: "extensions",
              isSelectable: true,
              selectedItems: this.props.selectedItems,
              listStyleClass: "product-list",
              showChannelsfor: this.showChannelsfor,
              cols: _COLS,
              resyncProduct: this.props.resyncProduct,
              scheduledItems: this.props.scheduledItems,
              scheduleResyncItems: this.props.scheduleResyncItems,
              handleVisibleSublist: this.handleVisibleSublist,
              visibleSubList: this.state.visibleSubList,
              readOnlyMode: this.props.readOnlyMode,
            }}
            handleSelectedItems={this.props.handleSelectedItems}
            handleUnselectedItems={this.props.handleUnselectedItems}
            treeLevel={1}
            childrenDisabled={false}
          />
        </CustomDataHandler>
        <ChannelsPopUp item={this.state.popupItem} />
      </div>
    );
  }
}

type CheckListProps = {
  data: any;
  bypassProps: any;
  treeLevel: any;
  childrenDisabled?: boolean;
  handleSelectedItems: (...args: any[]) => any;
  handleUnselectedItems: (...args: any[]) => any;
};

/**
 * Generate a custom list of elements for the products data
 */
class CheckList extends React.Component<CheckListProps> {
  isRootLevel = (level) => {
    return DEPRECATED_unsafeEquals(level, 1);
  };

  render() {
    return this.props.data ? (
      <ul className={this.props.bypassProps.listStyleClass}>
        {this.isRootLevel(this.props.treeLevel) ? (
          <li className="list-header">
            <CustomDiv
              className="col text-center"
              width={this.props.bypassProps.cols.selector.width}
              um={this.props.bypassProps.cols.selector.um}
            ></CustomDiv>
            <CustomDiv
              className="col text-center"
              width={this.props.bypassProps.cols.showSubList.width}
              um={this.props.bypassProps.cols.showSubList.um}
            ></CustomDiv>
            <CustomDiv
              className="col col-class-calc-width"
              width={this.props.bypassProps.cols.description.width}
              um={this.props.bypassProps.cols.description.um}
            >
              {t("Product Description")}
            </CustomDiv>
            <CustomDiv
              className="col"
              width={this.props.bypassProps.cols.arch.width}
              um={this.props.bypassProps.cols.arch.um}
              title={t("Architecture")}
            >
              {t("Arch")}
            </CustomDiv>
            <CustomDiv
              className="col text-right"
              width={this.props.bypassProps.cols.channels.width}
              um={this.props.bypassProps.cols.channels.um}
            >
              {t("Channels")}
            </CustomDiv>
            <CustomDiv
              className="col text-right"
              width={this.props.bypassProps.cols.mix.width}
              um={this.props.bypassProps.cols.mix.um}
            ></CustomDiv>
          </li>
        ) : null}
        {this.props.data.map((l, index) => {
          return (
            <CheckListItem
              key={l.identifier}
              item={l}
              bypassProps={this.props.bypassProps}
              handleSelectedItems={this.props.handleSelectedItems}
              handleUnselectedItems={this.props.handleUnselectedItems}
              treeLevel={this.props.treeLevel}
              childrenDisabled={this.props.childrenDisabled}
              index={index}
            />
          );
        })}
      </ul>
    ) : null;
  }
}

type CheckListItemProps = {
  item: any;
  bypassProps: any;
  treeLevel: any;
  childrenDisabled?: boolean;
  index: number;
  handleSelectedItems: (...args: any[]) => any;
  handleUnselectedItems: (...args: any[]) => any;
};

/**
 * A component to generate a list item which contains
 * all information for a single product
 */
class CheckListItem extends React.Component<CheckListItemProps> {
  state = {
    withRecommended: true,
  };

  UNSAFE_componentWillReceiveProps(nextProps) {
    if (this.isSelected(nextProps.item, nextProps.bypassProps.selectedItems)) {
      this.handleWithRecommendedState(nextProps.bypassProps.selectedItems);
    }
  }

  isRootLevel = (level) => {
    return DEPRECATED_unsafeEquals(level, 1);
  };

  isSelected = (item, selectedItems) => {
    return selectedItems.filter((i) => DEPRECATED_unsafeEquals(i.identifier, item.identifier)).length === 1;
  };

  isInstalled = () => {
    return this.props.item.status === _PRODUCT_STATUS.installed;
  };

  isSublistVisible = () => {
    return this.props.bypassProps.visibleSubList.includes(this.props.item.identifier);
  };

  handleSelectedItem = () => {
    const currentItem = this.props.item;

    // add base product first (the server fails if it tries to add extentions first)
    var arr = [this.props.item];

    // this item was selected but it is going to be removed from the selected set,
    // so all children are going to be removed as well
    if (this.isSelected(currentItem, this.props.bypassProps.selectedItems)) {
      arr = arr.concat(this.getChildrenTree(currentItem));
      this.handleUnselectedItems(arr);
    } else {
      // this item was not selected and it is going to be added to the selected set,

      // if any required product, add them first
      if (currentItem.required) {
        arr = arr.concat(currentItem.required);
      }

      // if it has the recommended flag enabled,
      // all recommended children are going to be added as well
      if (this.state.withRecommended && this.isRootLevel(this.props.treeLevel)) {
        arr = arr.concat(this.getRecommendedChildrenTree(currentItem));
      }
      this.handleSelectedItems(arr);
    }
  };

  getChildrenTree = (item) => {
    var arr = this.getNestedData(item);
    let nestedArr = [];
    arr.forEach((child) => {
      nestedArr = nestedArr.concat(this.getChildrenTree(child));
    });
    return arr.concat(nestedArr);
  };

  getRecommendedChildrenTree = (item) => {
    return this.getChildrenTree(item).filter((el) => el.recommended);
  };

  handleSelectedItems = (items) => {
    this.props.handleSelectedItems(items);
  };

  handleUnselectedItems = (items) => {
    this.props.handleUnselectedItems(items);
  };

  handleWithRecommended = () => {
    const withRecommendedNow = !this.state.withRecommended;
    this.setState({ withRecommended: withRecommendedNow });
    // only if this item is already selected
    if (this.isSelected(this.props.item, this.props.bypassProps.selectedItems)) {
      const arr = this.getRecommendedChildrenTree(this.props.item);
      // if the recommended flag is now enabled, select all recommended children
      if (withRecommendedNow) {
        this.props.handleSelectedItems(arr);
      } // else unselected them all
      else {
        this.props.handleUnselectedItems(arr);
      }
    }
  };

  // check if all recommended children are in the selection set,
  // and set the 'withRecommended' flag state accordingly
  handleWithRecommendedState = (arr) => {
    // it matters only for the root node
    if (DEPRECATED_unsafeEquals(this.props.treeLevel, 1)) {
      this.setState({
        withRecommended: this.getRecommendedChildrenTree(this.props.item).every((i) => arr.includes(i)) ? true : false,
      });
    }
  };

  scheduleResyncInProgress = () => {
    return this.props.bypassProps.scheduleResyncItems.includes(this.props.item.identifier);
  };

  productResyncScheduled = () => {
    return this.props.bypassProps.scheduledItems.includes(this.props.item.identifier);
  };

  resyncProduct = () => {
    this.props.bypassProps.resyncProduct(this.props.item.identifier, this.props.item.label);
  };

  getNestedData = (item) => {
    if (item && this.props.bypassProps.nestedKey && item[this.props.bypassProps.nestedKey] != null) {
      return item[this.props.bypassProps.nestedKey];
    }
    return [];
  };

  channelsStatusSync = (channelList, isRootProduct) => {
    const iconSize = isRootProduct ? "fa-1-5x" : "";
    const wrapPrefix = isRootProduct ? null : "(";
    const wrapSuffix = isRootProduct ? null : ")";
    const testPrefix = isRootProduct ? "Product" : "Child products";
    if (channelList.filter((c) => c.status === _CHANNEL_STATUS.failed).length > 0) {
      return (
        <span className="text-danger" title={t(testPrefix + " channels sync failed")}>
          {wrapPrefix}
          <i className={"fa fa-exclamation-circle " + iconSize}></i>
          {wrapSuffix}
        </span>
      );
    } else if (channelList.filter((c) => c.status === _CHANNEL_STATUS.syncing).length > 0) {
      return (
        <span className="text-info" title={t(testPrefix + " channels sync in progress")}>
          {wrapPrefix}
          <i className={"fa fa-spinner fa-spin " + iconSize}></i>
          {wrapSuffix}
        </span>
      );
    } else if (channelList.filter((c) => c.status === _CHANNEL_STATUS.synced).length > 0) {
      return (
        <span className="text-success" title={t(testPrefix + " channels synced")}>
          {wrapPrefix}
          <i className={"fa fa-check-circle " + iconSize}></i>
          {wrapSuffix}
        </span>
      );
    }
  };

  render() {
    const currentItem = this.props.item;

    /** generate item selector content **/
    let selectorContent: React.ReactNode = null;
    if (this.props.bypassProps.isSelectable && currentItem.status === _PRODUCT_STATUS.available) {
      selectorContent = (
        <input
          type="checkbox"
          id={"checkbox-for-" + currentItem.identifier}
          value={currentItem.identifier}
          onChange={this.handleSelectedItem}
          checked={this.isSelected(currentItem, this.props.bypassProps.selectedItems)}
          disabled={this.props.bypassProps.readOnlyMode || this.props.childrenDisabled}
          title={
            this.props.childrenDisabled
              ? t("To enable this product, the parent product should be selected first")
              : t("Select this product")
          }
        />
      );
    } else if (this.isInstalled()) {
      selectorContent = (
        <input
          type="checkbox"
          id={"checkbox-for-" + currentItem.identifier}
          value={currentItem.identifier}
          checked={true}
          disabled={true}
          title={t("This product is mirrored.")}
        />
      );
    }

    /** generate show nested list icon **/
    let showNestedDataIconContent;
    if (this.getNestedData(currentItem).length > 0) {
      const openSubListIconClass = this.isSublistVisible() ? "fa-angle-down" : "fa-angle-right";
      showNestedDataIconContent = (
        <i
          className={"fa " + openSubListIconClass + " fa-1-5x pointer product-hover"}
          onClick={() => this.props.bypassProps.handleVisibleSublist(currentItem.identifier)}
        />
      );
    }

    /** generate product description content **/
    let handleDescriptionClick: (() => any) | undefined = undefined;
    let hoverableDescriptionClass = "";
    if (this.getNestedData(currentItem).length > 0) {
      handleDescriptionClick = () => this.props.bypassProps.handleVisibleSublist(currentItem.identifier);
      hoverableDescriptionClass = "product-hover pointer";
    }
    let productDescriptionContent = (
      <span className={"product-description " + hoverableDescriptionClass} onClick={handleDescriptionClick}>
        {currentItem.label}&nbsp;
        {currentItem.recommended ? (
          <span className="recommended-tag" title={"This extension is recommended"}>
            {t("recommended")}
          </span>
        ) : null}
      </span>
    );

    /** generate recommended toggler if needed **/
    // only for root and installed products
    let recommendedTogglerContent;
    if (
      !this.isInstalled() &&
      this.isRootLevel(this.props.treeLevel) &&
      this.getNestedData(currentItem).some((i) => i.recommended)
    ) {
      recommendedTogglerContent = (
        <Toggler
          handler={this.handleWithRecommended.bind(this)}
          value={this.state.withRecommended}
          text={t("include recommended")}
        />
      );
    }

    /** generate channel sync status **/
    let channelSyncContent;
    if (this.isInstalled()) {
      let currentProductChannels = currentItem.channels.filter((c) => c.status !== _CHANNEL_STATUS.notSynced);
      channelSyncContent = this.channelsStatusSync(currentProductChannels, true);
    }

    let childProductChannelSyncContent;
    if (this.isInstalled()) {
      let childProductChannels: any[] = [];

      // add all channels of the subtree
      this.getChildrenTree(currentItem)
        .filter((product) => product.status === _PRODUCT_STATUS.installed)
        .forEach((prod) => {
          prod.channels
            .filter((channel) => channel.status !== _CHANNEL_STATUS.notSynced)
            .forEach((chan) => {
              childProductChannels.push(chan);
            });
        });

      childProductChannelSyncContent = this.channelsStatusSync(childProductChannels, false);
    }

    /** generate product resync button **/
    let resyncActionContent;
    if (this.isInstalled()) {
      const isResyncActionInProgress = this.scheduleResyncInProgress();
      const isResyncProductScheduled = this.productResyncScheduled();
      const isReadonly = this.props.bypassProps.readOnlyMode;

      let conditionalResyncStyleClass;
      let conditionalResyncTitle;

      if (isReadonly) {
        conditionalResyncStyleClass = "text-muted";
        conditionalResyncTitle = t("SCC product catalog refresh in progress");
      } else if (isResyncActionInProgress) {
        conditionalResyncStyleClass = "fa-spin text-muted";
        conditionalResyncTitle = t("Scheduling channels product resync");
      } else if (isResyncProductScheduled) {
        conditionalResyncStyleClass = "text-muted";
        conditionalResyncTitle = t("Channels product resync scheduled");
      } else {
        conditionalResyncStyleClass = "text-info";
        conditionalResyncTitle = t("Schedule channels product resync");
      }

      resyncActionContent = (
        <AsyncButton
          className="btn btn-default btn-sm"
          id="resyncProduct"
          defaultType="btn-default"
          disabled={isReadonly || isResyncActionInProgress || isResyncProductScheduled}
          action={!isReadonly ? this.resyncProduct : undefined}
          icon={isResyncProductScheduled ? "fa-clock-o " : "fa-refresh " + conditionalResyncStyleClass}
          title={conditionalResyncTitle}
        />
      );
    }

    const evenOddClass = this.props.index % 2 === 0 ? "list-row-even" : "list-row-odd";
    const productStatus = this.isInstalled() ? "product-installed" : "";
    return (
      <li className={evenOddClass + " " + (this.isSublistVisible() ? "sublistOpen" : "")} key={currentItem.identifier}>
        <div className={"product-details-wrapper " + productStatus} data-identifier={currentItem.identifier}>
          <CustomDiv
            className="col text-center"
            width={this.props.bypassProps.cols.selector.width}
            um={this.props.bypassProps.cols.selector.um}
          >
            {selectorContent}
          </CustomDiv>
          <CustomDiv
            className="col text-center"
            width={this.props.bypassProps.cols.showSubList.width}
            um={this.props.bypassProps.cols.showSubList.um}
          >
            {showNestedDataIconContent}
          </CustomDiv>
          <CustomDiv
            className="col col-class-calc-width"
            width={this.props.bypassProps.cols.description.width}
            um={this.props.bypassProps.cols.description.um}
          >
            {productDescriptionContent}
          </CustomDiv>
          <CustomDiv
            className="col"
            width={this.props.bypassProps.cols.arch.width}
            um={this.props.bypassProps.cols.arch.um}
            title={t("Architecture")}
          >
            {this.isRootLevel(this.props.treeLevel) ? currentItem.arch : ""}
          </CustomDiv>
          <CustomDiv
            className="col text-right"
            width={this.props.bypassProps.cols.channels.width}
            um={this.props.bypassProps.cols.channels.um}
          >
            {channelSyncContent}
            {childProductChannelSyncContent}
            <ModalLink
              id={"showChannelsFor-" + currentItem.identifier}
              className="showChannels"
              icon="fa-list"
              title={t("Show product's channels")}
              target="show-channels-popup"
              onClick={() => this.props.bypassProps.showChannelsfor(currentItem)}
            />
          </CustomDiv>
          <CustomDiv
            className="col text-right"
            width={this.props.bypassProps.cols.mix.width}
            um={this.props.bypassProps.cols.mix.um}
          >
            {recommendedTogglerContent}
            {resyncActionContent}
          </CustomDiv>
        </div>
        {this.isSublistVisible() ? (
          <CheckList
            data={this.getNestedData(currentItem)}
            bypassProps={this.props.bypassProps}
            handleSelectedItems={this.handleSelectedItems}
            handleUnselectedItems={this.handleUnselectedItems}
            treeLevel={this.props.treeLevel + 1}
            childrenDisabled={
              !(this.isSelected(currentItem, this.props.bypassProps.selectedItems) || this.isInstalled())
            }
          />
        ) : null}
      </li>
    );
  }
}

const ChannelsPopUp = (props) => {
  const titlePopup = t("Product Channels - ") + (props.item != null ? props.item.label : "");
  const contentPopup =
    props.item != null ? (
      <div>
        <ChannelList
          title={t("Mandatory Channels")}
          items={props.item.channels
            .filter((c) => !c.optional)
            .sort((a, b) => a.label.toLowerCase().localeCompare(b.label.toLowerCase()))}
          className={"product-channel-list"}
        />
        <ChannelList
          title={t("Optional Channels")}
          items={props.item.channels
            .filter((c) => c.optional)
            .sort((a, b) => a.label.toLowerCase().localeCompare(b.label.toLowerCase()))}
          className={"product-channel-list"}
        />
      </div>
    ) : null;
  return <PopUp id="show-channels-popup" title={titlePopup} content={contentPopup} className="modal-xs" />;
};

const decodeChannelStatus = (status) => {
  let decoded: React.ReactNode = "";
  switch (status) {
    case _CHANNEL_STATUS.notSynced:
      decoded = <span className="text-muted">{t("not synced")}</span>;
      break;
    case _CHANNEL_STATUS.syncing:
      decoded = <span className="text-info">{t("sync in progress")}</span>;
      break;
    case _CHANNEL_STATUS.synced:
      decoded = <span className="text-success">{t("synced")}</span>;
      break;
    case _CHANNEL_STATUS.failed:
      decoded = <span className="text-danger">{t("sync failed")}</span>;
      break;
  }
  return decoded;
};

const ChannelList = (props) => {
  return props.items.length > 0 ? (
    <div>
      <h4>{props.title}</h4>
      <ul className={props.className}>
        {props.items.map((c) => (
          <li key={c.label}>
            <strong>{c.name}</strong>
            &nbsp;{decodeChannelStatus(c.status)}
            <br />
            {!DEPRECATED_unsafeEquals(c.id, -1) ? (
              <ChannelLink id={c.id} newWindow={true}>
                {c.label}
              </ChannelLink>
            ) : (
              c.label
            )}
          </li>
        ))}
      </ul>
    </div>
  ) : null;
};

export const renderer = () =>
  SpaRenderer.renderNavigationReact(<ProductsPageWrapper />, document.getElementById("products"));
