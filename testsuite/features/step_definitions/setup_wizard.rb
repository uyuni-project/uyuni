# Copyright 2011-2014 Novell

When(/^I make the credentials primary$/) do
  fail unless find('i.fa-star-o').click
end

When(/^I refresh scc$/) do
  sshcmd('echo -e "admin\nadmin\n" | mgr-sync refresh', ignore_err: true)
end

When(/^I delete the primary credentials$/) do
  fail unless find('i.fa-trash-o', :match => :first).click
  step 'I click on "Delete"'
end

When(/^I view the primary subscription list$/) do
  fail unless find('i.fa-th-list', :match => :first).click
end

When(/^I view the primary subscription list for asdf$/) do
  within(:xpath, "//h3[contains(text(), 'asdf')]/../..") do
    fail unless find('i.fa-th-list', :match => :first).click
  end
end

When(/^I click on "([^"]*)" link in the setup wizard$/) do |arg1|
  tag = case arg1
  when /Edit/ then "i.fa-pencil"
  when /List/ then "i.fa-th-list"
  when /Verify/ then "i.fa-check-square"
  else raise "Unknown element"
  end
  within(".text-left") do
     fail unless find(tag).click
  end
end

When(/^I select "([^\"]*)" as a product for the "([^\"]*)" architecture$/) do |product, architecture|
  within(:xpath, "(//span[contains(text(), '#{product}')]/ancestor::tr[td[contains(text(), '#{architecture}')]])[1]") do
    fail unless find("button.product-add-btn").click
    begin
      # wait to finish scheduling
      Timeout.timeout(DEFAULT_TIMEOUT) do
        loop do
          begin
            break unless find('button.product-add-btn').visible?
            sleep 2
          rescue Capybara::ElementNotFound
            break
          end
        end
      end
    rescue Timeout::Error
      puts 'timeout reached'
    end
  end
end

When(/^I select the addon "(.*?)" for the product "(.*?)" with arch "(.*?)"$/) do |addon, product, arch|
  # xpath query is too long, so breaking up on multiple lines.
  xpath =  "//span[contains(text(), '#{product}')]/"
  xpath += "ancestor::tr[td[contains(text(), '#{arch}')]]/following::span"
  xpath += "[contains(text(), '#{addon}')]/../.."
  within(:xpath, "#{xpath}") do
    fail unless find("button.product-add-btn").click
    begin
      # wait to finish scheduling
      Timeout.timeout(DEFAULT_TIMEOUT) do
        loop do
          begin
            break unless find('button.product-add-btn').visible?
            sleep 2
          rescue Capybara::ElementNotFound
            break
          end
        end
      end
    rescue Timeout::Error
      puts 'timeout reached'
    end
  end
end

When(/^I click the Add Product button$/) do
  fail unless find("button#synchronize").click
end

When(/^I wait until it has finished$/) do
  find("button#synchronize .fa-plus")
end

When(/^I verify the products were added$/) do
  output = sshcmd('echo -e "admin\nadmin\n" | mgr-sync list channels', ignore_err: true)
  fail unless output[:stdout].include? '[I] SLES12-SP1-Pool for x86_64 SUSE Linux Enterprise Server 12 SP1 x86_64 [sles12-sp1-pool-x86_64]'
  fail unless output[:stdout].include? '[I] SLE-Manager-Tools12-Pool x86_64 SP1 SUSE Manager Tools [sle-manager-tools12-pool-x86_64-sp1]'
  fail unless output[:stdout].include? '[I] SLE-Module-Legacy12-Updates for x86_64 SP1 Legacy Module 12 x86_64 [sle-module-legacy12-updates-x86_64-sp1]'
end

When(/^I click the channel list of product "(.*?)" for the "(.*?)" architecture$/) do |product, architecture|
  within(:xpath, "//span[contains(text(), '#{product}')]/ancestor::tr[td[contains(text(), '#{architecture}')]]") do
    fail unless find('.product-channels-btn').click
  end
end

Then(/^I see verification succeeded/) do
  find("i.text-success")
end
