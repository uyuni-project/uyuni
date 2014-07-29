require 'net/ssh'
require 'stringio'

def sshcmd(command, host: ENV['TESTHOST'], user: 'root', ignore_err: false)
  #Execute a command on the remote server
  #Not passing :password uses systems keys for auth
  out = StringIO.new
  err = StringIO.new
  Net::SSH.start(host, user) do |ssh|
    ssh.exec!(command) do |chan, str, data|
      out << data if str == :stdout
      err << data if str == :stderr
    end
  end
  if err.string.empty? || ignore_err
    results = { stdout: out.string, stderr: err.string }
  else
    raise "Execute command failed #{command}: #{err.string}"
  end
end
