/*	HubiThings Replica Samsung Dryer Driver
	HubiThings Replica Applications Copyright 2023 by Bloodtick
	Replica RangeOven Copyright 2023 by Dave Gutheinz

	Licensed under the Apache License, Version 2.0 (the "License"); 
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at:
	      http://www.apache.org/licenses/LICENSE-2.0
	Unless required by applicable law or agreed to in writing, software 
	distributed under the License is distributed on an "AS IS" BASIS, 
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
	implied. See the License for the specific language governing 
	permissions and limitations under the License.

Changes:
1.0-1:	
a.	Changed link to help file and moved to library samsungDryerCommon
b.	Added Flex as a child device.
c.	Added logging prefs to library logging.
d.	Changed sendRawCommand to use dataValue replica vice description.

Issues with this driver: Contact davegut via Private Message on the
Hubitat Community site: https://community.hubitat.com/
==========================================================================*/
def driverVer() { return "1.0-1" }
def appliance() { return "ReplicaSamsungDryer" }

metadata {
	definition (name: appliance(),
				namespace: "replica",
				author: "David Gutheinz",
				importUrl: "https://raw.githubusercontent.com/DaveGut/HubithingsReplica/main/Drivers/${appliance()}.groovy"
			   ){
		capability "Configuration"
		attribute "healthStatus", "enum", ["offline", "online"]
		attribute "lockState", "string"
		attribute "dryerDryLevel", "string"
		attribute "dryerWrinklePrevent", "string"
		attribute "jobBeginningStatus", "string"
	}
	preferences {
	}
}

//	===== Installation, setup and update =====
def installed() {
	updateDataValue("componentId", "main")
	runIn(1, updated)
}

def updated() {
	unschedule()
	pauseExecution(2000)
	def updStatus = [:]
	if (!getDataValue("driverVersion") || getDataValue("driverVersion") != driverVer()) {
		updateDataValue("driverVersion", driverVer())
		updStatus << [driverVer: driverVer()]
	}
	if (logEnable) { runIn(1800, debugLogOff) }
	if (traceLog) { runIn(600, traceLogOff) }
	updStatus << [logEnable: logEnable, infoLog: infoLog, traceLog: traceLog]
	runIn(3, configure)
	logInfo("updated: ${updStatus}")
}

def designCapabilities() {
	return ["refresh", "remoteControlStatus", "samsungce.kidsLock", 
			"switch", "dryerOperatingState",  "custom.dryerDryLevel", 
			"custom.dryerWrinklePrevent", "custom.jobBeginningStatus"
			]
}

Map designChildren() { return ["sub": "Flex"] }

def sendRawCommand(component, capability, command, arguments = []) {
	Map status = [:]
	def rcEnabled = device.currentValue("remoteControlEnabled")
	if (rcEnabled) {
		def deviceId = new JSONObject(getDataValue("replica")).deviceId
		def cmdStatus = parent.setSmartDeviceCommand(deviceId, component, capability, command, arguments)
		def cmdData = [component, capability, command, arguments, cmdStatus]
		status << [cmdData: cmdData]
	} else {
		status << [FAILED: [rcEnabled: rcEnabled]]
	}
	return status
}

//	===== Libraries =====




// ~~~~~ start include (1314) replica.samsungDryerCommon ~~~~~
library ( // library marker replica.samsungDryerCommon, line 1
	name: "samsungDryerCommon", // library marker replica.samsungDryerCommon, line 2
	namespace: "replica", // library marker replica.samsungDryerCommon, line 3
	author: "Dave Gutheinz", // library marker replica.samsungDryerCommon, line 4
	description: "Common Methods for replica Samsung Oven parent/children", // library marker replica.samsungDryerCommon, line 5
	category: "utilities", // library marker replica.samsungDryerCommon, line 6
	documentationLink: "" // library marker replica.samsungDryerCommon, line 7
) // library marker replica.samsungDryerCommon, line 8
//	Version 1.0-2 // library marker replica.samsungDryerCommon, line 9

