/*	HubiThings Replica Samsung Washer Flex Driver
	HubiThings Replica Applications Copyright 2023 by Bloodtick
	Replica Washer Flex Copyright 2023 by Dave Gutheinz

	See parent driver for added information.
==========================================================================*/
def driverVer() { return "1.0-1" }
def appliance() { return "ReplicaSamsungWasherFlex" }

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
	return ["refresh", "remoteControlStatus", "samsungce.kidsLock",
			"switch", "washerOperatingState", "custom.washerWaterTemperature"]
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




// ~~~~~ start include (1311) replica.samsungWasherCommon ~~~~~
library ( // library marker replica.samsungWasherCommon, line 1
	name: "samsungWasherCommon", // library marker replica.samsungWasherCommon, line 2
	namespace: "replica", // library marker replica.samsungWasherCommon, line 3
	author: "Dave Gutheinz", // library marker replica.samsungWasherCommon, line 4
	description: "Common Methods for replica Samsung Oven parent/children", // library marker replica.samsungWasherCommon, line 5
	category: "utilities", // library marker replica.samsungWasherCommon, line 6
	documentationLink: "" // library marker replica.samsungWasherCommon, line 7
) // library marker replica.samsungWasherCommon, line 8
//	===== Common Capabilities, Commands, and Attributes ===== // library marker replica.samsungWasherCommon, line 9
capability "Refresh" // library marker replica.samsungWasherCommon, line 10
attribute "remoteControlEnabled", "boolean" // library marker replica.samsungWasherCommon, line 11
attribute "switch", "string" // library marker replica.samsungWasherCommon, line 12
attribute "completionTime", "string" // library marker replica.samsungWasherCommon, line 13
attribute "machineState", "string" // library marker replica.samsungWasherCommon, line 14
attribute "washerJobState", "string" // library marker replica.samsungWasherCommon, line 15
attribute "timeRemaining", "string" // library marker replica.samsungWasherCommon, line 16
attribute "washerWaterTemperature", "string" // library marker replica.samsungWasherCommon, line 17
attribute "washerSoilLevel", "string" // library marker replica.samsungWasherCommon, line 18
attribute "washerSpinLevel", "string" // library marker replica.samsungWasherCommon, line 19
attribute "lockState", "string" // library marker replica.samsungWasherCommon, line 20
command "start" // library marker replica.samsungWasherCommon, line 21
command "pause" // library marker replica.samsungWasherCommon, line 22
command "stop" // library marker replica.samsungWasherCommon, line 23

String helpLogo() { // library marker replica.samsungWasherCommon, line 25
	return """<a href="https://github.com/DaveGut/HubithingsReplica/blob/main/Docs/SamsungWasherReadme.md">""" + // library marker replica.samsungWasherCommon, line 26
		"""<div style="position: absolute; top: 20px; right: 150px; height: 80px; font-size: 28px;">Dryer Help</div></a>""" // library marker replica.samsungWasherCommon, line 27
} // library marker replica.samsungWasherCommon, line 28

//	===== Device Commands ===== // library marker replica.samsungWasherCommon, line 30
def start() { setMachineState("run") } // library marker replica.samsungWasherCommon, line 31
def pause() { setMachineState("pause") } // library marker replica.samsungWasherCommon, line 32
def stop() { setMachineState("stop") } // library marker replica.samsungWasherCommon, line 33
def setMachineState(machState) { // library marker replica.samsungWasherCommon, line 34
	def oldState = device.currentValue("machineState") // library marker replica.samsungWasherCommon, line 35
	Map cmdStatus = [oldState: oldState, newState: machState] // library marker replica.samsungWasherCommon, line 36
	if (oldState != machState) { // library marker replica.samsungWasherCommon, line 37
		cmdStatus << sendRawCommand(getDataValue("componentId"), "washerOperatingState",  // library marker replica.samsungWasherCommon, line 38
									"setMachineState", [machState]) // library marker replica.samsungWasherCommon, line 39
	} else { // library marker replica.samsungWasherCommon, line 40
		cmdStatus << [FAILED: "no change in state"] // library marker replica.samsungWasherCommon, line 41
		runIn(10, checkAttribute, [data: ["setMachineState", "machineState", machState]]) // library marker replica.samsungWasherCommon, line 42
	} // library marker replica.samsungWasherCommon, line 43
	logInfo("setMachineState: ${cmdStatus}") // library marker replica.samsungWasherCommon, line 44
} // library marker replica.samsungWasherCommon, line 45

