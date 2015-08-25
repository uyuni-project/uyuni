require 'rest-client'
require 'json'
require 'base64'

# refreshes test json files by querying SCC

AUTH_HEADER = { :Authorization => 'Basic ' + Base64.encode64( 'UCUSER:UCPASSWORD' ).chomp }

def process_rels(response)
  links = ( response.headers[:link] || '' ).split(', ').map do |link|
    href, name = link.match(/<(.*?)>; rel="(\w+)"/).captures

    [name.to_sym, href]
  end

  Hash[*links.flatten]
end

def get_paginated(url)
  resp = RestClient.get(url, AUTH_HEADER)
  raise StandardError, "not successful response of #{resp.code}" unless resp.code == 200

  result = []
  loop do
   links = process_rels(resp)
   result += JSON.parse(resp)
   break unless links[:next]
   resp = RestClient.get(links[:next], AUTH_HEADER)
  end

  result
end

def save_json(content, path)
  File.open(path, 'w') { |file| file.write(JSON.pretty_generate(content)) }
end

products = get_paginated("https://scc.suse.com/connect/organizations/products/unscoped")
save_json(products, "products.json")

repositories = get_paginated("https://scc.suse.com/connect/organizations/repositories")
save_json(repositories, "repositories.json")
