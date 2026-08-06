# Copyright (c) 2026 SUSE LLC.
# Licensed under the terms of the MIT license.

### Generic, JSON-data-driven runner for syncing products through the Setup Wizard
### "Products" UI. Replaces the one-Scenario-per-product pattern in
### srv_sync_all_products.feature / srv_sync_products.feature: the data (which
### product, which sub-products/modules, which minion/profile it applies to) lives
### in testsuite/features/support/data/*.json; this file only implements the
### traversal and tag matching that used to be duplicated per Scenario.

require 'json'

# Resolves a single named tag (minion deployment flag or profile characteristic) against
# live suite state. Reuses the same globals/helpers env.rb and commonlib.rb already compute
# for the equivalent Cucumber tag hooks, rather than re-deriving them.
def product_sync_tag_active?(tag)
  case tag
  when 'cloud' then $is_cloud_provider
  when 'transactional_server' then $is_transactional_server
  when 'skip_if_transactional_server' then !$is_transactional_server
  when 'susemanager' then product == 'SUSE Manager'
  when 'uyuni' then product == 'Uyuni'
  when 'run_if_proxy_transactional_or_slmicro62_minion'
    suse_proxy_transactional? || ENV.key?(ENV_VAR_BY_HOST['slmicro62_minion'])
  when 'run_if_proxy_not_transactional_or_sles15sp7_minion'
    suse_proxy_non_transactional? || ENV.key?(ENV_VAR_BY_HOST['sles15sp7_minion'])
  else
    raise ScriptError, "product sync JSON references unknown tag '#{tag}'" unless ENV_VAR_BY_HOST.key?(tag)

    ENV.key?(ENV_VAR_BY_HOST[tag])
  end
end

# minion_tags: OR-matched (deployed via any one of them). profile_tags: AND-matched (a
# product can require e.g. both @proxy and a specific minion). Both empty/absent => matches.
def product_sync_tags_match?(entry)
  minion_tags = entry['minion_tags'] || []
  profile_tags = entry['profile_tags'] || []
  (minion_tags.empty? || minion_tags.any? { |t| product_sync_tag_active?(t) }) &&
    (profile_tags.empty? || profile_tags.all? { |t| product_sync_tag_active?(t) })
end

def product_sync_normalize_parent_product_entry(raw)
  raw.is_a?(String) ? { 'name' => raw } : raw
end

