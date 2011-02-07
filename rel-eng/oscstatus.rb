#!/usr/bin/ruby
# ======================================================================
def usage
  puts ""
  puts "usage: oscstatus.rb [OPTIONS]"
  puts ""
  puts "Print submission status of packages:"
  puts "  #==  package is up to date (supressed unless -v is used)"
  puts "  #NN  package is up to date but last submitreq not yet accepted"
  puts "  #++  package needs to be submitted (submitreq provided on stdout)"
  puts "  #>>  package not in target project (initial submitreq missing)"
  puts "  #BB  package is blacklisted (intentionally not submitted)"
  puts "  #<<  package not in source project"
  puts ""
  puts "Options:"
  puts " -h, --help      This message."
  puts " -v, --verbose   Print unchanged packages too."
  puts ""
  puts 'oscstatus.rb [-v|--verbose]'; exit 0
  exit 0
end

$opt_brief = true

ARGV.each do |arg|
  case arg
  when '-v', '--verbose' then $opt_brief = false
  when '-?', '-h', '--help' then usage
  end
end

# ======================================================================
class Prj

  attr_reader :api, :name

  def initialize(name,api=:osc)
    @name = case name
      when /obs:\/\/(.*)/ then @api = :osc; $1
      when /osc:\/\/(.*)/ then @api = :osc; $1
      when /ibs:\/\/(.*)/ then @api = :isc; $1
      when /isc:\/\/(.*)/ then @api = :isc; $1
      else  @api = api; $1
    end
  end

  def apiCmd()
    case @api
    when :osc then 'osc'
    when :isc then 'osc -A https://api.suse.de'
    end
  end

  def packages()
    `#{apiCmd} list #{@name}`.split("\n")
  end

  def package(pkg)
    `#{apiCmd} log --csv #{@name} #{pkg}`.each do |rel|
      # 5|zypp-team|2011-01-11 17:29:55|b30f5af8aef32eab673f5c7c53a090ec|1.1.1|Git submitt Manager()|
      values = rel.split( '|' );
      return [
	:nam => pkg,
	:rev => values[0],
	:who => values[1],
	:dat => values[2],
	:chk => values[3],
	:ver => values[4],
	:msg => values[5]
      ]
      break
    end
    return nil
  end

  def request(pkg)
    `#{apiCmd} request list -b -s new,accepted #{@name} #{pkg} | head -n 1`.each do |rel|
      # 10087  State:accepted By:oertel       When:2011-01-14T17:26:18
      #        submit:       Devel:Galaxy:Server:Manager:1/spacewalk  ->        SUSE:SLE-11-SP1:Update:Manager:1.2
      #        From: mcalmer(new)
      #        Descr: - do not require a special release
      values = rel.split;
      ret = Hash[
	:nam => pkg,
	:rid => values[0],
	:sta => /:(.*)/.match(values[1])[1],
	:dat => /:(.*)/.match(values[3])[1]
      ]
      `#{apiCmd} request show #{ret[:rid]} | grep '^ *submit:'`.each do |rel|
	#  submit:   Devel:Galaxy:Server:Manager:1/zypper(r1) -> SUSE:SLE-11-SP1:Update:Manager:1.2/zypper
	values = rel.split[1].split('/')
	ret[:opr] = values[0]
	ret[:ore] = /\(r(.*)\)/.match(values[1])[1]
	break
      end
      return ret
      break
    end
    return nil
  end

  def [](name)
    package(name)
  end

  def to_s()
    "#{@api}://#{@name}"
  end

end
# ======================================================================

# Packages we do not submitt intentionally:
$target_blacklist = Hash[
  'SUSE:SLE-11-SP1:Update:Manager:1.2' => [
    'jabberd-selinux',
    'libsatsolver',
    'libzypp',
    'oracle-instantclient-selinux',
    'oracle-rhnsat-selinux',
    'oracle-selinux',
    'oracle-xe-selinux',
    'spacewalk-monitoring-selinux',
    'spacewalk-proxy-selinux',
    'spacewalk-selinux',
    'zypper'
  ]
];

def in_target_blacklist( prj, pkg )
  return $target_blacklist.include?( prj ) && $target_blacklist[prj].include?( pkg )
end

def check_whether_to_submitt( src_prj, trg_prj, packages=nil )
  packages = src_prj.packages unless packages
  puts "###"
  puts "### SUBMISSION #{src_prj.name} ==> #{trg_prj.name}"
  puts "###"
  packages.each do |pkg|
    package_detail = src_prj[pkg]
    if not package_detail
	puts "#!! #{pkg} has no history in #{src_prj}"
	return
    end
    package_detail.each do |rel|
      if not rel
	puts "#<< #{pkg} not yet created in #{src_prj}"
	next
      end
      sub = trg_prj.request( pkg )
      if not sub
	if not in_target_blacklist( trg_prj.name, pkg )
	  puts "#>> #{pkg} not yet submitted to #{trg_prj}"
	  puts "#   #{trg_prj.apiCmd} submitreq --yes -m \"update from #{src_prj.name}\" #{src_prj.name} #{rel[:nam]} #{trg_prj.name}"
	else
	  puts "#BB #{pkg} not yet submitted to #{trg_prj}" if not $opt_brief
	end
	next
      end
      if rel[:rev].to_i > sub[:ore].to_i
	puts "#++ #{rel[:nam]} (#{rel[:rev]}) <==> (#{sub[:ore]}) ##{sub[:rid]}:#{sub[:sta]} #{sub[:dat]}"
	puts "    #{trg_prj.apiCmd} submitreq --yes -m \"update from #{src_prj.name}\" #{src_prj.name} #{rel[:nam]} #{trg_prj.name}"
      else
	if sub[:sta] == 'accepted'
	  puts "#== #{rel[:nam]} (#{rel[:rev]}) <==> (#{sub[:ore]}) ##{sub[:rid]}:#{sub[:sta]} #{sub[:dat]}" if not $opt_brief
	else
	  puts "#NN #{rel[:nam]} (#{rel[:rev]}) <==> (#{sub[:ore]}) ##{sub[:rid]}:#{sub[:sta]} #{sub[:dat]}"
	end
      end
      break
    end
  end
end

# ======================================================================
# input projects
$src_prj = Prj.new('ibs://Devel:Galaxy:Server:Manager:1')
$ins_prj = Prj.new('ibs://Devel:Galaxy:Install:Manager:1')

# target projects
$trg_prj = Prj.new('ibs://SUSE:SLE-11-SP1:Update:Manager:1.2')
$cli_prj = Prj.new('ibs://SUSE:SLE-11-SP1:Update:Test')

# check required ubmissions:

check_whether_to_submitt( $src_prj, $trg_prj )

# in py-core now; RH only  'python-hashlib',
$ins_packages = [
  'python-jabberpy',
  'cobbler'
]
check_whether_to_submitt( $ins_prj, $cli_prj, $ins_packages )

$cli_packages = [
  'osad',
  'spacewalk-proxy-installer',
  'rhn-custom-info',
  'rhn-kickstart',
  'rhn-virtualization',
  'rhncfg',
  'rhnmd',
  'rhnlib',
  'rhnpush',
  'spacewalk-backend',
  'spacewalk-certs-tools',
  'spacewalk-koan',
  'spacewalk-proxy-installer',
  'spacewalk-remote-utils',
  'spacewalk-ssl-cert-check',
  'spacewalk-utils',
  'spacewalksd',
  'spacewalk-client-tools',
  'zypp-plugin-spacewalk',
  'suseRegisterInfo'
]
check_whether_to_submitt( $src_prj, $cli_prj, $cli_packages )

