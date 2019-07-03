$(document).on("ready", function(){

  /*
   * System Set Manager: actions to hide the SSM toolbar
   * when the Clear button is pressed or when
   * no system is selected
   */
  $(document).on('click', '#clear-ssm-btn', function() {
    hidesystemtool();
  });
  function hidesystemtool(){
    $(".spacewalk-bar").animate({
      "right": "-=50px",
      "opacity": "0"},
      300, function() {
      /* after animation is complete we hide the element */
      $(this).hide();
    });
  }
  // See if there is a system already selected as soon as the page loads
  updateSsmToolbarOpacity();

  // This is a function from spacewalk-checkall.js
  create_checkall_checkbox();

  // Wrapping the tables in a div which will make them responsive
  $(".table").wrap("<div class='table-responsive'>");

  // Set up the behavior and the event function
  // for the spacewalk section toolbar [sst]
  handleSst();

  // Show character length for textarea
  addTextareaLengthNotification();

  scrollTopBehavior();
});


function adaptFluidColLayout() {
  $('.col-class-calc-width').each(function() {
    var totalWidth = $(this).parent().width();
    $(this).siblings('.col').each(function() {
      totalWidth = Math.floor(totalWidth - $(this).outerWidth());
    });
    $(this).css('width', totalWidth - 10);
  });
}

/* Getting the screen size to create a fixed padding-bottom in the Section tag to make both columns the same size */
// On window load
$(window).load(function () {
  adjustDistanceForFixedHeader();
  columnHeight();
});

// On window resize
$(window).resize(function () {
  alignContentDimensions();
});

// On section#spacewalk-content scroll
function scrollTopBehavior() {
  $(window).scroll(function() {
    if($(this).scrollTop() > 100) {
      $('#scroll-top').show();
    } else {
      $('#scroll-top').hide();
    }

    sstScrollBehavior();
  });

  $(document).on('click', '#scroll-top', function() {
    window.scrollTo(0,0);
  });
}


// A container function for what should be fired
// to set HTML tag dimensions
function alignContentDimensions() {
  adaptFluidColLayout();
  adjustDistanceForFixedHeader();
  sstStyle();
  columnHeight();
}

// empty function by default hooked on window.scroll event
var sstScrollBehavior = function() {
  return;
}

var sstScrollBehaviorSetupIsDone = false; // flag to implement the function one time only
function sstScrollBehaviorSetup(sst) {
  sstScrollBehaviorSetupIsDone = true;
  const adjustSpaceObject = $('<div>').height(sst.outerHeight());
  var fixedTop = sst.offset().top;

  // override the empty function hooked on window.scroll event                                              │
  sstScrollBehavior = function() {
    var currentScroll = $(window).scrollTop();
    if (currentScroll >= fixedTop) {
      sst.after(adjustSpaceObject);
      $(sst).addClass('fixed');
    } else {
      $(sst).removeClass('fixed');
      adjustSpaceObject.remove();
    }
    sstStyle();
  }
}

// when the page scrolls down and the toolbar is going up and hidden,
// the toolbar takes a fixed place right below the header bar
function handleSst() {
  var sst = $('.spacewalk-section-toolbar');

  if ($('.move-to-fixed-toolbar').length > 0) {
    // if there is no 'spacewalk-section-toolbar', then create it
    if (sst.length == 0) {
      sst = $('<div class="spacewalk-section-toolbar">');
      $('.spacewalk-list.list').before(sst);
    }

    // move each named tag into the 'spacewalk-section-toolbar'
    $('.move-to-fixed-toolbar').each(function() {
      sst.append($(this));
      $(this).removeClass('move-to-fixed-toolbar');
    });
  }

  // move children of each named tag
  // into the 'spacewalk-section-toolbar > action-button-wrapper'
  if ($('.move-children-to-fixed-toolbar').length > 0) {
    // if there is no 'spacewalk-section-toolbar', then create it
    if (sst.length == 0) {
      sst = $('<div class="spacewalk-section-toolbar">');
      $('.spacewalk-list.list').before(sst);
    }
    var selectorButtonWrapper = $('.selector-button-wrapper');
    // if there is no 'action-button-wrapper', then create it
    if (selectorButtonWrapper.length == 0) {
      selectorButtonWrapper = $('<div class="selector-button-wrapper">');
      sst.prepend(selectorButtonWrapper);
    }
    $('.move-children-to-fixed-toolbar').each(function() {
      selectorButtonWrapper.append($(this).children());
      $(this).removeClass('move-children-to-fixed-toolbar');
    });
  }

  // setup the function only if there is the 'spacewalk-section-toolbar'
  // and the function is not yet setup
  if (!sstScrollBehaviorSetupIsDone && sst.length > 0) {
    sstScrollBehaviorSetup(sst);
  }
}

