# Pitfalls in Testsuite Ruby code

## Use of DOM elements in the .feature description

```gherkin
When the user clicks in the buton '#accept-btn'
```

The whole point of using cucumber is not to add a layer but to make tests readable. Do:

```gherkin
When the user clicks the accept button
```

Put the complexity in the step. Keep the feature free of how the test is executed.

## Unnecessary parametrization

```ruby
When(/^I start database with the command "(.*?)"$/) do |start_command|
  $output = sshcmd(start_command)
end

When(/^when I stop the database with the command "(.*?)"$/) do |stop_command|
  $output = sshcmd(stop_command)
end

When(/^when I check the database status with the command "(.*?)"$/) do |check_command|
  $output = sshcmd(check_command)
end

When(/^I stop the database with the command "(.*?)"$/) do |stop_command|
  $output = sshcmd(stop_command)
end
```

There aren't two ways to start the database. Either use `When I stop the database` or if you are testing a tool to stop databases itself and want to be explicit just use `When I run the command XXXX`.

## Usage of `fail`

```ruby
fail if not foo.include?(bar)
```

fail just raises an exception if $condition, and you will not see much in the report of what failed.
What to do:

Use the [minitest assertions](http://docs.seattlerb.org/minitest/Minitest/Assertions.html): `assert_equal`, `assert_includes`, `assert_nil`, etc and their `refute_*` counterparts.

As last resort, use assert and refute plus a boolean expression.

## Usage of global variables to pass data across steps

```ruby
When(/^I start database with the command "(.*?)"$/) do |start_command|
  $output = sshcmd(start_command)
end

# do something with $output then
```

Just use member variables.

## Complex or just too many arguments to steps

Use data tables feature of cucumber

## Conditional tests: eg.Oracle

Do not just conditionally assert based on a flag, because the test will look green. In reality they never ran.

There is no good solution, either tag the tests and do not run them under that environment or use a common `Given` clause and the pending feature:

```gherkin
  Scenario: Restore backup with SMDBA
    Given a postgresql database is running
    And database "susemanager" has no table "dummy"
    ...
```

```ruby
Given(/^a postgresql database is running$/) do
  pending if postgresql?
  ...
end
```

That will mark all following steps as skipped.

## Super defensive handling of exceptions

```ruby
  def login(luser, password)
    begin
      @sid = @connection.call("auth.login", luser, password)
      return true
    rescue Exception => exception
      puts "Login failed. Try harder. :)"
      return false
    end
  end
```

First, `begin` is an expression, so you don't need returns, but that is another topic.

Second, emoticons in code...

Emoticons in code and user messages is a sign the developer knows he is doing something wrong and it is trying to calm himself. Don't do it.

```ruby
    rescue Exception => exception
      puts "Login failed. Try harder. :)"
      return false
    end
```

Specially since there was time to write smileys but no time to tell the user what is going on. (BTW: why is the exception passed as variable if not used at all?)

```ruby
    rescue Exception => e
      puts "Login failed: #{e}"
      return false
    end
```
Is more useful.

Now, lets see how this code is used

```ruby
fail if not rpctest.login(luser, password)
```

* Here, it eats the exception and its message, replaces it to a bool, to then raise a RuntimeError without message.

```ruby
rpc.login(luser, password)
```

Here the bool is ignored completely


```ruby
assert(rpctest.login(luser, password))
```

* Here, it eats the exception and its message, convert it to a bool, assert (without message) that raises a Test::Unit::AssertionFailedError exception, without message. Useful!

### Moral of the story

If you can't add any value to an exception, better leave it alone. Raising it will still fail the testcase. In this code, things are made worse by doing something with the exception.

The right code:

```ruby
  def login(luser, password)
      @sid = @connection.call("auth.login", luser, password)
  end
```

## Super defensive handling of null

```ruby
  def list_all_actions()
    return (@connection.call("schedule.list_all_actions", @sid) || [])
  end
```
When I saw this I thought "the API is broken and it returns nil when there are no actions". Wrong. I tried it...

```ruby
ret = conn.call('schedule.list_all_actions', sid)
puts ret.inspect
=> []
```

So if nil is returned, it is because something *ELSE* happened. The developer is changing the semantics of "wrong" to "empty", which is missleading for the layers above.

## Ruby is a bit functional

```ruby
  def list_chains()
    chains = @connection.call("actionchain.list_chains", @sid)
    labels = []
    for chain in chains
      labels.push(chain['label'])
    end
    return labels
  end
```

Don't think about methods on `how` do I get to the result. Thing more about `what`: "I need the label of every element".

Can be writen as:

```ruby
  def list_chains()
    @connection.call("actionchain.list_chains", @sid).map {|x| x['label']}
  end
```

# WAT?

## What is this?

This code tries to load "xmlrpctest.rb" which is in the same directory as the file. First add the directory of the file to the load path:

```ruby
File.expand_path(__FILE__)           # For Ruby 1.9.2+
$LOAD_PATH << File.dirname(__FILE__) # For Ruby 1.8

require 'xmlrpctest'
```

The second line does something (appends), but the first, is an expression, which evaluates to some path, and then the value is lost, so is wrong.

In ruby 1.9+ the best is to use `require_relative`

```ruby
require_relative 'xmlrpctest'
```
## Just buggy code

```ruby
  def get_user_ids()
    users = @connection.call("user.list_users", @sid)
    ids = []
    for user in users
      ids.push(user['login'])
    end
  end
```

This function on inspection tries to return the ids of all users. However:

* it collects the login attribute, not the ids (bug#1)
* it never returns anything, so it ends returning the same as list_users (bug#2)

Because it is broken twice, the caller just access the attributes

```ruby
Then /^when I call user\.list_users\(\), I should see a user "([^"]*)"$/ do |luser|
  users = rpctest.get_user_ids()
  seen = false
  for user in users
    if luser == user['login']
      seen = true
      break
    end
  end

  fail if not seen
end
```

So, we will never know if the developer:

* Intended to return the full user, and named the function bad (in that case all the iteration to save the ids array is useless)
* Intended to return the logins, but named the function wrong, and then forgot to return the value
* Intended to return the ids, but set the wrong attribute and then forgot to return the value

Look at this one:

```ruby
Then /^I shall not see "([^"]*)" when I call user\.list_roles\(\) with "([^"]*)" uid$/ do |rolename, luser|
  
  roles = rpctest.get_user_roles(luser)
  fail if roles != nil ? roles.length != 0 : false
end
```

It asserts that `rolename` is not seen in a list of roles. It accomplishes it without using the `rolename` variable at all. In reality it is checking that roles is not empty.

The correct code should be something (guessing developer's intent):

```ruby
Then /^I shall not see "([^"]*)" when I call user\.list_roles\(\) with "([^"]*)" uid$/ do |rolename, luser|
  refute_includes(@rpc.get_user_roles(luser), rolename)
end
```
## The steps that could never be friends

```ruby
Then(/^there should be no action chain with the label "(.*?)"\.$/) do |label|
  fail if rpc.list_chains().include?(label)
end

Then(/^there should be no action chain with the label "(.*?)"$/) do |label|
  fail if rpc.list_chains().include?(label)
end
```

Find the difference!

## Meanless step names

```gherkin
  Scenario: Run a remote command
    Given I follow "Salt"
    And I follow "Remote Commands"
    And I should see a "Remote Commands" text
    And I click on preview
    Then I should see my hostname
    And I click on run
    Then I wait for "3" seconds
    And I expand the results
    And I verify the results
```

Here `And I verify the results` is a very unfortunate step description because it does not tell you what was expected as a usecase. Every test ever in the world will verify it results as the last step.

This step should had read something like `Then the result should look like ls output`, without much specifics. The step will implement the details, but still the step description should help the user understand what is the expected result.

To make it worse, this step was defined in the `salt_remote_cmds.rb` file. Steps should be grouped by topic and not by feature.

Steps files should be like:

* Creating users
* Navigating webpages
* Running commands
* ...

If you have a step `Click button` in a specific feature steps, then it is wrong. Those should be in "navigation_steps" or something similar.

So in this case, the fact that `And I verify the results` was so abstract, it was already hinting that something was not right.

