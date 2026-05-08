---
DocTitle:  Catfish Mockup
---
# Catfish Mockup:
  *  A simple, minimal Redfish Service
  *  For a monolithic Server
  *  Aligned with: OCP Remote Machine Management Spec feature set


# Top Level Description:
  * A Monolithic server:
      * One ComputerSystem
      * One Chassis
      * One Manager
  --Provides basic management features aligned with OCP Remote Machine Management Spec 1.01:
      * Power-on/off/reset
      * Boot to PXE, HDD, BIOS setup (boot override)
      * 4 temp sensors per DCMI (CPU1, CPU2, Board, Inlet)
      * Simple Power Reading, and  DCMI Power Limiting
      * Fan Monitoring w/ redundancy
      * Set asset tag and Indicator LED
      * Basic inventory (serial#, model, SKU, Vendor, BIOS ver…)
      * User Management
      * BMC management: get/set IP, version, enable/disable protocol

# What it does NOT have -- that the Redfish 1.0 model supports
   * No PSUs in model  (RMM spec did not include PSUs) 
   * No ProcessorInfo, MemoryInfo, StorageInfo, System-EthernetInterfaceInfo
   * No Tasks
   * JsonSchema and Registries collections left out (since that is optional)
   * No EventService
   * Remote Machine Management spec used basic PET alerts


# Discussion
   * Opportunity to define some Redfish ‘Integration Recipe’ that specify
      * What APIs and properties are supported / required
      * How to capture