function sstStyle() {
  var sst = $('.spacewalk-section-toolbar');
  if (sst.hasClass('fixed')) {
    sst.css({
      top: $('header').outerHeight() - 1,
      left: $('section').offset().left,
      'min-width': $('section').outerWidth()
    });
  }
  else {
    sst.css({
      'min-width': 0
    });
  }
  sst.width(sst.parent().width() - sst.css('padding-left') - sst.css('padding-right'));
}

// Header is fixed, the main content column needs
// padding-top equals the header height to be fully visible
function adjustDistanceForFixedHeader() {
  // subtract 1px in case the outerHeight comes to us already upper rounded
  $('body').css('padding-top', $('header').outerHeight());
}

// Make columns 100% in height
function columnHeight() {
  const aside = $('.spacewalk-main-column-layout aside');
  const navToolBox = $('.spacewalk-main-column-layout aside .nav-tool-box');
  const headerHeight = $('header').outerHeight();
  const footerHeight = $('footer').outerHeight();
  const winHeight = $(window).height();
  // // Column height should equal the window height minus the header and footer height
  aside.css('height', winHeight - headerHeight);
  // aside.css('padding-bottom', footerHeight);
  const nav = $('.spacewalk-main-column-layout aside #nav nav ul.level1');
  nav.css('height', aside.outerHeight() - navToolBox.outerHeight() - footerHeight);
};

$(document).on('click', '.navbar-toggle', function() {
  $('aside').toggleClass('in');
  $('aside').toggleClass('collapse');
  columnHeight();
});

// returns an object that can be passed to DWR renderer as a callback
// puts rendered HTML in #divId, opens an alert with the same text if
// debug is true
function makeRendererHandler(divId, debug) {
  return makeAjaxHandler(function(text) {
    if (debug) {
      alert(text);
    }
    $('#' + divId).html(text);
    $('#' + divId).fadeIn();
    columnHeight();
  });
}

// returns an object that can be passed to DWR as a callback
// callbackFunction: function to call when AJAX requests succeeds
// errorHandlerFunction: function to call when AJAX requests fail
// (can be omitted for showFatalError)
// works around a DWR bug calling errorHandler when navigating away
// from a page during an AJAX request
function makeAjaxHandler(callbackFunction, errorHandlerFunction) {
    errorHandlerFunction = typeof errorHandlerFunction !== "undefined" ?
      errorHandlerFunction : showFatalError;

    // workaround to a DWR bug that calls errorHandler when user
    // navigates away from page during an AJAX call
    // first, we detect page unloading
    $(window).on("beforeunload", function() {
      $.unloading = true;
    });
    return {
      callback: callbackFunction,
      errorHandler: function(message, exception) {
        // second, if we get an error during unloading we ignore it
        if ($.unloading == true) {
          console.log("Ignoring exception " + exception + " with message " + message + " because it is a DWR error during unload");
        }
        else {
          errorHandlerFunction(message, exception);
        }
      }
    }
}

// shows a fatal DWR/AJAX error
function showFatalError(message, exception) {
  console.log("DWR AJAX call failed with message: " + message);
  console.log(exception);
  alert("Unexpected error, please reload the page and check server logs.");
}


