$(document).ready(function() {
  $.openNav = function(li) {
    li.addClass('open');
    li.children('ul').addClass('open').slideDown(300);
  }
  $.closeNav = function(li) {
    li.removeClass('open');
    li.children('ul').removeClass('open').slideUp(300);
  }
  // nav --> open one level at a time
  $(document).on('click', 'nav li.node > a', function() {
    const li = $(this).parent('li');
    // toggle open/close this
    if (li.hasClass('open')) {
      $.closeNav(li);
    }
    else {
      $.closeNav(li.siblings());
      $.openNav(li);
    }
  });

  // open the active menu after page loaded
  $.setMenu = function() {
    $('nav li').each(function() {
      if ($(this).hasClass('active')) {
        $.openNav($(this));
      }
      else {
        $.closeNav($(this));
      }
    });
  }

  "use strict";

  const React = require("react");
  const ReactDOM = require("react-dom");

  const Link = (props) => <a href={props.url} target={props.target}>{props.image}{props.label}</a>;

  const Element = React.createClass({
    render: function() {
      const isLeaf = this.props.element.submenu == null;
      const image =
        this.props.withImg && this.props.level == 1 ?
          <img src={"images/" + this.props.imgFolder + '/' + this.props.element.label.toLowerCase() + ".png"} /> :
          null;
      return (
        <li className={
          (this.props.element.active ? " active " : "") +
          (isLeaf ? " leaf " : " node ")
          }
        >
          <Link url={this.props.element.submenu ? "#" : this.props.element.url}
            image={image} label={this.props.element.label} target={this.props.element.target} />
          {
            isLeaf ? null :
            <MenuLevel level={this.props.level+1} elements={this.props.element.submenu} />
          }
        </li>
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
          imgFolder={this.props.imgFolder}
          withImg={this.props.withImg}
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

    componentDidMount: function() {
      $.setMenu();
    },

    onSearch: function(e) {
      this.setState({search: e.target.value});
    },

    filterTree: function(fullTree) {
      if (fullTree == null) {
        return [];
      }
      if (this.state.search == null || this.state.search.length == 0) {
        return fullTree;
      }

      var filteredTree = fullTree.map(level =>
          level.label.toLowerCase().includes(this.state.search.toLowerCase()) ||
          this.filterTree(level.submenu).length > 0 ?
          level : null
        );
      return filteredTree.filter(m => m != null);
    },

    render: function() {
      return (
        <nav>
          <div className="nav-search-box">
            <input type="text" className="form-control" name="nav-search" id="nav-search" value={this.state.search}
              onChange={this.onSearch} placeholder="menu filter" />
          </div>
          <MenuLevel level={1} elements={this.filterTree(JSONMenu)} imgFolder='black' withImg={this.props.withImg} />
        </nav>
      );
    }
  });

  ReactDOM.render(
    <Nav withImg={false} />,
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
          {
            breadcrumbArray.map((a, i) => {
              return (
                <span>
                  <span className="level">{a.label}</span>
                  { i == breadcrumbArray.length -1 ? null : <span>></span>}
                </span>
              );
            })
          }
        </div>
      );
    }
  });

  ReactDOM.render(
    <Breadcrumb withImg={false} />,
    document.getElementById('breadcrumb')
  );
});
