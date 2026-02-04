import { type MouseEvent, type ReactNode, Component } from "react";

import SpaRenderer from "core/spa/spa-renderer";
import { isUyuni } from "core/user-preferences";

import { MessagesContainer } from "components/toastr/toastr";
import { DEPRECATED_onClick } from "components/utils";

import { stringToReact } from "utils";
import { flatten } from "utils/jsx";
import { DEPRECATED_unsafeEquals } from "utils/legacy";

import styles from "./menu.module.scss";

type LinkProps = {
  url: string;
  className?: string;
  target?: string;
  title?: string;
  responsiveLabel?: ReactNode;
  label?: ReactNode;
  betaBadge?: ReactNode;
};

const Link = (props: LinkProps) => (
  <a href={props.url} className={flatten([props.className, "js-spa"])} target={props.target} title={props.title}>
    {props.responsiveLabel}
    {props.label}
    {props.betaBadge}
  </a>
);

type NodeProps = {
  handleClick: ((event?: MouseEvent) => any) | null;
  isLeaf?: boolean;
  icon?: string;
  url: string;
  target: string;
  label: string;
  isSearchActive?: boolean;
  isOpen?: boolean;
  isBeta?: boolean;
};

class Node extends Component<NodeProps> {
  handleClick = (event) => {
    // if the click is triggered on a link, do not toggle the menu, just offload the page and go to the requested link
    if (!event.target.href) {
      this.props.handleClick?.();
    }
  };

  render() {
    const betaBadge = this.props.isBeta ? <span className="menu-beta-badge">BETA</span> : null;

    return (
      <div
        className={this.props.isLeaf ? "leafLink" : "nodeLink"}
        {...DEPRECATED_onClick((event) => this.handleClick(event))}
        role="button"
      >
        {this.props.icon ? <i className={"fa " + this.props.icon}></i> : null}
        <Link
          url={this.props.url}
          target={this.props.target}
          label={stringToReact(this.props.label)}
          betaBadge={betaBadge}
        />
        {this.props.isLeaf ? null : !this.props.isSearchActive ? (
          <i className={"submenuIcon " + (this.props.isOpen ? "fa fa-angle-up" : "fa fa-angle-down")}></i>
        ) : null}
      </div>
    );
  }
}

type ElementProps = {
  element?: any;
  searchString?: any;
  level: any;
  visiblityForcedByParent: any;
  forceCollapse?: any;
};

class ElementState {
  open: boolean;
  visiblityForcedByParent = false;

  constructor(props: ElementProps) {
    this.open = props.element?.active ? true : false;
  }
}

class Element extends Component<ElementProps, ElementState> {
  state: ElementState;

  constructor(props: ElementProps) {
    super(props);
    this.state = new ElementState(props);
  }

  UNSAFE_componentWillReceiveProps(nextProps: ElementProps) {
    this.setState({
      open: nextProps.element.active && !nextProps.forceCollapse,
      visiblityForcedByParent: nextProps.visiblityForcedByParent,
    });
  }

  isCurrentVisible = (element, search) => {
    if (DEPRECATED_unsafeEquals(search, null) || DEPRECATED_unsafeEquals(search.length, 0)) {
      return true;
    }

    return element.label.toLowerCase().includes(search.toLowerCase());
  };

  isVisible = (element, search) => {
    if (DEPRECATED_unsafeEquals(search, null) || DEPRECATED_unsafeEquals(search.length, 0)) {
      return true;
    }

    const leafVisible = this.isCurrentVisible(element, search);
    const childrenVisible = this.isLeaf(element)
      ? leafVisible
      : element.submenu.filter((l) => this.isVisible(l, search)).length > 0;
    return leafVisible || childrenVisible;
  };

  isLeaf = (element) => {
    return DEPRECATED_unsafeEquals(element.submenu, null);
  };

  toggleView = () => {
    this.setState((prevState) => ({ open: !prevState.open }));
  };

  getUrl = (element) => {
    return element.submenu ? this.getUrl(element.submenu[0]) : element.primaryUrl;
  };