// Extension to Twitter Bootstrap.
// Gives you a col-XX-auto class like Bootstrap
// That dynamically adjust the grid for the columns to take
// as much space as possible while still being responsive
// So three col-md-auto would get col-md-4 each.
// Five col-md-auto would get two with col-md-3 and three with col-md-2
$(document).on("ready", function() {
  $.each(['xs', 'sm', 'md', 'lg'], function(idx, gridSize) {
    //for each div with class row
    $('.col-' + gridSize + '-auto:first').parent().each(function() {
      //we count the number of childrens with class col-md-6
      var numberOfCols = $(this).children('.col-'  + gridSize + '-auto').length;
      if (numberOfCols > 0 && numberOfCols < 13) {
        minSpan = Math.floor(12 / numberOfCols);
        remainder = (12 % numberOfCols);
        $(this).children('.col-' + gridSize + '-auto').each(function(idx, col) {
          var width = minSpan;
          if (remainder > 0) {
            width += 1;
            remainder--;
          }
          $(this).addClass('col-' + gridSize + '-' + width);
        });
      }
    });
  });
});


// Put the focus on a given form element
function formFocus(form, name) {
  var focusControl = document.forms[form].elements[name];
  if (focusControl.type != "hidden" && !focusControl.disabled) {
     focusControl.focus();
  }
}

// Humanizes all the time elements with the human class
function humanizeDates() {
  $("time.human-from, time.human-calendar").each(function (index) {
    var datetime = $(this).attr('datetime');
    if (datetime == undefined) {
      // if the attribute is not set, the content
      // should be a valid date
      datetime = $(this).html();
    }
    var parsed = moment(datetime);
    if (parsed.isValid()) {
      var originalContent = $(this).html();
      if ($(this).hasClass("human-from")) {
        var ref = $(this).attr("data-reference-date");
        if (ref) {
          var refParsed = moment(ref);
          if (refParsed.isValid()) {
            $(this).html(parsed.from(refParsed));
          }
        }
        else {
          $(this).html(parsed.fromNow());
        }
      }
      if ($(this).hasClass("human-calendar")) {
        $(this).html(parsed.calendar());
      }
      // if the original did not had a datetime attribute, add it
      var datetimeAttr = $(this).attr('datetime');
      if (datetimeAttr == undefined) {
        $(this).attr('datetime', datetime);
      }
      // add a tooltip
      $(this).attr('title', originalContent);
    }
  });
}

$(document).on("ready", function() {
  humanizeDates();
});

/**
 * Setups ACE editor in a textarea element
 * textarea is a jQuery object
 * mode is the language mode, if emmpty
 * shows a select box to choose it.
 */
function setupTextareaEditor(textarea, mode) {
  // if textarea is not shown, the height will be negative,
  // so we set the height of the editor in the popup to the 70% of the window height
  var tH = textarea.height() > 0 ? textarea.height() : ($(window).height()  * 0.7);

  var editDiv = $('<div>', {
      position: 'absolute',
      width: textarea.width(),
      height: tH,
      'class': textarea.attr('class')
  }).attr('id', textarea.attr('id') + '-editor').insertBefore(textarea);

  var toolBar = $('<div></div>').insertBefore(editDiv[0]);
  textarea.hide();

  var editor = ace.edit(editDiv[0]);
  editor.getSession().setValue(textarea.val());
  editor.getSession().setOptions({ tabSize: 4, useSoftTabs: true });

  editor.setTheme("ace/theme/xcode");
  editor.getSession().setMode("ace/mode/sh");

  // before submitting the code, the textarea
  // should be updated with the editor value
  textarea.closest('form').submit(function () {
      textarea.val(editor.getSession().getValue());
  })

  toolBar.addClass('ace_editor');
  toolBar.css('width', editDiv.css('width'));
  var modeSel = $('<select> \
    <option selected value="sh">Shell</option> \
    <option value="xml">XML</option> \
    <option value="ruby">Ruby</option> \
    <option value="python">Python</option> \
    <option value="perl">perl</option> \
    <option value="yaml">Yaml</option> \
    </select>');
  modeSel.find('option').each(function() {
  if ($(this).text() == mode)
    $(this).attr('selected', 'selected');
  });

  toolBar.append(modeSel);
  if (mode != "") {
    editor.getSession().setMode("ace/mode/" + mode);
    toolBar.hide();
  }

  modeSel.change(function () {
    editor.getSession().setMode("ace/mode/" + $(this).val());
  });

  // Set editor to read only according to data attribute
  editor.setReadOnly(textarea.data('readonly') || textarea.attr('readonly'));

  editor.getSession().on('change', function() {
    textarea.val(editor.getSession().getValue());
  });
}

