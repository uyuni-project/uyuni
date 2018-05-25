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

      @product_name = SCR.Read(path(".usr_share_rhn_config_defaults_rhn.product_name"))
      @invalid_pw_chars = "\"$'!"

      @settings = {
        "SATELLITE_HOST"    => "",
        "SATELLITE_DOMAIN"  => Hostname.CurrentDomain,
        "SATELLITE_DB_USER" => "susemanager",
        "SATELLITE_DB_PASS" => "",
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
            _("&Hostname of source #{@product_name} Server"),
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
            _("Source #{@product_name} Database &User Name"),
            Ops.get(@settings, "SATELLITE_DB_USER", "")
          ),
          # text entry label
          Password(
            Id("SATELLITE_DB_PASS"),
            Opt(:hstretch),
            _("Source #{@product_name} Database &Password"),
            Ops.get(@settings, "SATELLITE_DB_PASS", "")
          ),
          # text entry label
          Password(
            Id("SATELLITE_DB_PASS2"),
            Opt(:hstretch),
            _("&Repeat Password"),
            Ops.get(@settings, "SATELLITE_DB_PASS", "")
          ),
          # text entry label
          InputField(
            Id("SATELLITE_DB_SID"),
            Opt(:hstretch),
            _("Source #{@product_name} Database &Name"),
            Ops.get(@settings, "SATELLITE_DB_SID", "")
          ),
          VSpacing(0.5)
        ),
        HSpacing(1)
      )


      @help_text = ""

      # dialog caption
      Wizard.SetContents(
        _("Migration from previous #{@product_name} to new #{@product_name}"),
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
          @hs1 = Convert.to_string(
            UI.QueryWidget(Id("SATELLITE_HOST"), :Value)
          )
          if @hs1 == ""
            Popup.Error(_("Hostname is missing."))
            UI.SetFocus(Id("SATELLITE_HOST"))
            next
          end
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
          if @pw1 != UI.QueryWidget(Id("SATELLITE_DB_PASS2"), :Value)
            Popup.Error(_("Passwords do not match."))
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
