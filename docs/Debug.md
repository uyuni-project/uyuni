## Debugging the testsuite itself (not the product tested!)

You need to change the spacewalk-testsuite-base/features/support/env.rb file to the following settings:

    diff --git a/features/support/env.rb b/features/support/env.rb
    index ee30deb..73b44fb 100644
    --- a/features/support/env.rb
    +++ b/features/support/env.rb
    @@ -86,10 +86,11 @@ when :phantomjs
                                                              '--ssl-protocol=TLSv1',
                                                              '--web-security=false'],
                                                              :js_errors => false,
    -                                      :debug => false)
    +                                      :debug => true,
    +                                      :inspector => 'cat')
     end
       Capybara.default_driver = :poltergeist
    -  Capybara.javascript_driver = :poltergeist
    +  Capybara.javascript_driver = :poltergeist_debug
       Capybara.app_host = host
     when :firefox
       require 'selenium-webdriver'
        @@ -118,6 +119,7 @@ Capybara.run_server = false
         # screenshots
         After do |scenario|
       if scenario.failed?
    +    page.driver.debug
         encoded_img = page.driver.render_base64(:png, :full => true)
         embed("data:image/png;base64,#{encoded_img}", 'image/png')
       end

Note: if your control node has a graphical interface, replace e.g. ```inspector => 'cat'``` with ```inspector => 'firefox'```.

This will stop on errors and let you debug.  You can connect to the debugger in your browser via http://$your_control_node:9664 . It will show you a list of urls being tested.  Click on the link and you'll be shown a webkit debugger.  It's like the chrome developer or firefox tools.  You can't see the page rendered, but you can run jquery commands or xpath queries in the console.  In your console where you are running the test suite, it'll sit here until you're ready to continue.  Just hit enter in the console and it'll go until the next failure.  

If you want to manually insert a debug into the feature, you can use a special step named "debug".  You can use it by putting "And debug" above the feature you want to stop on.  All it has to do is call ```page.driver.debug```.

Click on the link and you'll be shown a webkit debugger.  It's like the chrome developer or firefox tools.  You can't see the page rendered, but you can run jquery commands or xpath queries in the console.  In your console where you are running the test suite, it'll sit here until you're ready to continue.  Just hit enter in the console and it'll go until the next failure.  

If you want to manually insert a debug into the feature(or steps).  I added a debug step, called "debug".  You can use it by putting "And debug" above the feature you want to stop on.  


Some examples:

```javascript
// To just load the HTML body
$('body')  // jquery
$x('//body') // xpath
// You can do more specific queries...
$x("//span[contains(text(), 'SUSE Linux Enterprise Server 12')]/ancestor::tr[td[contains(text(), 'x86_64')]]") // xpath
$("div:contains('sumas21')") // jquery
// A couple of other useful things... reloading and getting the url
location.reload() // Reload
window.location.href() // For checking to see if you're on the right url
```

The error message from capybara will usually give you the xpath or whatever went wrong.  So, check there and then search from there.
