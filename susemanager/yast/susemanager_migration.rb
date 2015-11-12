# encoding: utf-8

module Yast
  class SusemanagerMigrationClient < Client
    def main
      Yast.import "UI"
      textdomain "susemanager"

      Yast.import "Directory"
      Yast.import "Hostname"
      Yast.import "FileUtils"
      Yast.import "GetInstArgs"
      Yast.import "IP"
      Yast.import "Popup"
      Yast.import "Stage"
      Yast.import "Wizard"

      @args = GetInstArgs.argmap
      @ret = :auto

      @migration_file = Ops.add(Directory.tmpdir, "/susemanager_migration")
      @migration = FileUtils.Exists(@migration_file)
      if !@migration
        Builtins.y2milestone(
          "setup from scratch was chosen, skipping this step"
        )
        return deep_copy(@ret)
      end

      @invalid_pw_chars = "\"$'!"

      @settings = {
        "SATELLITE_HOST"    => "susemanager",
        "SATELLITE_DOMAIN"  => Hostname.CurrentDomain,
        "SATELLITE_DB_USER" => "susemanager",
        "SATELLITE_DB_PASS" => "susemanager",
        "SATELLITE_DB_SID"  => "susemanager"
      }

      @env_file = Ops.add(Directory.tmpdir, "/env_migration")
      if FileUtils.Exists(@env_file)
        SCR.Execute(path(".target.remove"), @env_file)
      end
      SCR.Execute(
        path(".target.bash"),
        Builtins.sformat("/usr/bin/touch %1; /bin/chmod 0600 %1;", @env_file)
      )

      # read existing values, if present
      Builtins.foreach(@settings) do |key, value|
        val = Builtins.getenv(key)
        if val != nil && val != ""
          Builtins.y2internal("value for %1 present: %2", key, val)
          Ops.set(@settings, key, val)
        end
      end


      @contents = HBox(
        HSpacing(1),
        VBox(
          InputField(
            Id("SATELLITE_HOST"),
            Opt(:hstretch),
            # text entry label
            _("&Hostname of the Satellite Server"),
            Ops.get(@settings, "SATELLITE_HOST", "")
          ),
          # text entry label
          InputField(
            Id("SATELLITE_DOMAIN"),
            Opt(:hstretch),
            _("&Domain name"),
            Ops.get(@settings, "SATELLITE_DOMAIN", "")
          ),
          # text entry label
          InputField(
            Id("SATELLITE_DB_USER"),
            Opt(:hstretch),
            _("Satellite Database &User Name"),
            Ops.get(@settings, "SATELLITE_DB_USER", "")
          ),
          # text entry label
          InputField(
            Id("SATELLITE_DB_PASS"),
            Opt(:hstretch),
            _("Satellite Database &Password"),
            Ops.get(@settings, "SATELLITE_DB_PASS", "")
          ),
          # text entry label
          InputField(
            Id("SATELLITE_DB_SID"),
            Opt(:hstretch),
            _("Satellite Database &Name"),
            Ops.get(@settings, "SATELLITE_DB_SID", "")
          ),
          VSpacing(0.5)
        ),
        HSpacing(1)
      )


      @help_text = ""

      # dialog caption
      Wizard.SetContents(
        _("Migration from Red Hat Satellite to SUSE Manager"),
        @contents,
        @help_text,
        Ops.get_boolean(@args, "enable_back", true),
        Ops.get_boolean(@args, "enable_next", true)
      )
      UI.SetFocus(Id("SATELLITE_HOST"))

      while true
        @ret = UI.UserInput
        break if @ret == :back
        break if @ret == :abort && Popup.ConfirmAbort(:incomplete)
        if @ret == :next
          @pw1 = Convert.to_string(
            UI.QueryWidget(Id("SATELLITE_DB_PASS"), :Value)
          )
          if @pw1 == ""
            Popup.Error(_("Password is missing."))
            UI.SetFocus(Id("SATELLITE_DB_PASS"))
            next
          end
          if @pw1 != Builtins.deletechars(@pw1, @invalid_pw_chars)
            Popup.Error(
              Builtins.sformat(
                _(
                  "The password contains invalid characters.\nThe invalid characters are: %1"
                ),
                @invalid_pw_chars
              )
            )
            UI.SetFocus(Id("SATELLITE_DB_PASS"))
            next
          end

          @domain = Convert.to_string(
            UI.QueryWidget(Id("SATELLITE_DOMAIN"), :Value)
          )
          if !Hostname.CheckDomain(@domain)
            Popup.Error(Hostname.ValidDomain)
            UI.SetFocus(Id("SATELLITE_DOMAIN"))
            next
          end

          # now, values are considered correct
          Builtins.foreach(@settings) do |key, value|
            val = Convert.to_string(UI.QueryWidget(Id(key), :Value))
            Builtins.setenv(key, val, true)
            # write env files
            SCR.Execute(
              path(".target.bash"),
              Builtins.sformat(
                "echo \"export %1='%2'\" >> %3",
                key,
                val,
                @env_file
              )
            )
          end

          break
        end
      end

      deep_copy(@ret)
    end
  end
end

Yast::SusemanagerMigrationClient.new.main
