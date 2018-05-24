"use strict";

const React = require("react");
const ReactDOM = require("react-dom");

const Link = (props) =>
  <a href={props.url} className={props.cssClass} target={props.target}
    title={props.title} onClick={props.handleClick}>
    {props.preIcon ? <i className={'fa ' + props.preIcon}></i> : null}
    {
      props.icon ?
      <i className={'fa ' + props.icon}></i>
      : null
    }
    {props.responsiveLabel}
    {props.label}
  </a>
  ;

const NodeLink = (props) =>
  <div className={props.isLeaf ? " leafLink " : " nodeLink "} >
    {
      props.isLeaf ?
      <Link url={props.url} target={props.target} label={props.label} />
      : <Link url="#" target={props.target} cssClass="node-text" handleClick={props.handleClick}
          label={props.label} icon={props.icon}
          preIcon={ !props.isSearchActive ?
            'submenuIcon ' + (props.isOpen ? "fa fa-angle-down" : "fa fa-angle-right")
            : null
          }/>
    }
    {
      !props.isLeaf ?
      <Link url={props.url} title={props.completeUrlLabel}
        label={<i className="fa fa-dot-circle-o"></i>}
        cssClass="direct-link" target={props.target} />
      : null
    }
  </div>;

const Element = React.createClass({
  getInitialState: function() {
    return {
      open: (this.props.element.active ? true : false),
      visiblityForcedByParent: false
    }
  },

  componentWillReceiveProps: function(nextProps) {
    this.setState({
      open: nextProps.element.open && !nextProps.forceCollapse,
      visiblityForcedByParent: nextProps.visiblityForcedByParent
    });
  },

  isCurrentVisible(element, search) {
    if (search == null || search.length == 0) {
      return true;
    }

    return element.label.toLowerCase().includes(search.toLowerCase());
  },

  isVisible: function(element, search) {
    if (search == null || search.length == 0) {
      return true;
    }

    const leafVisible = this.isCurrentVisible(element, search);
    const childrenVisible = this.isLeaf(element) ? leafVisible : element.submenu.filter(l => this.isVisible(l, search)).length > 0;
    return leafVisible || childrenVisible;
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

  getCompleteUrlLabel: function(element) {
    return element.submenu ? (element.label + ' > ' + this.getCompleteUrlLabel(element.submenu[0])) : element.label;
  },

  render: function() {
    const element = this.props.element;
    return (
      this.isVisible(element, this.props.searchString) || this.state.visiblityForcedByParent ?
        <li className={
          (element.active ? " active" : "") +
          (this.state.open || this.state.visiblityForcedByParent ? " open " : "") +
          (this.isLeaf(element) ? " leaf " : " node ")
          }
        >
          <NodeLink isLeaf={this.isLeaf(element)} url={this.getUrl(element)}
              label={element.label} target={element.target}
              completeUrlLabel={this.getCompleteUrlLabel(element)}
              handleClick={this.isLeaf(element) ? null : this.toggleView}
              isOpen={this.state.open} isSearchActive={this.props.searchString}
              icon={element.icon}
          />
          {
            this.isLeaf(element) ? null :
            <MenuLevel level={this.props.level+1} elements={element.submenu}
                searchString={this.props.searchString}
                visiblityForcedByParent={this.state.visiblityForcedByParent ||
                  (this.props.searchString && this.isCurrentVisible(element, this.props.searchString))}
            />
          }
        </li>
      : null
    );
  }
});

const MenuLevel = React.createClass({
  render: function() {
    var length = this.props.elements.length;
    var contentMenu = this.props.elements.map((el, i) =>
      <Element element={el}
        key={this.props.level + '_' + el.label + '_' + i}
        level={this.props.level}
        searchString={this.props.searchString}
        visiblityForcedByParent={this.props.visiblityForcedByParent}
        forceCollapse={this.props.forceCollapse}
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
    return {search: '', forceCollapse: false}
  },

  onSearch: function(e) {
    this.setState({ search: e.target.value });
  },

  closeEmAll: function() {
    this.setState({search: '', forceCollapse: true});
  },

  scrollToTop: function() {
    window.scrollTo(0, 0);
  },

  render: function() {
    return (
      <nav className={this.state.search != null && this.state.search.length > 0 ? '' : 'collapsed'}>
        <div className="nav-tool-box">
          <input type="text" className="form-control" name="nav-search" id="nav-search" value={this.state.search}
            onChange={this.onSearch} placeholder="Search page" />
          <span className="clear">
            <i className="fa fa-times-circle-o no-margin" onClick={this.closeEmAll} title={t('Clear Menu')}></i>
          </span>
        </div>
        <MenuLevel level={1} elements={JSONMenu} searchString={this.state.search} forceCollapse={this.state.forceCollapse} />
        <button id="scroll-top" onClick={this.scrollToTop}><i className='fa fa-angle-up'></i></button>
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

    const product_name_link =
      _IS_UYUNI ?
        <Link key='home' cssClass="navbar-brand" url='/'
            responsiveLabel={<i className='fa fa-home' title="Uyuni homepage"></i>}
            label={<span>Uyuni</span>}
            target=''
            title={t("Uyuni homepage")}
          />
        :
        <Link key='home' cssClass="navbar-brand" url='/'
          responsiveLabel={<i className='fa fa-home' title="SUSE Manager homepage"></i>}
          label={<span>SUSE<i className="fa fa-registered"></i>Manager</span>}
          target=''
          title={t("SUSE Manager homepage")}
        />
    ;
    return (
      <div>
        {product_name_link}
        <span>></span>
        {
          breadcrumbArray.map((a, i) => {
            const htmlElement =
            (
              a.submenu ?
              <Link url={a.submenu[0].primaryUrl}
                  label={a.label} target={a.target} />
              :
              <span className="level">{a.label}</span>
            );
            return (
              <span key={a.label + '_' + i}>{htmlElement}{ i == breadcrumbArray.length -1 ? null : <span>></span>}</span>
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
