"use strict";

const React = require("react");
const ReactDOM = require("react-dom");

const Link = (props) =>
  <a href={props.url} target={props.target}>
    {
      props.icon ?
      <i className={'fa ' + props.icon}></i>
      : null
    }
    {props.label}
  </a>
  ;

const NodeLink = (props) =>
  <div className={props.isLeaf ? " leafLink " : " nodeLink "}>
    {
      !props.isLeaf ?
      <div className="showSubMenu" onClick={props.handleClick}>
        <i className={props.isOpen ? "fa fa-minus-square" : "fa fa-plus-square"}></i>
      </div>
      : null
    }
    {props.children}
  </div>;

const Element = React.createClass({
  getInitialState: function() {
    return {
      open: (this.props.element.active ? true : false)
    }
  },

  componentWillReceiveProps: function(nextProps) {
    this.setState({open: nextProps.element.open});
  },

  isVisible: function(element, search) {
    if (search == null || search.length == 0) {
      return true;
    }

    if (this.isLeaf(element)) {
      return element.label.toLowerCase().includes(search.toLowerCase());
    }

    return element.submenu.filter(l => this.isVisible(l, search)).length > 0;
  },

  isLeaf: function(element) {
    return element.submenu == null;
  },

  toggleView: function() {
    this.setState({open : !this.state.open});
  },

  getUrl: function(element) {
    return element.submenu ? this.getUrl(element.submenu[0]) : element.primaryUrl;
  },

  render: function() {
    const element = this.props.element;
    return (
      this.isVisible(element, this.props.searchString) ?
        <li className={
          (element.active ? " active" : "") +
          (this.state.open ? " open " : "") +
          (this.isLeaf(element) ? " leaf " : " node ")
          }
        >
          <NodeLink isLeaf={this.isLeaf(element)}
              handleClick={this.isLeaf(element) ? null : this.toggleView}
              isOpen={this.state.open}>
              <Link url={this.getUrl(element)}
                  label={element.label} target={element.target} icon={element.icon} />
          </NodeLink>
          {
            this.isLeaf(element) ? null :
            <MenuLevel level={this.props.level+1} elements={element.submenu}
                searchString={this.props.searchString} />
          }
        </li>
      : null
    );
  }
});

const MenuLevel = React.createClass({
  render: function() {
    var length = this.props.elements.length;
    var contentMenu = this.props.elements.map((el) =>
      <Element element={el}
        key={el.label + '_' + this.props.level}
        level={this.props.level}
        searchString={this.props.searchString}
      />
    );
    return (
      <ul className={"level" + this.props.level}>
        {contentMenu}
      </ul>
    );
  }
});

const Nav = React.createClass({
  getInitialState: function () {
    return {search: ''}
  },

  onSearch: function(e) {
    this.setState({ search: e.target.value });
  },

  render: function() {
    return (
      <nav className={this.state.search != null && this.state.search.length > 0 ? '' : 'collapsed'}>
        <div className="nav-search-box">
          <input type="text" className="form-control" name="nav-search" id="nav-search" value={this.state.search}
            onChange={this.onSearch} placeholder="Search page" />
        </div>
        <MenuLevel level={1} elements={JSONMenu} searchString={this.state.search} />
      </nav>
    );
  }
});

ReactDOM.render(
  <Nav />,
  document.getElementById('nav')
);


const Breadcrumb = React.createClass({
  componentDidMount: function() {
  },

  render: function() {
    var breadcrumbArray = Array();
    var level = JSONMenu.find(l => l.active);
    while (level != null) {
      breadcrumbArray.push(level);
      level = level.submenu ? level.submenu.find(l => l.active) : null;
    }
    return (
      <div>
        <Link key='home' url='/' label={<i className='fa fa-home'></i>} target='' />
        <span>></span>
        {
          breadcrumbArray.map((a, i) => {
            const htmlElement =
            (
              a.submenu ?
              <Link key={a.label + '_' + i} url={a.submenu[0].primaryUrl}
                  label={a.label} target={a.target} />
              :
              <span className="level">{a.label}</span>
            );
            return (
              <span>{htmlElement}{ i == breadcrumbArray.length -1 ? null : <span>></span>}</span>
            );
          })
        }
      </div>
    );
  }
});

ReactDOM.render(
  <Breadcrumb />,
  document.getElementById('breadcrumb')
);