//	===== Common Capabilities, Commands, and Attributes ===== // library marker replica.samsungDryerCommon, line 11
capability "Refresh" // library marker replica.samsungDryerCommon, line 12
attribute "remoteControlEnabled", "boolean" // library marker replica.samsungDryerCommon, line 13
attribute "switch", "string" // library marker replica.samsungDryerCommon, line 14
attribute "completionTime", "string" // library marker replica.samsungDryerCommon, line 15
attribute "machineState", "string" // library marker replica.samsungDryerCommon, line 16
attribute "dryerJobState", "string" // library marker replica.samsungDryerCommon, line 17
command "start" // library marker replica.samsungDryerCommon, line 18
command "pause" // library marker replica.samsungDryerCommon, line 19
command "stop" // library marker replica.samsungDryerCommon, line 20
attribute "timeRemaining", "string" // library marker replica.samsungDryerCommon, line 21

String helpLogo() { // library marker replica.samsungDryerCommon, line 23
	return """<a href="https://github.com/DaveGut/HubithingsReplica/blob/main/Docs/SamsungDryerReadme.md">""" + // library marker replica.samsungDryerCommon, line 24
		"""<div style="position: absolute; top: 20px; right: 150px; height: 80px; font-size: 28px;">Dryer Help</div></a>""" // library marker replica.samsungDryerCommon, line 25
} // library marker replica.samsungDryerCommon, line 26

//	===== Device Commands ===== // library marker replica.samsungDryerCommon, line 28
def start() { setMachineState("run") } // library marker replica.samsungDryerCommon, line 29
def pause() { setMachineState("pause") } // library marker replica.samsungDryerCommon, line 30
def stop() { setMachineState("stop") } // library marker replica.samsungDryerCommon, line 31
def setMachineState(machState) { // library marker replica.samsungDryerCommon, line 32
	def oldState = device.currentValue("machineState") // library marker replica.samsungDryerCommon, line 33
	Map cmdStatus = [oldState: oldState, newState: machState] // library marker replica.samsungDryerCommon, line 34
	if (oldState != machState) { // library marker replica.samsungDryerCommon, line 35
		cmdStatus << sendRawCommand(getDataValue("componentId"), "dryerOperatingState",  // library marker replica.samsungDryerCommon, line 36
									"setMachineState", [machState]) // library marker replica.samsungDryerCommon, line 37
	} else { // library marker replica.samsungDryerCommon, line 38
		cmdStatus << [FAILED: "no change in state"] // library marker replica.samsungDryerCommon, line 39
		runIn(10, checkAttribute, [data: ["setMachineState", "machineState", machState]]) // library marker replica.samsungDryerCommon, line 40
	} // library marker replica.samsungDryerCommon, line 41
	logInfo("setMachineState: ${cmdStatus}") // library marker replica.samsungDryerCommon, line 42
} // library marker replica.samsungDryerCommon, line 43

def checkAttribute(setCommand, attrName, attrValue) { // library marker replica.samsungDryerCommon, line 45
	def checkValue = device.currentValue(attrName).toString() // library marker replica.samsungDryerCommon, line 46
	if (checkValue != attrValue.toString()) { // library marker replica.samsungDryerCommon, line 47
		Map warnTxt = [command: setCommand, // library marker replica.samsungDryerCommon, line 48
					   attribute: attrName, // library marker replica.samsungDryerCommon, line 49
					   checkValue: checkValue, // library marker replica.samsungDryerCommon, line 50
					   attrValue: attrValue, // library marker replica.samsungDryerCommon, line 51
					   failed: "Function may be disabled by SmartThings"] // library marker replica.samsungDryerCommon, line 52
		logWarn("checkAttribute: ${warnTxt}") // library marker replica.samsungDryerCommon, line 53
	} // library marker replica.samsungDryerCommon, line 54
} // library marker replica.samsungDryerCommon, line 55

