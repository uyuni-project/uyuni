jQuery(document).ready(function() {
  // move the footer to the end of the body
  const footer = jQuery('footer');
  footer.remove();
  jQuery('body').append(footer);
  jQuery("#scroll-top").remove();
  jQuery("aside").remove();
  jQuery('.navbar-toggle').remove();
  formFocus('loginForm', 'username');
  jQuery("body").addClass('login-page');
  jQuery('header').wrapInner('<div class="wrap"></div>').addClass('Raleway-font');

  // move and keep only the logo at the beginning of the header
  const logo = jQuery('.navbar-brand');
  jQuery('#breadcrumb').remove();
  jQuery('header .wrap').prepend(logo);

  jQuery('section').wrapInner('<div class="wrap"></div>');
  jQuery('footer .wrapper').addClass('wrap');
});
