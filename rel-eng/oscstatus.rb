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
  puts "  #!!  NAG. package needs to be submitted but has no updated .changes file"
  puts "  #BB  package is blacklisted (intentionally not to be submitted)"
  puts "  #<<  package not in source project"
  puts "  #>>  package not in target project (initial submitreq missing)"
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
      return Hash[
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

  def list(pkg)
    ret = nil
    `#{apiCmd} ls -e -v #{@name} #{pkg}`.each do |rel|
      # 133a3b08cf390d4ee5c48b44f01a6e07      77      3946 Feb 15 10:23 spacewalk-backend.changes
      values = rel.split;
      ret = Hash.new if ret.nil?
      ret[values[6]] = values[0]
    end
    return ret
  end

  def rdiff(pkg, trg_prj, trg_pkg=nil)
    # :nag       - changed but no new .changes file
    # :unchanged - unchanged
    # :changed   - changed and new .changes file
    trg_pkg = pkg if trg_pkg.nil?
    t = trg_prj.list(trg_pkg)
    s = list(pkg)
    ret = :unchanged
    skiptar = false
    s.each do |file, hash|
      # TODO: OBS reformats the specfile, so it's hard to test for changes :(
      next if file == "#{pkg}.spec"
      # list of source tarballs accidentally submitted without any change:
      if [
	'spacewalk-remote-utils-git-3e6a6ef5bcc562f3d833856d6ba9263ac9299936.tar.gz',
	'rhnpush-git-264766239e6bb4ca32bf95a91b5c1932e2955932.tar.gz',
	'rhnmd-git-29d69a8b5d8a05fe8dc4cd885680588991219e29.tar.gz',
	'rhn-kickstart-git-dc6f81061d23f56026f741a4482f2561c303a609.tar.gz',
	'perl-Satcon-git-f1156210c40ed109f22f1175d8daae3099bebb59.tar.gz',
	'spacewalk-utils-git-29d69a8b5d8a05fe8dc4cd885680588991219e29.tar.gz',
	'spacewalk-setup-jabberd-git-264766239e6bb4ca32bf95a91b5c1932e2955932.tar.gz',
	'spacewalk-reports-git-2ddf92f56e810f7162e931ddb3768b42b73d1e6d.tar.gz',
	'spacewalk-remote-utils-git-264766239e6bb4ca32bf95a91b5c1932e2955932.tar.gz',
	'spacewalk-doc-indexes-git-a4ed1eef18de04265c6059e988afffd49e8b234f.tar.gz',
	'SatConfig-general-git-8a223d8889b5eb7bdb8381f63beaed43a9ef74cb.tar.gz',
	'SatConfig-cluster-git-b382944cd35255630bbe17e2f985da6ac737fcb6.tar.gz',
	'rhnpush-git-264766239e6bb4ca32bf95a91b5c1932e2955932.tar.gz',
	'rhnmd-git-29d69a8b5d8a05fe8dc4cd885680588991219e29.tar.gz',
	'rhn-kickstart-git-dc6f81061d23f56026f741a4482f2561c303a609.tar.gz',
	'python-hwdata-git-264766239e6bb4ca32bf95a91b5c1932e2955932.tar.gz',
	'perl-Satcon-git-f1156210c40ed109f22f1175d8daae3099bebb59.tar.gz',
	'perl-NOCpulse-Gritch-git-7e81be2fa8d6ebe71514af709613a36065a482bb.tar.gz',
	'perl-NOCpulse-CLAC-git-8a223d8889b5eb7bdb8381f63beaed43a9ef74cb.tar.gz',
	'perl-NOCpulse-Utils-git-aba88e44d4df65989367d43edf38fd58539b44ec.tar.gz',
	'NOCpulsePlugins-git-42ebcab7f80134d2404055abb657a1d54ef45469.tar.gz',
	'nocpulse-common-git-5f02cae65a6b7a48035b55f5126998d3b33bee73.tar.gz'
      ].include?(file)
	puts "###     >>> #{file} skiped" if not $opt_brief
	skiptar = true
	next
      end
      if !t.has_key?(file)
	puts "###     +++ #{file}" if not $opt_brief
	ret = :changed
      elsif hash != t[file]
	puts "###     <=> #{file} #{hash} <=> #{t[file]}" if not $opt_brief
	ret = :changed
      end
    end
    t.each do |file, hash|
      next if skiptar && Regexp.new("^#{pkg}.*\.tar\.gz$") === file
      if !s.has_key?(file)
	puts "###     --- #{file}" if not $opt_brief
	ret = :changed
      end
    end
    if ret == :changed
      chf = "#{pkg}.changes"
      if !s.has_key?(chf) || s[chf] == t[chf]
	ret = :nag
      end
    end
    return ret
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
  return $target_blacklist.include?(prj) && $target_blacklist[prj].include?(pkg)
end

def check_whether_to_submitt( src_prj, trg_prj, packages=nil )
  packages = src_prj.packages unless packages
  puts "###"
  puts "### SUBMISSION #{src_prj.name} ==> #{trg_prj.name}"
  puts "###"
  packages.each do |pkg|

    if in_target_blacklist( trg_prj.name, pkg )
      puts "#BB #{pkg} is on blacklist for #{trg_prj}" if not $opt_brief
      next
    end

    rel = src_prj[pkg]
    if rel.nil? || rel.empty?
      puts "#<< #{pkg} not yet created in #{src_prj}"
      next
    end

    sub = trg_prj.request( pkg )
    if sub.nil? || sub.empty?
      puts "#>> #{pkg} not yet submitted to #{trg_prj}"
      puts "#   #{trg_prj.apiCmd} submitreq --yes -m \"update from #{src_prj.name}\" #{src_prj.name} #{rel[:nam]} #{trg_prj.name}"
      next
    end

    case src_prj.rdiff(pkg, trg_prj)
      when :unchanged:
	if sub[:sta] == 'accepted'
	  puts "#== #{rel[:nam]} (#{rel[:rev]}) <==> (#{sub[:ore]}) ##{sub[:rid]}:#{sub[:sta]} #{sub[:dat]}" if not $opt_brief
	else
	  puts "#NN #{rel[:nam]} (#{rel[:rev]}) <==> (#{sub[:ore]}) ##{sub[:rid]}:#{sub[:sta]} #{sub[:dat]}"
	end

      when :changed:
	if sub[:sta] == 'new' && rel[:rev] == sub[:ore]
	  puts "#NN #{rel[:nam]} (#{rel[:rev]}) <==> (#{sub[:ore]}) ##{sub[:rid]}:#{sub[:sta]} #{sub[:dat]}"
	else
	  puts "#++ #{rel[:nam]} (#{rel[:rev]}) <==> (#{sub[:ore]}) ##{sub[:rid]}:#{sub[:sta]} #{sub[:dat]}"
	  puts "    #{trg_prj.apiCmd} submitreq --yes -m \"update from #{src_prj.name}\" #{src_prj.name} #{rel[:nam]} #{trg_prj.name}"
	end

      when :nag:
	puts "#!! #{rel[:nam]} (#{rel[:rev]}) <==> (#{sub[:ore]}) ##{sub[:rid]}:#{sub[:sta]} #{sub[:dat]}"
	puts "#   #{trg_prj.apiCmd} submitreq --yes -m \"update from #{src_prj.name}\" #{src_prj.name} #{rel[:nam]} #{trg_prj.name}"
    end

  end
end

# ======================================================================
# input projects
$src_prj = Prj.new('ibs://Devel:Galaxy:Server:Manager:1')
$ins_prj = Prj.new('ibs://Devel:Galaxy:Install:Manager:1')
$res_prj = Prj.new('ibs://Devel:Galaxy:RESClient:Manager:1')

# target projects
$trg_prj = Prj.new('ibs://SUSE:SLE-11-SP1:Update:Manager:1.2')
$cli_prj = Prj.new('ibs://SUSE:SLE-11-SP1:Update:Test')

# check required submissions:
# ======================================================================

#
check_whether_to_submitt( $src_prj, $trg_prj )

#
$cli_packages = [
  'osad',
  'perl-Satcon',
  'rhn-custom-info',
  'rhn-kickstart',
  'rhn-virtualization',
  'rhncfg',
  'rhnlib',
  'rhnmd',
  'rhnpush',
  'spacewalk-backend',
  'spacewalk-certs-tools',
  'spacewalk-client-tools',
  'spacewalk-config',
  'spacewalk-koan',
  'spacewalk-remote-utils',
  'spacewalk-ssl-cert-check',
  'spacewalksd',
  'suseRegisterInfo',
  'yum-rhn-plugin',
  'zypp-plugin-spacewalk'
]
check_whether_to_submitt( $src_prj, $cli_prj, $cli_packages )

#
$ins_packages = [
  'cobbler'
]
check_whether_to_submitt( $ins_prj, $cli_prj, $ins_packages )

# required for RHEL but exists in SLES 'python-setuptools'
$res_packages = [
]
check_whether_to_submitt( $res_prj, $cli_prj, $res_packages )