def parseEvent(event) { // library marker replica.samsungDryerCommon, line 57
	logDebug("parseEvent: <b>${event}</b>") // library marker replica.samsungDryerCommon, line 58
	if (state.deviceCapabilities.contains(event.capability)) { // library marker replica.samsungDryerCommon, line 59
		logTrace("parseEvent: <b>${event}</b>") // library marker replica.samsungDryerCommon, line 60
		if (event.value != null) { // library marker replica.samsungDryerCommon, line 61
			switch(event.attribute) { // library marker replica.samsungDryerCommon, line 62
				case "completionTime": // library marker replica.samsungDryerCommon, line 63
					setEvent(event) // library marker replica.samsungDryerCommon, line 64
					def timeRemaining = calcTimeRemaining(event.value) // library marker replica.samsungDryerCommon, line 65
					setEvent([attribute: "timeRemaining", value: timeRemaining, unit: null]) // library marker replica.samsungDryerCommon, line 66
					break // library marker replica.samsungDryerCommon, line 67
				case "supportedMachineStates": // library marker replica.samsungDryerCommon, line 68
				case "supportedDryerDryLevel": // library marker replica.samsungDryerCommon, line 69
				case "operatingState": // library marker replica.samsungDryerCommon, line 70
				case "supportedDryingTime": // library marker replica.samsungDryerCommon, line 71
					break				 // library marker replica.samsungDryerCommon, line 72
				default: // library marker replica.samsungDryerCommon, line 73
					setEvent(event) // library marker replica.samsungDryerCommon, line 74
					break // library marker replica.samsungDryerCommon, line 75
			} // library marker replica.samsungDryerCommon, line 76
		} // library marker replica.samsungDryerCommon, line 77
	} // library marker replica.samsungDryerCommon, line 78
} // library marker replica.samsungDryerCommon, line 79

def setEvent(event) { // library marker replica.samsungDryerCommon, line 81
	logTrace("<b>setEvent</b>: ${event}") // library marker replica.samsungDryerCommon, line 82
	sendEvent(name: event.attribute, value: event.value, unit: event.unit) // library marker replica.samsungDryerCommon, line 83
	if (device.currentValue(event.attribute).toString() != event.value.toString()) { // library marker replica.samsungDryerCommon, line 84
		logInfo("setEvent: [event: ${event}]") // library marker replica.samsungDryerCommon, line 85
	} // library marker replica.samsungDryerCommon, line 86
} // library marker replica.samsungDryerCommon, line 87

def calcTimeRemaining(completionTime) { // library marker replica.samsungDryerCommon, line 89
	Integer currTime = now() // library marker replica.samsungDryerCommon, line 90
	Integer compTime // library marker replica.samsungDryerCommon, line 91
	try { // library marker replica.samsungDryerCommon, line 92
		compTime = Date.parse("yyyy-MM-dd'T'HH:mm:ss'Z'", completionTime,TimeZone.getTimeZone('UTC')).getTime() // library marker replica.samsungDryerCommon, line 93
	} catch (e) { // library marker replica.samsungDryerCommon, line 94
		compTime = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", completionTime,TimeZone.getTimeZone('UTC')).getTime() // library marker replica.samsungDryerCommon, line 95
	} // library marker replica.samsungDryerCommon, line 96
	Integer timeRemaining = ((compTime-currTime) /1000).toInteger() // library marker replica.samsungDryerCommon, line 97
	def hhmmss // library marker replica.samsungDryerCommon, line 98
	if (timeRemaining < 0) { // library marker replica.samsungDryerCommon, line 99
		hhmmss = "00:00:00" // library marker replica.samsungDryerCommon, line 100
    } else { // library marker replica.samsungDryerCommon, line 101
		hhmmss = new GregorianCalendar( 0, 0, 0, 0, 0, timeRemaining, 0 ).time.format( 'HH:mm:ss' ) // library marker replica.samsungDryerCommon, line 102
	} // library marker replica.samsungDryerCommon, line 103
	return hhmmss // library marker replica.samsungDryerCommon, line 104
} // library marker replica.samsungDryerCommon, line 105

