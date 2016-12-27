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
      // $.closeNav(li.siblings());
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

  const Link = (props) =>
    <a href={props.url} target={props.target}>
      <span>{props.label}</span>
    </a>;

  const Element = React.createClass({
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

    render: function() {
      return (
        this.isVisible(this.props.element, this.props.searchString) ?
          <li className={
            (this.props.element.active ? " active " : "") +
            (this.isLeaf(this.props.element) ? " leaf " : " node ")
            }
          >
            <Link url={this.props.element.submenu ? "#" : this.props.element.url}
                label={this.props.element.label} target={this.props.element.target} />
            {
              this.isLeaf(this.props.element) ? null :
              <MenuLevel level={this.props.level+1} elements={this.props.element.submenu}
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

    componentDidMount: function() {
      $.setMenu(this.state.search);
    },

    onSearch: function(e) {
      this.setState({ search: e.target.value });
    },

    render: function() {
      return (
        <nav className={this.state.search != null && this.state.search.length > 0 ? '' : 'collapsed'}>
          <div className="nav-search-box">
            <input type="text" className="form-control" name="nav-search" id="nav-search" value={this.state.search}
              onChange={this.onSearch} placeholder="menu filter" />
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
                <Link key={a.label + '_' + i} url={a.submenu[0].url}
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
});