  render() {
    const element = this.props.element;
    const className = flatten([
      element.active && "active",
      (this.state.open || this.state.visiblityForcedByParent) && "open",
      this.isLeaf(element) ? "leaf" : "node",
    ]);
    return this.isVisible(element, this.props.searchString) || this.state.visiblityForcedByParent ? (
      <li className={className}>
        <Node
          isLeaf={this.isLeaf(element)}
          url={this.getUrl(element)}
          label={element.label}
          target={element.target}
          handleClick={this.isLeaf(element) ? null : this.toggleView}
          isOpen={this.state.open}
          isSearchActive={this.props.searchString}
          icon={element.icon}
          isBeta={element.isBeta}
        />
        {this.isLeaf(element) ? null : (
          <MenuLevel
            level={this.props.level + 1}
            elements={element.submenu}
            searchString={this.props.searchString}
            visiblityForcedByParent={
              this.state.visiblityForcedByParent ||
              (this.props.searchString && this.isCurrentVisible(element, this.props.searchString))
            }
          />
        )}
      </li>
    ) : null;
  }
}

type MenuLevelProps = {
  elements: any[];
  level: any;
  searchString: any;
  visiblityForcedByParent?: any;
  forceCollapse?: any;
};

class MenuLevel extends Component<MenuLevelProps> {
  render() {
    const contentMenu = this.props.elements.map((el, i) => (
      <Element
        element={el}
        key={this.props.level + "_" + el.label + "_" + i}
        level={this.props.level}
        searchString={this.props.searchString}
        visiblityForcedByParent={this.props.visiblityForcedByParent}
        forceCollapse={this.props.forceCollapse}
      />
    ));
    return <ul className={"level" + this.props.level}>{contentMenu}</ul>;
  }
}

class Nav extends Component {
  state = { search: "", forceCollapse: false };

  onSearch = (e) => {
    this.setState({ search: e.target.value });
  };

  closeAll = () => {
    this.setState({ search: "", forceCollapse: true });
  };

  onSPAEndNavigation = () => {
    this.setState({ search: "", forceCollapse: false });
  };

  render() {
    const isSearchActive = !DEPRECATED_unsafeEquals(this.state.search, null) && this.state.search.length > 0;
    return (
      <nav className={isSearchActive ? "" : "collapsed"}>
        <div className="nav-tool-box">
          <input
            type="text"
            className="form-control"
            name="nav-search"
            id="nav-search"
            value={this.state.search}
            onChange={this.onSearch}
            placeholder="Search page"
          />
          <span className={"input-right-icon " + (isSearchActive ? "clear" : "")}>
            {isSearchActive ? (
              <i
                className="fa fa-times-circle-o no-margin"
                {...DEPRECATED_onClick(this.closeAll)}
                title={t("Clear Menu")}
              ></i>
            ) : (
              <i className="fa fa-search no-margin" title={t("Filter menu")}></i>
            )}
          </span>
        </div>
        <MenuLevel
          level={1}
          elements={window.JSONMenu}
          searchString={this.state.search}
          forceCollapse={this.state.forceCollapse}
        />
      </nav>
    );
  }
}

SpaRenderer.renderGlobalReact(<Nav />, document.getElementById("nav"));

class Breadcrumb extends Component {
  onSPAEndNavigation() {
    this.forceUpdate();
  }

  render() {
    const breadcrumbArray: any[] = [];
    let level = window.JSONMenu.find((l) => l.active);
    while (!DEPRECATED_unsafeEquals(level, null)) {
      breadcrumbArray.push(level);
      level = level.submenu ? level.submenu.find((l) => l.active) : null;
    }

    const product_name_link = isUyuni ? (
      <Link
        key="home"
        className="navbar-brand"
        url="/"
        label={<span>{t("Uyuni")}</span>}
        target=""
        title={t("Uyuni homepage")}
      />
    ) : (
      <Link
        key="home"
        className="navbar-brand"
        url="/"
        label={
          <span>
            SUSE<i className="fa fa-registered"></i> Multi-Linux Manager
          </span>
        }
        target=""
        title={t("SUSE Multi-Linux Manager homepage")}
      />
    );

    return (
      <div className={styles.breadcrumb}>
        {product_name_link}
        <span className="menu-link">&gt;</span>
        {breadcrumbArray.map((a, i) => {
          return (
            <span key={a.label + "_" + i}>
              <Link
                className="menu-link"
                url={a.submenu ? a.submenu[0].primaryUrl : a.primaryUrl}
                label={stringToReact(a.label)}
                target={a.target}
              />
              {i === breadcrumbArray.length - 1 ? null : <span className="menu-link">&gt;</span>}
            </span>
          );
        })}
      </div>
    );
  }
}

SpaRenderer.renderGlobalReact(<Breadcrumb />, document.getElementById("breadcrumb"));

SpaRenderer.renderGlobalReact(
  <MessagesContainer containerId="global" />,
  document.getElementById("messages-container")
);