// ~~~~~ end include (1314) replica.samsungDryerCommon ~~~~~

// ~~~~~ start include (1305) replica.samsungReplicaCommon ~~~~~
library ( // library marker replica.samsungReplicaCommon, line 1
	name: "samsungReplicaCommon", // library marker replica.samsungReplicaCommon, line 2
	namespace: "replica", // library marker replica.samsungReplicaCommon, line 3
	author: "Dave Gutheinz", // library marker replica.samsungReplicaCommon, line 4
	description: "Common Methods for replica Samsung Appliances", // library marker replica.samsungReplicaCommon, line 5
	category: "utilities", // library marker replica.samsungReplicaCommon, line 6
	documentationLink: "" // library marker replica.samsungReplicaCommon, line 7
) // library marker replica.samsungReplicaCommon, line 8
//	version 1.0 // library marker replica.samsungReplicaCommon, line 9

import org.json.JSONObject // library marker replica.samsungReplicaCommon, line 11
import groovy.json.JsonOutput // library marker replica.samsungReplicaCommon, line 12
import groovy.json.JsonSlurper // library marker replica.samsungReplicaCommon, line 13

def configure() { // library marker replica.samsungReplicaCommon, line 15
	Map logData = [:] // library marker replica.samsungReplicaCommon, line 16
    updateDataValue("triggers", groovy.json.JsonOutput.toJson(getReplicaTriggers())) // library marker replica.samsungReplicaCommon, line 17
    updateDataValue("commands", groovy.json.JsonOutput.toJson(getReplicaCommands())) // library marker replica.samsungReplicaCommon, line 18
	updateDataValue("rules", getReplicaRules()) // library marker replica.samsungReplicaCommon, line 19
	logData << [triggers: "initialized", commands: "initialized", rules: "initialized"] // library marker replica.samsungReplicaCommon, line 20
	logData << [replicaRules: "initialized"] // library marker replica.samsungReplicaCommon, line 21
	state.checkCapabilities = true // library marker replica.samsungReplicaCommon, line 22
	sendCommand("configure") // library marker replica.samsungReplicaCommon, line 23
	logData: [device: "configuring HubiThings"] // library marker replica.samsungReplicaCommon, line 24
	runIn(5, listAttributes,[data:true]) // library marker replica.samsungReplicaCommon, line 25
	logInfo("configure: ${logData}") // library marker replica.samsungReplicaCommon, line 26
} // library marker replica.samsungReplicaCommon, line 27

Map getReplicaCommands() { // library marker replica.samsungReplicaCommon, line 29
    return (["replicaEvent":[[name:"parent*",type:"OBJECT"],[name:"event*",type:"JSON_OBJECT"]],  // library marker replica.samsungReplicaCommon, line 30
			 "replicaStatus":[[name:"parent*",type:"OBJECT"],[name:"event*",type:"JSON_OBJECT"]],  // library marker replica.samsungReplicaCommon, line 31
			 "replicaHealth":[[name:"parent*",type:"OBJECT"],[name:"health*",type:"JSON_OBJECT"]], // library marker replica.samsungReplicaCommon, line 32
			 "setHealthStatusValue":[[name:"healthStatus*",type:"ENUM"]]]) // library marker replica.samsungReplicaCommon, line 33
} // library marker replica.samsungReplicaCommon, line 34

Map getReplicaTriggers() { // library marker replica.samsungReplicaCommon, line 36
	return [refresh:[], deviceRefresh: []] // library marker replica.samsungReplicaCommon, line 37
} // library marker replica.samsungReplicaCommon, line 38

