$(document).on("ready", function() {
  $("aside").remove();
  var $section = $("section");
  $section.addClass('wrap').removeAttr('id');
  formFocus('loginForm', 'username');
  $("body").addClass('login-page');
  var $bodyContent = $(".spacewalk-main-column-layout");
  $bodyContent.replaceWith($section);
  var $bottomLine = $('.bottom-line');
  $('footer').detach('.bottom-line');
  $('footer').wrapInner('<div class="wrap"><div class="row"></div></div>');
  $('header').wrapInner('<div class="wrap"><div class="row"></div></div>');
  $bottomLine.appendTo('footer');
  $('header .wrap .navbar-collapse').remove();
  $('header .wrap .navbar-header .navbar-brand:first-of-type').remove();
});