/**
 * setups every textarea with data-editor attribute
 * set to some language with an ACE editor
 */
$(function () {
  $('textarea[data-editor]').each(function () {
    var textarea = $(this);
    var mode = textarea.data('editor');
    setupTextareaEditor(textarea, mode);
  });
});

// Disables the enter key from submitting the form
function disableEnterKey() {
  $(window).keydown(function(event){
    if(event.keyCode == 13) {
      event.preventDefault();
      return false;
    }
  });
}

// Binds the enter key to a specific submit button on a key event
function enterKeyHandler(event, $button) {
    if(event.keyCode == 13) {
        $button.click();
        return false;
    }
}

/**
 * Translates a string, implemented now as a 'true-bypass',
 * with placeholder replacement like Java's MessageFormat class.
 * Accepts any number of arguments after key.
 */
function t(key) {
  var result = key;

  // Minimal implementation of https://docs.oracle.com/javase/7/docs/api/java/text/MessageFormat.html
  for (var i=1; i<arguments.length; i++) {
    result = result.replace('{' + (i-1) + '}', arguments[i]);
  }

  return result;
}

/*
* Create an Observer object that monitors if something in the HTML has changes,
* if that happens it fires the window resize computation event
* https://developer.mozilla.org/en-US/docs/Web/API/MutationObserver
*/
// create an observer instance
var spacewalkContentObserver = new MutationObserver(function(mutations) {
    if (mutations.length > 0) {
        alignContentDimensions();
        // trigger the 'spacewalk-section-toolbar' (sst) handler on content change
        // since it can happen that the sst has just been added right now
        handleSst();
    }
});

$(document).ready(function() {
  var target = document.getElementById('spacewalk-content');
  // configuration of the observer:
  var config = { childList: true, characterData: true, subtree: true };
  // pass in the target node, as well as the observer options
  spacewalkContentObserver.observe(target, config);
});

$(document).on('click', '.toggle-box', function() {
  if ($(this).hasClass('open')) {
    $(this).removeClass('open');
  }
  else {
    $('.toggle-box.open').trigger('click');
    $(this).addClass('open');
  }
  $(this).blur(); // remove the focus
})

// focus go away from the menu or the nav menu
$(document).click(function (e) {
  var target = $(e.target);
  // if a toggle-box button is active and the current click
  // is not on its related box, trigger a close for it
  $('.toggle-box.open').each(function() {
    var toggleButton = $(this);
    var toggleBox = toggleButton.parent();
    if (!target.closest(toggleBox).length) {
      toggleButton.trigger('click');
    }
  });
});

$(document).on('click', '.navbar-toggle', function() {
  $('aside').toggle();
});

/* prevent jumping to the top of the page because
of an <a href> tag that is actually not a link */
$(document).on('click', 'a', function(e) {
  const href = $(this).attr('href');
  if (href != null && href.length == 1 && href == '#') {
    e.preventDefault();
    $(this).blur(); // remove the focus
  }
});

/*
* Check if the field contains the allowed values only
*/
$(document).on('keyup change', '.activationKey-check', function(e) {
  if ($(this).val().match(/([^a-zA-Z0-9-_.])/g)) {
    $(this).parent().addClass('has-error');
  }
  else {
    $(this).parent().removeClass('has-error');
  }
});

function addTextareaLengthNotification() {
  // Add a notification text of the remaining length for a textarea
  $('textarea.with-maxlength').each(function() {
    const textareaId = $(this).attr('id');
    $(this).after(
      $('<div/>')
        .attr("id", "newDiv1")
        .addClass("remaining-length-wrapper text-right")
        .html(
          $('<span/>')
            .html([
              $('<span/>')
              .attr("id", textareaId + '-remaining-length')
              .text($(this).attr('maxlength') - $(this).val().length)
              , $('<span/>').text(' ' + t('remaining'))
            ])
        )
    );
  });

  // Update the remaining length text of the related textarea
  $(document).on('input', 'textarea.with-maxlength', function() {
    $('#' + $(this).attr('id') + '-remaining-length')
      .html($(this).attr('maxlength') - $(this).val().length);
  });
}