String getReplicaRules() { // library marker replica.samsungReplicaCommon, line 40
	return """{"version":1,"components":[{"trigger":{"type":"attribute","properties":{"value":{"title":"HealthState","type":"string"}},"additionalProperties":false,"required":["value"],"capability":"healthCheck","attribute":"healthStatus","label":"attribute: healthStatus.*"},"command":{"name":"setHealthStatusValue","label":"command: setHealthStatusValue(healthStatus*)","type":"command","parameters":[{"name":"healthStatus*","type":"ENUM"}]},"type":"smartTrigger","mute":true},{"trigger":{"name":"refresh","label":"command: refresh()","type":"command"},"command":{"name":"refresh","type":"command","capability":"refresh","label":"command: refresh()"},"type":"hubitatTrigger"},{"trigger":{"name":"deviceRefresh","label":"command: deviceRefresh()","type":"command"},"command":{"name":"refresh","type":"command","capability":"refresh","label":"command: refresh()"},"type":"hubitatTrigger"}]}""" // library marker replica.samsungReplicaCommon, line 41
} // library marker replica.samsungReplicaCommon, line 42

//	===== Event Parse Interface s===== // library marker replica.samsungReplicaCommon, line 44
void replicaStatus(def parent=null, Map status=null) { // library marker replica.samsungReplicaCommon, line 45
	def logData = [parent: parent, status: status] // library marker replica.samsungReplicaCommon, line 46
	if (state.checkCapabilities) { // library marker replica.samsungReplicaCommon, line 47
		runIn(10, checkCapabilities, [data: status.components]) // library marker replica.samsungReplicaCommon, line 48
	} else if (state.refreshAttributes) { // library marker replica.samsungReplicaCommon, line 49
		refreshAttributes(status.components) // library marker replica.samsungReplicaCommon, line 50
	} // library marker replica.samsungReplicaCommon, line 51
	logDebug("replicaStatus: ${logData}") // library marker replica.samsungReplicaCommon, line 52
} // library marker replica.samsungReplicaCommon, line 53

def checkCapabilities(components) { // library marker replica.samsungReplicaCommon, line 55
	state.checkCapabilities = false // library marker replica.samsungReplicaCommon, line 56
	def componentId = getDataValue("componentId") // library marker replica.samsungReplicaCommon, line 57
	def disabledCapabilities = [] // library marker replica.samsungReplicaCommon, line 58
	try { // library marker replica.samsungReplicaCommon, line 59
		disabledCapabilities << components[componentId]["custom.disabledCapabilities"].disabledCapabilities.value // library marker replica.samsungReplicaCommon, line 60
	} catch (e) { } // library marker replica.samsungReplicaCommon, line 61

	def enabledCapabilities = [] // library marker replica.samsungReplicaCommon, line 63
	Map description // library marker replica.samsungReplicaCommon, line 64
	try { // library marker replica.samsungReplicaCommon, line 65
		description = new JsonSlurper().parseText(getDataValue("description")) // library marker replica.samsungReplicaCommon, line 66
	} catch (error) { // library marker replica.samsungReplicaCommon, line 67
		logWarn("checkCapabilities.  Data element Description not loaded. Run Configure") // library marker replica.samsungReplicaCommon, line 68
	} // library marker replica.samsungReplicaCommon, line 69
	def thisComponent = description.components.find { it.id == componentId } // library marker replica.samsungReplicaCommon, line 70
	thisComponent.capabilities.each { capability -> // library marker replica.samsungReplicaCommon, line 71
		if (designCapabilities().contains(capability.id) && // library marker replica.samsungReplicaCommon, line 72
			!disabledCapabilities.contains(capability.id)) { // library marker replica.samsungReplicaCommon, line 73
			enabledCapabilities << capability.id // library marker replica.samsungReplicaCommon, line 74
		} // library marker replica.samsungReplicaCommon, line 75
	} // library marker replica.samsungReplicaCommon, line 76
	state.deviceCapabilities = enabledCapabilities // library marker replica.samsungReplicaCommon, line 77
	runIn(1, configureChildren, [data: components]) // library marker replica.samsungReplicaCommon, line 78
	runIn(5, refreshAttributes, [data: components]) // library marker replica.samsungReplicaCommon, line 79
	logInfo("checkCapabilities: [design: ${designCapabilities()}, disabled: ${disabledCapabilities}, enabled: ${enabledCapabilities}]") // library marker replica.samsungReplicaCommon, line 80
} // library marker replica.samsungReplicaCommon, line 81

