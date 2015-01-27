require 'rest-client'
require 'json'
require 'base64'

# refreshes the products.json file by querying SCC

AUTH_HEADER = { :Authorization => 'Basic ' + Base64.encode64( 'UCUSER:UCPASSWORD' ).chomp }

def process_rels(response)
  links = ( response.headers[:link] || '' ).split(', ').map do |link|
    href, name = link.match(/<(.*?)>; rel="(\w+)"/).captures

    [name.to_sym, href]
  end

  Hash[*links.flatten]
end

resp = RestClient.get('https://scc.suse.com/connect/organizations/products/unscoped', AUTH_HEADER)
raise StandardError, "not successful response of #{resp.code}" unless resp.code == 200

products = []
loop do
 links = process_rels(resp)
 products += JSON.parse(resp)
 break unless links[:next]
 resp = RestClient.get(links[:next], AUTH_HEADER)
end

File.open("products.json", 'w') { |file| file.write(JSON.pretty_generate(products)) }
