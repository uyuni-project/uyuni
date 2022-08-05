# Pitfalls in the test suite Ruby code

## Use of DOM elements in the .feature description

```gherkin
# bad
When the user clicks in the button '#accept-btn'
```

The whole point of using Cucumber is not to add a layer but to make tests readable. Do:

```gherkin
# good
When the user clicks the accept button
```

Put the complexity in the step. Keep the feature free of how the test is executed.

## Unnecessary parametrization

```ruby
When(/^I start database with the command "(.*?)"$/) do |start_command|
  $output, _code = $server.run(start_command)
end

When(/^when I stop the database with the command "(.*?)"$/) do |stop_command|
  $output, _code = $server.run(stop_command)
end

When(/^when I check the database status with the command "(.*?)"$/) do |check_command|
  $output, _code = $server.run(check_command)
end

When(/^I stop the database with the command "(.*?)"$/) do |stop_command|
  $output, _code = $server.run(stop_command)
end
```

There aren't two ways to start the database. Either use `When I stop the database` or if you are testing a tool to
stop databases itself and want to be explicit just use `When I run the command XXXX`.

## Usage of global variables to pass data across steps

```ruby
When(/^I start database with the command "(.*?)"$/) do |start_command|
  $output, _code = $server.run(start_command)
end

# do something with $output then
```

Just use member variables.

## Complex or just too many arguments to steps

Use the [data tables](https://cucumber.io/docs/cucumber/api/?lang=ruby) feature of cucumber.

## Conditional tests

Do not just conditionally fail based on a flag, because the test will look green. In reality it never ran.

There are several solutions:
* tag the tests and do not run them under that environment
* use a common `Given` clause and the `pending` feature:

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
# bad
  def login(luser, password)
    begin
      @sid = @connection.call('auth.login', luser, password)
      return true
    rescue Exception => e
      puts 'Login failed.'
      return false
    end
  end
```

The exception is not informative. Either use:

```ruby
# better
  def login(luser, password)
    begin
      @sid = @connection.call('auth.login', luser, password)
      return true
    rescue Exception => e
      puts "Login failed: #{e}"
      return false
    end
  end
```

Or, if you can't add any value to an exception, better leave it alone.
Raising it will still fail the test case:

```ruby
# best
  def login(luser, password)
    @sid = @connection.call('auth.login', luser, password)
  end
```

## Super defensive handling of null

```ruby
  def list_all_actions()
    @connection.call("schedule.list_all_actions", @sid) || []
  end
```

When I saw this I thought "the API is broken and it returns nil when there are no actions". Wrong. I tried it...

```ruby
ret = conn.call('schedule.list_all_actions', sid)
puts ret.inspect
=> []
```

So if nil is returned, it is because something *ELSE* happened. The developer is changing the semantics of "wrong" to
"empty", which is misleading for the layers above.

## Ruby is a bit functional

```ruby

def list_chains()
  chains = @connection.call("actionchain.list_chains", @sid)
  labels = []
  chains.each { |chain|
    labels.push(chain['label'])
  }
  return labels
end
```

Don't think about methods on `how` do I get to the result. Thing more about `what`: "I need the label of every element".

Can be written as:

```ruby
  def list_chains()
    @connection.call("actionchain.list_chains", @sid).map {|x| x['label']}
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

## Meaningless step names

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

Here `And I verify the results` is a very unfortunate step description because it is too generic.
Every test ever in the world will verify its results as the last step.
The step description should help the user understand what is the expected result.

To make it worse, this step was defined in the `salt_remote_cmds.rb` file. Steps should be grouped by topic and not by
feature. Step files should be like:

* Navigating web pages
* Running commands
* Using Salt
* etc.