//	===== Child Configure / Install ===== // library marker replica.samsungReplicaCommon, line 83
def configureChildren(components) { // library marker replica.samsungReplicaCommon, line 84
	def logData = [:] // library marker replica.samsungReplicaCommon, line 85
	def componentId = getDataValue("componentId") // library marker replica.samsungReplicaCommon, line 86
	def disabledComponents = [] // library marker replica.samsungReplicaCommon, line 87
	try { // library marker replica.samsungReplicaCommon, line 88
		disabledComponents << components[componentId]["custom.disabledComponents"].disabledComponents.value // library marker replica.samsungReplicaCommon, line 89
	} catch (e) { } // library marker replica.samsungReplicaCommon, line 90
	designChildren().each { designChild -> // library marker replica.samsungReplicaCommon, line 91
		if (disabledComponents.contains(designChild.key)) { // library marker replica.samsungReplicaCommon, line 92
			logData << ["${designChild.key}": [status: "SmartThingsDisabled"]] // library marker replica.samsungReplicaCommon, line 93
		} else { // library marker replica.samsungReplicaCommon, line 94
			def dni = device.getDeviceNetworkId() // library marker replica.samsungReplicaCommon, line 95
			def childDni = "${dni}-${designChild.key}" // library marker replica.samsungReplicaCommon, line 96
			def child = getChildDevice(childDni) // library marker replica.samsungReplicaCommon, line 97
			def name = "${device.displayName} ${designChild.key}" // library marker replica.samsungReplicaCommon, line 98
			if (child == null) { // library marker replica.samsungReplicaCommon, line 99
				def type = "${appliance()}${designChild.value}" // library marker replica.samsungReplicaCommon, line 100
				try { // library marker replica.samsungReplicaCommon, line 101
					addChildDevice("replica", "${type}", "${childDni}", [ // library marker replica.samsungReplicaCommon, line 102
						name: type,  // library marker replica.samsungReplicaCommon, line 103
						label: name, // library marker replica.samsungReplicaCommon, line 104
						componentId: designChild.key // library marker replica.samsungReplicaCommon, line 105
					]) // library marker replica.samsungReplicaCommon, line 106
					logData << ["${name}": [status: "installed"]] // library marker replica.samsungReplicaCommon, line 107
				} catch (error) { // library marker replica.samsungReplicaCommon, line 108
					logData << ["${name}": [status: "FAILED", reason: error]] // library marker replica.samsungReplicaCommon, line 109
				} // library marker replica.samsungReplicaCommon, line 110
			} else { // library marker replica.samsungReplicaCommon, line 111
				child.checkCapabilities(components) // library marker replica.samsungReplicaCommon, line 112
				logData << ["${name}": [status: "already installed"]] // library marker replica.samsungReplicaCommon, line 113
			} // library marker replica.samsungReplicaCommon, line 114
		} // library marker replica.samsungReplicaCommon, line 115
	} // library marker replica.samsungReplicaCommon, line 116
	runIn(1, checkChildren, [data: components]) // library marker replica.samsungReplicaCommon, line 117
	runIn(3, refreshAttributes, [data: components]) // library marker replica.samsungReplicaCommon, line 118
	logInfo("configureChildren: ${logData}") // library marker replica.samsungReplicaCommon, line 119
} // library marker replica.samsungReplicaCommon, line 120

def checkChildren(components) { // library marker replica.samsungReplicaCommon, line 122
	getChildDevices().each { // library marker replica.samsungReplicaCommon, line 123
		it.checkCapabilities(components) // library marker replica.samsungReplicaCommon, line 124
	} // library marker replica.samsungReplicaCommon, line 125
} // library marker replica.samsungReplicaCommon, line 126