def checkAttribute(setCommand, attrName, attrValue) { // library marker replica.samsungWasherCommon, line 47
	def checkValue = device.currentValue(attrName).toString() // library marker replica.samsungWasherCommon, line 48
	if (checkValue != attrValue.toString()) { // library marker replica.samsungWasherCommon, line 49
		Map warnTxt = [command: setCommand, // library marker replica.samsungWasherCommon, line 50
					   attribute: attrName, // library marker replica.samsungWasherCommon, line 51
					   checkValue: checkValue, // library marker replica.samsungWasherCommon, line 52
					   attrValue: attrValue, // library marker replica.samsungWasherCommon, line 53
					   failed: "Function may be disabled by SmartThings"] // library marker replica.samsungWasherCommon, line 54
		logWarn("checkAttribute: ${warnTxt}") // library marker replica.samsungWasherCommon, line 55
	} // library marker replica.samsungWasherCommon, line 56
} // library marker replica.samsungWasherCommon, line 57

def parseEvent(event) { // library marker replica.samsungWasherCommon, line 59
	logDebug("parseEvent: <b>${event}</b>") // library marker replica.samsungWasherCommon, line 60
	if (state.deviceCapabilities.contains(event.capability)) { // library marker replica.samsungWasherCommon, line 61
		logTrace("parseEvent: <b>${event}</b>") // library marker replica.samsungWasherCommon, line 62
		if (event.value != null) { // library marker replica.samsungWasherCommon, line 63
			switch(event.attribute) { // library marker replica.samsungWasherCommon, line 64
				case "completionTime": // library marker replica.samsungWasherCommon, line 65
					setEvent(event) // library marker replica.samsungWasherCommon, line 66
					def timeRemaining = calcTimeRemaining(event.value) // library marker replica.samsungWasherCommon, line 67
					setEvent([attribute: "timeRemaining", value: timeRemaining, unit: null]) // library marker replica.samsungWasherCommon, line 68
					break // library marker replica.samsungWasherCommon, line 69
				case "supportedMachineStates": // library marker replica.samsungWasherCommon, line 70
				case "supportedWasherWaterTemperature": // library marker replica.samsungWasherCommon, line 71
				case "supportedWasherSoilLevel": // library marker replica.samsungWasherCommon, line 72
				case "supportedWasherSpinLevel": // library marker replica.samsungWasherCommon, line 73
					break				 // library marker replica.samsungWasherCommon, line 74
				default: // library marker replica.samsungWasherCommon, line 75
					setEvent(event) // library marker replica.samsungWasherCommon, line 76
					break // library marker replica.samsungWasherCommon, line 77
			} // library marker replica.samsungWasherCommon, line 78
		} // library marker replica.samsungWasherCommon, line 79
	} // library marker replica.samsungWasherCommon, line 80
} // library marker replica.samsungWasherCommon, line 81

def setEvent(event) { // library marker replica.samsungWasherCommon, line 83
	logTrace("<b>setEvent</b>: ${event}") // library marker replica.samsungWasherCommon, line 84
	sendEvent(name: event.attribute, value: event.value, unit: event.unit) // library marker replica.samsungWasherCommon, line 85
	if (device.currentValue(event.attribute).toString() != event.value.toString()) { // library marker replica.samsungWasherCommon, line 86
		logInfo("setEvent: [event: ${event}]") // library marker replica.samsungWasherCommon, line 87
	} // library marker replica.samsungWasherCommon, line 88
} // library marker replica.samsungWasherCommon, line 89

def calcTimeRemaining(completionTime) { // library marker replica.samsungWasherCommon, line 91
	Integer currTime = now() // library marker replica.samsungWasherCommon, line 92
	Integer compTime // library marker replica.samsungWasherCommon, line 93
	try { // library marker replica.samsungWasherCommon, line 94
		compTime = Date.parse("yyyy-MM-dd'T'HH:mm:ss'Z'", completionTime,TimeZone.getTimeZone('UTC')).getTime() // library marker replica.samsungWasherCommon, line 95
	} catch (e) { // library marker replica.samsungWasherCommon, line 96
		compTime = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", completionTime,TimeZone.getTimeZone('UTC')).getTime() // library marker replica.samsungWasherCommon, line 97
	} // library marker replica.samsungWasherCommon, line 98
	Integer timeRemaining = ((compTime-currTime) /1000).toInteger() // library marker replica.samsungWasherCommon, line 99
	def hhmmss // library marker replica.samsungWasherCommon, line 100
	if (timeRemaining < 0) { // library marker replica.samsungWasherCommon, line 101
		hhmmss = "00:00:00" // library marker replica.samsungWasherCommon, line 102
    } else { // library marker replica.samsungWasherCommon, line 103
		hhmmss = new GregorianCalendar( 0, 0, 0, 0, 0, timeRemaining, 0 ).time.format( 'HH:mm:ss' ) // library marker replica.samsungWasherCommon, line 104
	} // library marker replica.samsungWasherCommon, line 105
	return hhmmss // library marker replica.samsungWasherCommon, line 106
} // library marker replica.samsungWasherCommon, line 107

// ~~~~~ end include (1311) replica.samsungWasherCommon ~~~~~

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
