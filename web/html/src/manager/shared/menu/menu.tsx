import * as React from "react";

import escapeHtml from "html-react-parser";

import SpaRenderer from "core/spa/spa-renderer";

import { MessagesContainer } from "components/toastr/toastr";

import { flatten } from "utils/jsx";
import { DEPRECATED_unsafeEquals } from "utils/legacy";

type LinkProps = {
  url: string;
  cssClass?: string;
  target?: string;
  title?: string;
  responsiveLabel?: React.ReactNode;
  label?: React.ReactNode;
};

const Link = (props: LinkProps) => (
  <a href={props.url} className={flatten([props.cssClass, "js-spa"])} target={props.target} title={props.title}>
    {props.responsiveLabel}
    {props.label}
  </a>
);

type NodeProps = {
  handleClick: ((event?: React.MouseEvent) => any) | null;
  isLeaf?: boolean;
  icon?: string;
  url: string;
  target: string;
  label: string;
  isSearchActive?: boolean;
  isOpen?: boolean;
};

class Node extends React.Component<NodeProps> {
  handleClick = (event) => {
    // if the click is triggered on a link, do not toggle the menu, just offload the page and go to the requested link
    if (!event.target.href) {
      this.props.handleClick?.();
    }
  };

  render() {
    return (
      <div className={this.props.isLeaf ? "leafLink" : "nodeLink"} onClick={(event) => this.handleClick(event)}>
        {this.props.icon ? <i className={"fa " + this.props.icon}></i> : null}
        <Link url={this.props.url} target={this.props.target} label={escapeHtml(this.props.label)} />
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

class Element extends React.Component<ElementProps> {
  state = {
    open: this.props.element?.active ? true : false,
    visiblityForcedByParent: false,
  };

  UNSAFE_componentWillReceiveProps(nextProps: ElementProps) {
    this.setState({
      open: nextProps.element.active && !nextProps.forceCollapse,
      visiblityForcedByParent: nextProps.visiblityForcedByParent,
    });
  }

  isCurrentVisible = (element, search) => {
    if (search == null || DEPRECATED_unsafeEquals(search.length, 0)) {
      return true;
    }

    return element.label.toLowerCase().includes(search.toLowerCase());
  };

  isVisible = (element, search) => {
    if (search == null || DEPRECATED_unsafeEquals(search.length, 0)) {
      return true;
    }

    const leafVisible = this.isCurrentVisible(element, search);
    const childrenVisible = this.isLeaf(element)
      ? leafVisible
      : element.submenu.filter((l) => this.isVisible(l, search)).length > 0;
    return leafVisible || childrenVisible;
  };

  isLeaf = (element) => {
    return element.submenu == null;
  };

  toggleView = () => {
    this.setState({ open: !this.state.open });
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

class MenuLevel extends React.Component<MenuLevelProps> {
  render() {
    var contentMenu = this.props.elements.map((el, i) => (
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

class Nav extends React.Component {
  state = { search: "", forceCollapse: false };

  onSearch = (e) => {
    this.setState({ search: e.target.value });
  };

  closeEmAll = () => {
    this.setState({ search: "", forceCollapse: true });
  };

  onSPAEndNavigation = () => {
    this.setState({ search: "", forceCollapse: false });
  };

  render() {
    const isSearchActive = this.state.search != null && this.state.search.length > 0;
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
              <i className="fa fa-times-circle-o no-margin" onClick={this.closeEmAll} title={t("Clear Menu")}></i>
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

class Breadcrumb extends React.Component {
  componentDidMount() {}

  onSPAEndNavigation() {
    this.forceUpdate();
  }

  render() {
    var breadcrumbArray: any[] = [];
    var level = window.JSONMenu.find((l) => l.active);
    while (level != null) {
      breadcrumbArray.push(level);
      level = level.submenu ? level.submenu.find((l) => l.active) : null;
    }

    const product_name_link = window._IS_UYUNI ? (
      <Link
        key="home"
        cssClass="navbar-brand"
        url="/"
        label={<span>{t("Uyuni")}</span>}
        target=""
        title={t("Uyuni homepage")}
      />
    ) : (
      <Link
        key="home"
        cssClass="navbar-brand"
        url="/"
        label={
          <span>
            {t("SUSE")}
            <i className="fa fa-registered"></i>
            {t("Manager")}
          </span>
        }
        target=""
        title={t("SUSE Manager homepage")}
      />
    );
    return (
      <div>
        {product_name_link}
        <span>&gt;</span>
        {breadcrumbArray.map((a, i) => {
          return (
            <span key={a.label + "_" + i}>
              <Link
                url={a.submenu ? a.submenu[0].primaryUrl : a.primaryUrl}
                label={escapeHtml(a.label)}
                target={a.target}
              />
              {DEPRECATED_unsafeEquals(i, breadcrumbArray.length - 1) ? null : ">"}
            </span>
          );
        })}
      </div>
    );
  }
}

SpaRenderer.renderGlobalReact(<Breadcrumb />, document.getElementById("breadcrumb"));

SpaRenderer.renderGlobalReact(
  <>
    <MessagesContainer containerId="global" />
  </>,
  document.getElementById("messages-container")
);