//	===== Attributes // library marker replica.samsungReplicaCommon, line 128
def refreshAttributes(components) { // library marker replica.samsungReplicaCommon, line 129
	state.refreshAttributes = false // library marker replica.samsungReplicaCommon, line 130
	def component = components."${getDataValue("componentId")}" // library marker replica.samsungReplicaCommon, line 131
	logDebug("refreshAttributes: ${component}") // library marker replica.samsungReplicaCommon, line 132
	component.each { capability -> // library marker replica.samsungReplicaCommon, line 133
		capability.value.each { attribute -> // library marker replica.samsungReplicaCommon, line 134
			parseEvent([capability: capability.key, // library marker replica.samsungReplicaCommon, line 135
						attribute: attribute.key, // library marker replica.samsungReplicaCommon, line 136
						value: attribute.value.value, // library marker replica.samsungReplicaCommon, line 137
						unit: attribute.value.unit]) // library marker replica.samsungReplicaCommon, line 138
			pauseExecution(50) // library marker replica.samsungReplicaCommon, line 139
		} // library marker replica.samsungReplicaCommon, line 140
	} // library marker replica.samsungReplicaCommon, line 141
	getChildDevices().each { // library marker replica.samsungReplicaCommon, line 142
		it.refreshAttributes(components) // library marker replica.samsungReplicaCommon, line 143
	} // library marker replica.samsungReplicaCommon, line 144
} // library marker replica.samsungReplicaCommon, line 145

void replicaHealth(def parent=null, Map health=null) { // library marker replica.samsungReplicaCommon, line 147
	if(parent) { logInfo("replicaHealth: ${parent?.getLabel()}") } // library marker replica.samsungReplicaCommon, line 148
	if(health) { logInfo("replicaHealth: ${health}") } // library marker replica.samsungReplicaCommon, line 149
} // library marker replica.samsungReplicaCommon, line 150

def setHealthStatusValue(value) {     // library marker replica.samsungReplicaCommon, line 152
    sendEvent(name: "healthStatus", value: value, descriptionText: "${device.displayName} healthStatus set to $value") // library marker replica.samsungReplicaCommon, line 153
} // library marker replica.samsungReplicaCommon, line 154

void replicaEvent(def parent=null, Map event=null) { // library marker replica.samsungReplicaCommon, line 156
	if (event && event.deviceEvent.componentId == getDataValue("componentId")) { // library marker replica.samsungReplicaCommon, line 157
		try { // library marker replica.samsungReplicaCommon, line 158
			parseEvent(event.deviceEvent) // library marker replica.samsungReplicaCommon, line 159
		} catch (err) { // library marker replica.samsungReplicaCommon, line 160
			logWarn("replicaEvent: [event = ${event}, error: ${err}") // library marker replica.samsungReplicaCommon, line 161
		} // library marker replica.samsungReplicaCommon, line 162
	} else { // library marker replica.samsungReplicaCommon, line 163
		getChildDevices().each {  // library marker replica.samsungReplicaCommon, line 164
			it.parentEvent(event) // library marker replica.samsungReplicaCommon, line 165
		} // library marker replica.samsungReplicaCommon, line 166
	} // library marker replica.samsungReplicaCommon, line 167
} // library marker replica.samsungReplicaCommon, line 168

def sendCommand(String name, def value=null, String unit=null, data=[:]) { // library marker replica.samsungReplicaCommon, line 170
    parent?.deviceTriggerHandler(device, [name:name, value:value, unit:unit, data:data, now:now]) // library marker replica.samsungReplicaCommon, line 171
} // library marker replica.samsungReplicaCommon, line 172

//	===== Refresh Commands ===== // library marker replica.samsungReplicaCommon, line 174
def refresh() { // library marker replica.samsungReplicaCommon, line 175
	logDebug("refresh") // library marker replica.samsungReplicaCommon, line 176
	state.refreshAttributes = true // library marker replica.samsungReplicaCommon, line 177
	sendCommand("deviceRefresh") // library marker replica.samsungReplicaCommon, line 178
	runIn(1, sendCommand, [data: ["refresh"]]) // library marker replica.samsungReplicaCommon, line 179
} // library marker replica.samsungReplicaCommon, line 180

