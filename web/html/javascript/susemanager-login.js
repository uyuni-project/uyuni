$(document).on("ready", function() {
  // move the footer to the end of the body
  const footer = $('footer');
  footer.remove();
  $('body').append(footer);
  $("#scroll-top").remove();
  $("aside").remove();
  $('.navbar-toggle').remove();
  formFocus('loginForm', 'username');
  $("body").addClass('login-page');
  $('header').wrapInner('<div class="wrap"></div>').addClass('Raleway-font');

  // move and keep only the logo at the beginning of the header
  const logo = $('.navbar-brand');
  $('#breadcrumb').remove();
  $('header .wrap').prepend(logo);

  $('section').wrapInner('<div class="wrap"></div>');
  $('footer .wrapper').addClass('wrap');
});
