# Copyright (c) 2010-2011 SUSE Linux Products GmbH.
# Licensed under the terms of the MIT license.

Feature: Test XML-RPC "Action Chain" functionality.

   @xmlrpc
   Scenario: Basic chain operations
     Given I am logged in via XML-RPC/actionchain as user "admin" and password "admin"
     When I call XML-RPC/createChain with chainLabel "Quick Brown Fox"
      And I call actionchain.listChains() if label "Quick Brown Fox" is there
     Then I delete the action chain
     And there should be no action chain with the label "Quick Brown Fox".
     When I call XML-RPC/createChain with chainLabel "Quick Brown Fox"
     Then I call actionchain.renameChain() to rename it from "Quick Brown Fox" to "Slow Gray Elephant"
     Then there should be a new action chain with the label "Slow Gray Elephant"
     Then I delete an action chain, labeled "Slow Gray Elephant"
     And there should be no action chain with the label "Slow Gray Elephant"
     And no action chain with the label "Quick Brown Fox".

   @xmlrpc
   Scenario: Schedule operations
     Given I am logged in via XML-RPC/actionchain as user "admin" and password "admin"
     When I call XML-RPC/createChain with chainLabel "Quick Brown Fox"
     When I call actionchain.addPackageInstall()
     When I call actionchain.addPackageRemoval()
     When I call actionchain.addPackageUpgrade()
     When I call actionchain.addPackageVerify()
     When I call actionchain.addScriptRun() with the script like "#!/bin/bash\nexit 1;"
     When I call actionchain.addSystemReboot()
     Then I should be able to see all these actions in the action chain
     When I call actionchain.removeAction on each action within the chain
     Then I should be able to see that the current action chain is empty
     Then I delete the action chain

   @xmlrpc
   Scenario: Run the action chain
     Given I am logged in via XML-RPC/actionchain as user "admin" and password "admin"
     When I call XML-RPC/createChain with chainLabel "Quick Brown Fox"
     When I call actionchain.addSystemReboot()
     Then I should be able to see all these actions in the action chain
     When I schedule the action chain
     Then there should be no more my action chain
      And I should see scheduled action, called "System reboot"
     Then I cancel all scheduled actions
      And there should be no more any scheduled actions
