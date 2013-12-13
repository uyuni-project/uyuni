$(document).on("ready", function(){

  /* Menu in the left column - actions to hide submenu and create animation when a 
  menu only has submenues and doesnt have a URL */
  $("#sidenav ul>ul").hide();

  $("#sidenav li.active").each(function() {
    $(this).next("ul").show();
  });

  /* Systems Selected Toolbar - actions to hide the toolbar when th Clear button is pressed or when 
  no system is selected */

  $("#clear-btn").click(hidesystemtool);

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

});

/* Getting the screen size to create a fixed padding-bottom in the Section tag to make both columns the same size */
// On window load
$(window).load(function () {
  columnHeight();
});

// On window resize
$(window).resize( function () {
  $(".spacewalk-main-column-layout section").css("padding-bottom", "");
  columnHeight();
});

// Make columns 100% in height
function columnHeight() {
  //Only if the screen size is higher than the max-width set up in the Variables.less under the definition @screen-md: 
  //PLEASE: update this if you change the content of @screen-md
  if ($(document).width()>992) {
    var sectionHeight = $(".spacewalk-main-column-layout section").outerHeight();
  	var heightDoc = $(document).height();
    // Column heights should equal the document height minus the header height and footer height
    var newHeight = heightDoc - 145 - sectionHeight + "px";
    $(".spacewalk-main-column-layout section").css("padding-bottom", newHeight);
  };
  //function to hide or show the Collapsable menues.
  
  if ($(document).width()>465) {
    $(".collapse").addClass('in').css({
      "height": 'auto'
    });
  } else {
    $(".collapse").removeClass('in');
  }; 
};

// Render page fragments loaded via DWR
function makeAjaxCallback(divId, debug) {
  cb = function(text) {
    if (debug) {
      alert(text);
    }
    $('#' + divId).html(text);
    $('#' + divId).fadeIn();
    columnHeight();
  };
  return cb;
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

// Call the password strength meter in pwstrength.js
function checkPasswordStrength() {
    "use strict";
    var options = {
        scores: [10, 26, 40, 50],
        minChar: 5,
        bootstrap3: true,
        showVerdicts: false,
        usernameField: "#loginname",
        errorMessages:
        {
            password_too_short: '<dl><dt>The Password is too short.</dt><dd>must be at least 5 characters</dd></dl>',
        },
        onKeyUp: function (evt) {
            $('input[name="desiredpassword"]').popover('destroy');
            $(evt.target).pwstrength("outputErrorList");
            $('input[name="desiredpassword"]').popover({
                'html': true,
                'content': $('ul.error-list').html(),
                'trigger': 'focus',
                'placement': 'auto top'
            });
            $('input[name="desiredpassword"]').popover('show');
            //when there is no input the progressbar goes empty
            //and the popover disappears
            var desiredpassval = $.trim($('input[name="desiredpassword"]').val());
            if (desiredpassval.length == 0) {
                $('input[name="desiredpassword"]').popover('destroy');
                $('.progress-bar').css('width','0%');
            }
            //update the tick next to the desiredpassword input field
            updateTickIcon();
        }
    };
    $('input[name="desiredpassword"]').pwstrength(options);
}

function updateTickIcon() {
    var desiredpassval = $.trim($('input[name="desiredpassword"]').val());
    function success(element) {
        element.removeClass("fa-times-circle text-danger");
        element.addClass("fa-check-circle text-success");
    }
    function danger(element) {
        element.removeClass("fa-check-circle text-success");
        element.addClass("fa-times-circle text-danger");
    }
    if (desiredpassval.length < 5 && $("#desiredtick").hasClass("text-success")) {
        danger($("#desiredtick"));
    }
    else if (desiredpassval.length >= 5 && $("#desiredtick").hasClass("text-danger")) {
        success($("#desiredtick"));
    }
    if ($("#confirmpass").val() == desiredpassval && desiredpassval.length >= 5) {
        success($("#confirmtick"));
    }
    else if ($("#confirmtick").hasClass("text-success")) {
        danger($("#confirmtick"));
    }
}
