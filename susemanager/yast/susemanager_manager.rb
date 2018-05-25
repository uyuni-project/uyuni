# encoding: utf-8

module Yast
  class SusemanagerManagerClient < Client
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

      @migration_file = Ops.add(Directory.tmpdir, "/susemanager_migration")
      @migration = FileUtils.Exists(@migration_file)
      @product_name = SCR.Read(path(".usr_share_rhn_config_defaults_rhn.product_name")) || "SUSE Manager"

      @settings = {
        "MANAGER_IP"          => "",
        "MANAGER_ADMIN_EMAIL" => Ops.add("susemanager@", Hostname.CurrentDomain),
        "ACTIVATE_SLP"        => "n"
      }
      if !@migration
        Ops.set(@settings, "MANAGER_ENABLE_TFTP", "y")
      end

      @env_file = Ops.add(Directory.tmpdir, "/env_manager")
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

      if Ops.get(@settings, "MANAGER_IP", "") == ""
        @out = Convert.to_map(
          SCR.Execute(
            path(".target.bash_output"),
            "ip -f inet -o addr show scope global | awk '{print $4}' | awk -F / '{print $1}'"
          )
        )
        Ops.set(
          @settings,
          "MANAGER_IP",
          Ops.get(
            Builtins.splitstring(Ops.get_string(@out, "stdout", ""), "\n"),
            0,
            ""
          )
        )
      end

      @contents = HBox(
        HSpacing(1),
        VBox(
          @migration ?
            VBox(
              InputField(
                Id("MANAGER_IP"),
                Opt(:hstretch),
                # text entry label
                _("&IP Address of the #{@product_name} Server"),
                Ops.get(@settings, "MANAGER_IP", "")
              ),
            VSpacing(0.5),
            ) :
            VBox(),
          InputField(
            Id("MANAGER_ADMIN_EMAIL"),
            Opt(:hstretch),
            # text entry label
            _("#{@product_name} &Administrator E-mail Address"),
            Ops.get(@settings, "MANAGER_ADMIN_EMAIL", "")
          ),
          VSpacing(0.5),
          Left(
            CheckBox(
              Id("ACTIVATE_SLP"),
              _("Advertise #{@product_name} via SLP"),
              false
            )
          )
        ),
        HSpacing(1)
      )


      # help text
      @help_text = _(
        "<p>Fill in <b>Administrator E-mail Address</b>. It is used for notifications by #{@product_name}.</p>
         <p>By checking SLP (Service Location Protocol) #{@product_name} will advertise its service in the
           network so it can easily be found by client systems.</p>"
      )

      # dialog caption
      Wizard.SetContents(
        "#{@product_name}",
        @contents,
        @help_text,
        Ops.get_boolean(@args, "enable_back", true),
        Ops.get_boolean(@args, "enable_next", true)
      )

      UI.SetFocus(Id("MANAGER_ADMIN_EMAIL"))
      UI.SetFocus(Id("MANAGER_IP")) if UI.WidgetExists(Id("MANAGER_IP"))

      @ret = :back
      while true
        @ret = UI.UserInput
        break if @ret == :back
        break if @ret == :abort && Popup.ConfirmAbort(:incomplete)
        if @ret == :next
          if UI.WidgetExists(Id("MANAGER_IP")) &&
              !IP.Check(
                Convert.to_string(UI.QueryWidget(Id("MANAGER_IP"), :Value))
              )
            Popup.Error(IP.Valid4)
            UI.SetFocus(Id("MANAGER_IP"))
            next
          end

          if UI.WidgetExists(Id("MANAGER_ADMIN_EMAIL"))
            @email = Convert.to_string(
              UI.QueryWidget(Id("MANAGER_ADMIN_EMAIL"), :Value)
            )
            if !Builtins.issubstring(@email, "@") ||
                Builtins.find(@email, "@") == 0 ||
                Builtins.find(@email, "@") ==
                  Ops.subtract(Builtins.size(@email), 1)
              Popup.Error(_("The Administrator E-mail Address is not valid."))
              UI.SetFocus(Id("MANAGER_ADMIN_EMAIL"))
              next
            end
          end

          # now, values are considered correct
          Builtins.foreach(@settings) do |key, value|
            val = value
            if UI.WidgetExists(Id(key))
              if key == "ACTIVATE_SLP"
                val = UI.QueryWidget(Id(key), :Value) == true ? "y" : "n"
              else
                val = Convert.to_string(UI.QueryWidget(Id(key), :Value))
              end
            end
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

Yast::SusemanagerManagerClient.new.main
