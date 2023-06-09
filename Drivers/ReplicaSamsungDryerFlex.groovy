/*	HubiThings Replica Samsung Dryer Flex Driver
	HubiThings Replica Applications Copyright 2023 by Bloodtick
	Replica Dryer Flex Copyright 2023 by Dave Gutheinz

	See parent driver for added information.
==========================================================================*/
def driverVer() { return "1.0-1" }
def appliance() { return "ReplicaSamsungDryerFlex" }

metadata {
	definition (name: appliance(),
				namespace: "replica",
				author: "David Gutheinz",
				importUrl: "https://raw.githubusercontent.com/DaveGut/HubithingsReplica/main/Drivers/${appliance()}.groovy"
			   ){
	}
	preferences {
	}
}

//	===== Installation, setup and update =====
def installed() {
	runIn(1, updated)
}

def updated() {
	unschedule()
	def updStatus = [:]
	if (!getDataValue("driverVersion") || getDataValue("driverVersion") != driverVer()) {
		updateDataValue("driverVersion", driverVer())
		updStatus << [driverVer: driverVer()]
	}
	if (logEnable) { runIn(1800, debugLogOff) }
	if (traceLog) { runIn(600, traceLogOff) }
	updStatus << [logEnable: logEnable, infoLog: infoLog, traceLog: traceLog]
	listAttributes(true)
	logInfo("updated: ${updStatus}")
}

def designCapabilities() {
	return ["refresh", "remoteControlStatus", "switch", "dryerOperatingState"]
}

def sendRawCommand(component, capability, command, arguments = []) {
	Map status = [:]
	def rcEnabled = device.currentValue("remoteControlEnabled")
	if (rcEnabled) {
		status << parent.sendRawCommand(component, capability, command, arguments)
	} else {
		status << [FAILED: [cavityInst: cavityInst]]
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

// ~~~~~ start include (1304) replica.samsungReplicaChildCommon ~~~~~
library ( // library marker replica.samsungReplicaChildCommon, line 1
	name: "samsungReplicaChildCommon", // library marker replica.samsungReplicaChildCommon, line 2
	namespace: "replica", // library marker replica.samsungReplicaChildCommon, line 3
	author: "Dave Gutheinz", // library marker replica.samsungReplicaChildCommon, line 4
	description: "Common Methods for replica Samsung Appliances children", // library marker replica.samsungReplicaChildCommon, line 5
	category: "utilities", // library marker replica.samsungReplicaChildCommon, line 6
	documentationLink: "" // library marker replica.samsungReplicaChildCommon, line 7
) // library marker replica.samsungReplicaChildCommon, line 8
//	Version 1.0 // library marker replica.samsungReplicaChildCommon, line 9
import groovy.json.JsonSlurper // library marker replica.samsungReplicaChildCommon, line 10

def checkCapabilities(components) { // library marker replica.samsungReplicaChildCommon, line 12
	def componentId = getDataValue("componentId") // library marker replica.samsungReplicaChildCommon, line 13
	def disabledCapabilities = [] // library marker replica.samsungReplicaChildCommon, line 14
	try { // library marker replica.samsungReplicaChildCommon, line 15
		disabledCapabilities << components[componentId]["custom.disabledCapabilities"].disabledCapabilities.value // library marker replica.samsungReplicaChildCommon, line 16
	} catch (e) { } // library marker replica.samsungReplicaChildCommon, line 17
	def enabledCapabilities = [] // library marker replica.samsungReplicaChildCommon, line 18
	Map description = new JsonSlurper().parseText(parent.getDataValue("description")) // library marker replica.samsungReplicaChildCommon, line 19
	def descComponent = description.components.find { it.id == componentId } // library marker replica.samsungReplicaChildCommon, line 20
	descComponent.capabilities.each { capability -> // library marker replica.samsungReplicaChildCommon, line 21
		if (designCapabilities().contains(capability.id) && // library marker replica.samsungReplicaChildCommon, line 22
			!disabledCapabilities.contains(capability.id)) { // library marker replica.samsungReplicaChildCommon, line 23
			enabledCapabilities << capability.id // library marker replica.samsungReplicaChildCommon, line 24
		} // library marker replica.samsungReplicaChildCommon, line 25
	} // library marker replica.samsungReplicaChildCommon, line 26
	state.deviceCapabilities = enabledCapabilities // library marker replica.samsungReplicaChildCommon, line 27
	runIn(1, refreshAttributes, [data: components]) // library marker replica.samsungReplicaChildCommon, line 28
	logInfo("checkCapabilities: [disabled: ${disabledCapabilities}, enabled: ${enabledCapabilities}]") // library marker replica.samsungReplicaChildCommon, line 29
} // library marker replica.samsungReplicaChildCommon, line 30

def refreshAttributes(components) { // library marker replica.samsungReplicaChildCommon, line 32
	logDebug("refreshAttributes: ${component}") // library marker replica.samsungReplicaChildCommon, line 33
	def component = components."${getDataValue("componentId")}" // library marker replica.samsungReplicaChildCommon, line 34
	component.each { capability -> // library marker replica.samsungReplicaChildCommon, line 35
		capability.value.each { attribute -> // library marker replica.samsungReplicaChildCommon, line 36
			parseEvent([capability: capability.key, // library marker replica.samsungReplicaChildCommon, line 37
						attribute: attribute.key, // library marker replica.samsungReplicaChildCommon, line 38
						value: attribute.value.value, // library marker replica.samsungReplicaChildCommon, line 39
						unit: attribute.value.unit]) // library marker replica.samsungReplicaChildCommon, line 40
			pauseExecution(100) // library marker replica.samsungReplicaChildCommon, line 41
		} // library marker replica.samsungReplicaChildCommon, line 42
	} // library marker replica.samsungReplicaChildCommon, line 43
	listAttributes(false) // library marker replica.samsungReplicaChildCommon, line 44
} // library marker replica.samsungReplicaChildCommon, line 45

void parentEvent(Map event) { // library marker replica.samsungReplicaChildCommon, line 47
	if (event.deviceEvent.componentId == getDataValue("componentId")) { // library marker replica.samsungReplicaChildCommon, line 48
		try { // library marker replica.samsungReplicaChildCommon, line 49
			parseEvent(event.deviceEvent) // library marker replica.samsungReplicaChildCommon, line 50
		} catch (err) { // library marker replica.samsungReplicaChildCommon, line 51
			logWarn("replicaEvent: [event = ${event}, error: ${err}") // library marker replica.samsungReplicaChildCommon, line 52
		} // library marker replica.samsungReplicaChildCommon, line 53
	} // library marker replica.samsungReplicaChildCommon, line 54
} // library marker replica.samsungReplicaChildCommon, line 55

//	===== Device Commands ===== // library marker replica.samsungReplicaChildCommon, line 57
def refresh() { parent.refresh() } // library marker replica.samsungReplicaChildCommon, line 58

// ~~~~~ end include (1304) replica.samsungReplicaChildCommon ~~~~~

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