def deviceRefresh() { // library marker replica.samsungReplicaCommon, line 182
	sendCommand("deviceRefresh") // library marker replica.samsungReplicaCommon, line 183
} // library marker replica.samsungReplicaCommon, line 184

// ~~~~~ end include (1305) replica.samsungReplicaCommon ~~~~~

// ~~~~~ start include (1303) davegut.Logging ~~~~~
library ( // library marker davegut.Logging, line 1
	name: "Logging", // library marker davegut.Logging, line 2
	namespace: "davegut", // library marker davegut.Logging, line 3
	author: "Dave Gutheinz", // library marker davegut.Logging, line 4
	description: "Common Logging Methods", // library marker davegut.Logging, line 5
	category: "utilities", // library marker davegut.Logging, line 6
	documentationLink: "" // library marker davegut.Logging, line 7
) // library marker davegut.Logging, line 8

preferences { // library marker davegut.Logging, line 10
	input ("logEnable", "bool",  title: "Enable debug logging for 30 minutes", defaultValue: false) // library marker davegut.Logging, line 11
	input ("infoLog", "bool", title: "Enable information logging${helpLogo()}",defaultValue: true) // library marker davegut.Logging, line 12
	input ("traceLog", "bool", title: "Enable trace logging as directed by developer", defaultValue: false) // library marker davegut.Logging, line 13
} // library marker davegut.Logging, line 14

//	Logging during development // library marker davegut.Logging, line 16
def listAttributes(trace = false) { // library marker davegut.Logging, line 17
	def attrs = device.getSupportedAttributes() // library marker davegut.Logging, line 18
	def attrList = [:] // library marker davegut.Logging, line 19
	attrs.each { // library marker davegut.Logging, line 20
		def val = device.currentValue("${it}") // library marker davegut.Logging, line 21
		attrList << ["${it}": val] // library marker davegut.Logging, line 22
	} // library marker davegut.Logging, line 23
	if (trace == true) { // library marker davegut.Logging, line 24
		logInfo("Attributes: ${attrList}") // library marker davegut.Logging, line 25
	} else { // library marker davegut.Logging, line 26
		logDebug("Attributes: ${attrList}") // library marker davegut.Logging, line 27
	} // library marker davegut.Logging, line 28
} // library marker davegut.Logging, line 29

def logTrace(msg){ // library marker davegut.Logging, line 31
	if (traceLog == true) { // library marker davegut.Logging, line 32
		log.trace "${device.displayName}-${driverVer()}: ${msg}" // library marker davegut.Logging, line 33
	} // library marker davegut.Logging, line 34
} // library marker davegut.Logging, line 35

def traceLogOff() { // library marker davegut.Logging, line 37
	device.updateSetting("traceLog", [type:"bool", value: false]) // library marker davegut.Logging, line 38
	logInfo("traceLogOff") // library marker davegut.Logging, line 39
} // library marker davegut.Logging, line 40

def logInfo(msg) {  // library marker davegut.Logging, line 42
	if (textEnable || infoLog) { // library marker davegut.Logging, line 43
		log.info "${device.displayName}-${driverVer()}: ${msg}" // library marker davegut.Logging, line 44
	} // library marker davegut.Logging, line 45
} // library marker davegut.Logging, line 46

def debugLogOff() { // library marker davegut.Logging, line 48
	device.updateSetting("logEnable", [type:"bool", value: false]) // library marker davegut.Logging, line 49
	logInfo("debugLogOff") // library marker davegut.Logging, line 50
} // library marker davegut.Logging, line 51

def logDebug(msg) { // library marker davegut.Logging, line 53
	if (logEnable || debugLog) { // library marker davegut.Logging, line 54
		log.debug "${device.displayName}-${driverVer()}: ${msg}" // library marker davegut.Logging, line 55
	} // library marker davegut.Logging, line 56
} // library marker davegut.Logging, line 57

def logWarn(msg) { log.warn "${device.displayName}-${driverVer()}: ${msg}" } // library marker davegut.Logging, line 59

// ~~~~~ end include (1303) davegut.Logging ~~~~~