# Opens a single tree node's sub-list, tracking what's already open so a node referenced
# from multiple sub_products' parent_products (e.g. a shared parent module) is only opened
# once — opening an already-open sub-list raises, since the underlying step looks for the
# "collapsed" (fa-angle-right) icon specifically.
def product_sync_open_once!(name, opened, if_present: false)
  return if opened.include?(name)

  suffix = if_present ? ' if present' : ''
  step(%(I open the sub-list of the product "#{name}"#{suffix}))
  opened << name
end

def product_sync_reveal_tree!(product, sub_products, opened)
  product_sync_open_once!(product['select'], opened)
  sub_products.each do |sub_product|
    (sub_product['parent_products'] || []).each do |raw|
      entry = product_sync_normalize_parent_product_entry(raw)
      step(%(I select "#{entry['name']}" as a product)) if entry['select_first']
      product_sync_open_once!(entry['name'], opened, if_present: entry['if_present'])
    end
    next unless sub_product['open_if_present']

    product_sync_open_once!(sub_product['select'], opened, if_present: true)
  end
end

def product_sync_apply_sub_product!(sub_product)
  if sub_product['beta_toggle']
    step(%(I select or deselect "#{sub_product['select']}" beta client tools))
    return
  end

  step(%(I select "#{sub_product['select']}" as a product))
  step(%(I should see the "#{sub_product['select']}" selected))
end

def product_sync_resolve_base(product)
  matches = product['extends_one_of'].select { |branch| product_sync_tags_match?(branch) }
  raise ScriptError, "no matching base for '#{product['id']}' in the active environment" if matches.empty?
  if matches.size > 1
    raise ScriptError, "ambiguous base for '#{product['id']}': #{matches.map { |m| m['base_id'] }.join(', ')}"
  end

  matches.first
end

def product_sync_add_and_wait!(select_text, wait_id)
  step(%(I click the Add Product button))
  step(%(I wait until I see "Selected channels/products were scheduled successfully for syncing." text))
  step(%(I wait until I see "#{select_text}" product has been added))
  step(%(I wait until all synchronized channels for "#{wait_id}" have finished))
end

def product_sync_process_extra_channels!(product)
  (product['extra_channels'] || []).each do |extra|
    next unless product_sync_tags_match?(extra)

    step(%(I add "#{extra['channel']}" channel))
    step(%(I wait until the channel "#{extra['channel']}" has been synced))
  end
end

def product_sync_find_base_product(all_products, branch)
  all_products.find { |p| p['id'] == branch['base_id'] } ||
    (raise ScriptError, "extends_one_of references unknown base_id '#{branch['base_id']}'")
end

def product_sync_sync_extension!(product, all_products)
  base = product_sync_resolve_base(product)
  base_product = product_sync_find_base_product(all_products, base)
  select_text = base['select_variant'] || product['select']
  search_text = base['search_text'] || select_text

  step(%(Given I am authorized for the "Admin" section))
  step(%(I follow the left menu "Admin > Setup Wizard > Products"))
  step(%(I wait until I do not see "currently running" text))
  step(%(I wait until I do not see "Loading" text))
  step(%(I enter "#{search_text}" as the filtered product description))
  step(%(I select "#{base_product['select']}" as a product))
  step(%(I should see the "#{base_product['select']}" selected))

  opened = []
  (base['parent_products'] || [base_product['select']]).each do |raw|
    entry = product_sync_normalize_parent_product_entry(raw)
    step(%(I select "#{entry['name']}" as a product)) if entry['select_first']
    product_sync_open_once!(entry['name'], opened, if_present: entry['if_present'])
  end
  step(%(I select "#{select_text}" as a product))
  step(%(I should see the "#{select_text}" selected))
  product_sync_add_and_wait!(select_text, base['wait_id'] || product['id'])
end

def product_sync_sync_product!(product, sub_products_override)
  step(%(Given I am authorized for the "Admin" section))
  step(%(I follow the left menu "Admin > Setup Wizard > Products"))
  step(%(I wait until I do not see "currently running" text))
  step(%(I wait until I do not see "Loading" text))
  step(%(I enter "#{product['search_text']}" as the filtered product description)) if product['search_text']

  sub_products = sub_products_override.nil? ? (product['sub_products'] || []) : sub_products_override

  opened = []
  product_sync_reveal_tree!(product, sub_products, opened) if sub_products.any?
  sub_products.select { |sp| sp['recommended'] }.each do |sp|
    step(%(Then I should see that the "#{sp['select']}" product is "recommended"))
  end

  step(%(I select "#{product['select']}" as a product))
  step(%(I should see the "#{product['select']}" selected))

  sub_products.each { |sp| product_sync_apply_sub_product!(sp) }

  product_sync_add_and_wait!(product['select'], product['id'])
  product_sync_process_extra_channels!(product)
end

def product_sync_resolve_sub_products_override(product, mode, names)
  return nil if mode.empty?
  return [] if mode.strip == 'without sub-products'

  wanted = names.split(',').map(&:strip)
  available = product['sub_products'] || []
  wanted.map do |name|
    available.find { |sp| sp['select'] == name } ||
      (raise ScriptError, "'#{name}' is not a sub_product of '#{product['id']}' in the JSON file")
  end
end

Given(/^I sync products from JSON file "([^"]*)"(?: restricted to id "([^"]*)")?(?: (with sub-products "([^"]*)"|without sub-products))?$/) \
  do |filename, only_id, mode, subset_names|
  data = JSON.parse(File.read("#{File.dirname(__FILE__)}/../support/data/#{filename}"))
  products = data['products']

  if only_id
    product = products.find { |p| p['id'] == only_id }
    raise ScriptError, "no product with id '#{only_id}' in #{filename}" unless product

    products = [product]
  end

  matched_count = 0
  products.each do |candidate_product|
    next unless product_sync_tags_match?(candidate_product)

    matched_count += 1
    if candidate_product['extends_one_of']
      product_sync_sync_extension!(candidate_product, data['products'])
      product_sync_process_extra_channels!(candidate_product)
      next
    end

    override = only_id ? product_sync_resolve_sub_products_override(candidate_product, mode.to_s, subset_names.to_s) : nil
    product_sync_sync_product!(candidate_product, override)
  end

  raise ScriptError, "no products in #{filename} matched the active minion/profile tags" if matched_count.zero?
end
