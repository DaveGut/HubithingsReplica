/*	HubiThings Replica Samsung Oven Driver
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
1.0-1
a.	Changed link to help file and moved to library samsungDryerCommon
b.	Added logging prefs to library logging.
c.	Changed sendRawCommand to use dataValue replica vice description.

Issues with this driver: Contact davegut via Private Message on the
Hubitat Community site: https://community.hubitat.com/
==========================================================================*/
def driverVer() { return "1.0-1" }
def appliance() { return "ReplicaSamsungOven" }

metadata {
	definition (name: appliance(),
				namespace: "replica",
				author: "David Gutheinz",
				importUrl: "https://raw.githubusercontent.com/DaveGut/HubithingsReplica/main/Drivers/${appliance()}.groovy"
			   ){
		capability "Configuration"
		capability "Refresh"
		attribute "healthStatus", "enum", ["offline", "online"]
		attribute "lockState", "string"
		command "setOvenLight", [[name: "from state.supported BrightnessLevels", type: "STRING"]]
		attribute "brightnessLevel", "string"
		attribute "remoteControlEnabled", "boolean"
		attribute "doorState", "string"
		attribute "cooktopOperatingState", "string"
		command "setProbeSetpoint", [[name: "probe alert temperature", type: "NUMBER"]]
		attribute "probeSetpoint", "number"
		attribute "probeStatus", "string"
		attribute "probeTemperature", "number"
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
	return ["refresh", "remoteControlStatus", "ovenSetpoint", "ovenMode",
			"ovenOperatingState", "temperatureMeasurement", "samsungce.doorState",
			"samsungce.ovenMode", "samsungce.ovenOperatingState", "samsungce.meatProbe",
			"samsungce.lamp", "samsungce.kidsLock", "custom.cooktopOperatingState"]
}

Map designChildren() {
	return ["cavity-01": "Cavity"]
}

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

//	===== Device Commands =====
//	Common parent/child Oven commands are in library replica.samsungReplicaOvenCommon
def setProbeSetpoint(temperature) {
	temperature = temperature.toInteger()
	def isCapability =  state.deviceCapabilities.contains("samsungce.meatProbe")
	Map cmdStatus = [temperature: temperature, isCapability: isCapability]
	def probeStatus = device.currentValue("probeStatus")
	if (isCapability && probeStatus == "connected") {
		if (temperature > 0) {
			cmdStatus << sendRawCommand(getDataValue("componentId"), "samsungce.meatProbe", "setTemperatureSetpoint", [temperature])
		} else {
			cmdStatus << [FAILED: "invalidTemperature"]
		}
	} else {
		cmdStatus << [FAILED: [probeStatus: probeStatus]]
	}
	logInfo("setProbeSetpoint: ${cmdStatus}")
}

def setOvenLight(lightLevel) {
	lightLevel = state.supportedBrightnessLevel.find { it.toLowerCase() == lightLevel.toLowerCase() }
	def isCapability =  state.deviceCapabilities.contains("samsungce.lamp")
	Map cmdStatus = [lightLevel: lightLevel, isCapability: isCapability]
	if (lightLevel != null && isCapability) {
		cmdStatus << sendRawCommand(getDataValue("componentId"), "samsungce.lamp", "setBrightnessLevel", [lightLevel])
	} else {
		cmdStatus << [FAILED: "invalidLightLevel"]
	}
	logInfo("setOvenLight: ${cmdStatus}")
}

//	===== Libraries =====




// ~~~~~ start include (1307) replica.samsungOvenCommon ~~~~~
library ( // library marker replica.samsungOvenCommon, line 1
	name: "samsungOvenCommon", // library marker replica.samsungOvenCommon, line 2
	namespace: "replica", // library marker replica.samsungOvenCommon, line 3
	author: "Dave Gutheinz", // library marker replica.samsungOvenCommon, line 4
	description: "Common Methods for replica Samsung Oven parent/children", // library marker replica.samsungOvenCommon, line 5
	category: "utilities", // library marker replica.samsungOvenCommon, line 6
	documentationLink: "" // library marker replica.samsungOvenCommon, line 7
) // library marker replica.samsungOvenCommon, line 8
//	Version 1.0 // library marker replica.samsungOvenCommon, line 9

//	===== Common Capabilities, Commands, and Attributes ===== // library marker replica.samsungOvenCommon, line 11
command "setOvenSetpoint", [[name: "oven temperature", type: "NUMBER"]] // library marker replica.samsungOvenCommon, line 12
attribute "ovenSetpoint", "number" // library marker replica.samsungOvenCommon, line 13
attribute "ovenTemperature", "number"	//	attr.temperature // library marker replica.samsungOvenCommon, line 14
command "setOvenMode", [[name: "from state.supported OvenModes", type:"STRING"]] // library marker replica.samsungOvenCommon, line 15
attribute "ovenMode", "string" // library marker replica.samsungOvenCommon, line 16
command "stop" // library marker replica.samsungOvenCommon, line 17
command "pause" // library marker replica.samsungOvenCommon, line 18
command "start", [[name: "mode", type: "STRING"], // library marker replica.samsungOvenCommon, line 19
				  [name: "time (hh:mm:ss OR secs)", type: "STRING"], // library marker replica.samsungOvenCommon, line 20
				  [name: "setpoint", type: "NUMBER"]] // library marker replica.samsungOvenCommon, line 21
attribute "completionTime", "string"	//	time string // library marker replica.samsungOvenCommon, line 22
attribute "progress", "number"			//	percent // library marker replica.samsungOvenCommon, line 23
attribute "operatingState", "string"	//	attr.machineState // library marker replica.samsungOvenCommon, line 24
attribute "ovenJobState", "string" // library marker replica.samsungOvenCommon, line 25
attribute "operationTime", "string" // library marker replica.samsungOvenCommon, line 26
command "setOperationTime", [[name: "time (hh:mm:ss OR secs)", type: "STRING"]] // library marker replica.samsungOvenCommon, line 27

String helpLogo() { // library marker replica.samsungOvenCommon, line 29
	return """<a href="https://github.com/DaveGut/HubithingsReplica/blob/main/Docs/SamsungOvenReadme.md">""" + // library marker replica.samsungOvenCommon, line 30
		"""<div style="position: absolute; top: 20px; right: 150px; height: 80px; font-size: 28px;">Oven Help</div></a>""" // library marker replica.samsungOvenCommon, line 31
} // library marker replica.samsungOvenCommon, line 32

def parseEvent(event) { // library marker replica.samsungOvenCommon, line 34
	logDebug("parseEvent: <b>${event}</b>") // library marker replica.samsungOvenCommon, line 35
	if (state.deviceCapabilities.contains(event.capability)) { // library marker replica.samsungOvenCommon, line 36
		logTrace("parseEvent: <b>${event}</b>") // library marker replica.samsungOvenCommon, line 37
		if (event.value != null) { // library marker replica.samsungOvenCommon, line 38
			switch(event.attribute) { // library marker replica.samsungOvenCommon, line 39
				case "machineState": // library marker replica.samsungOvenCommon, line 40
					if (!state.deviceCapabilities.contains("samsungce.ovenOperatingState")) { // library marker replica.samsungOvenCommon, line 41
						event.attribute = "operatingState" // library marker replica.samsungOvenCommon, line 42
						setEvent(event) // library marker replica.samsungOvenCommon, line 43
					} // library marker replica.samsungOvenCommon, line 44
					break // library marker replica.samsungOvenCommon, line 45
				case "operationTime": // library marker replica.samsungOvenCommon, line 46
					def opTime = formatTime(event.value, "hhmmss", "parseEvent") // library marker replica.samsungOvenCommon, line 47
					event.value = opTime // library marker replica.samsungOvenCommon, line 48
				case "completionTime": // library marker replica.samsungOvenCommon, line 49
				case "progress": // library marker replica.samsungOvenCommon, line 50
				case "ovenJobState": // library marker replica.samsungOvenCommon, line 51
				case "operationTime": // library marker replica.samsungOvenCommon, line 52
					if (state.deviceCapabilities.contains("samsungce.ovenOperatingState")) { // library marker replica.samsungOvenCommon, line 53
						if (event.capability == "samsungce.ovenOperatingState") { // library marker replica.samsungOvenCommon, line 54
							setEvent(event) // library marker replica.samsungOvenCommon, line 55
						} // library marker replica.samsungOvenCommon, line 56
					} else { // library marker replica.samsungOvenCommon, line 57
						setEvent(event) // library marker replica.samsungOvenCommon, line 58
					} // library marker replica.samsungOvenCommon, line 59
					break // library marker replica.samsungOvenCommon, line 60
				case "temperature": // library marker replica.samsungOvenCommon, line 61
					def attr = "ovenTemperature" // library marker replica.samsungOvenCommon, line 62
					if (event.capability == "samsungce.meatProbe") { // library marker replica.samsungOvenCommon, line 63
						attr = "probeTemperature" // library marker replica.samsungOvenCommon, line 64
					} // library marker replica.samsungOvenCommon, line 65
					event["attribute"] = attr // library marker replica.samsungOvenCommon, line 66
					setEvent(event) // library marker replica.samsungOvenCommon, line 67
					break // library marker replica.samsungOvenCommon, line 68
				case "temperatureSetpoint": // library marker replica.samsungOvenCommon, line 69
					event["attribute"] = "probeSetpoint" // library marker replica.samsungOvenCommon, line 70
					setEvent(event) // library marker replica.samsungOvenCommon, line 71
					break // library marker replica.samsungOvenCommon, line 72
				case "status": // library marker replica.samsungOvenCommon, line 73
					event["attribute"] = "probeStatus" // library marker replica.samsungOvenCommon, line 74
					setEvent(event) // library marker replica.samsungOvenCommon, line 75
					break // library marker replica.samsungOvenCommon, line 76
				case "ovenMode": // library marker replica.samsungOvenCommon, line 77
					if (state.deviceCapabilities.contains("samsungce.ovenMode")) { // library marker replica.samsungOvenCommon, line 78
						if (event.capability == "samsungce.ovenMode") { // library marker replica.samsungOvenCommon, line 79
							setEvent(event) // library marker replica.samsungOvenCommon, line 80
						} // library marker replica.samsungOvenCommon, line 81
					} else { // library marker replica.samsungOvenCommon, line 82
						setEvent(event) // library marker replica.samsungOvenCommon, line 83
					} // library marker replica.samsungOvenCommon, line 84
					break // library marker replica.samsungOvenCommon, line 85
				case "supportedOvenModes": // library marker replica.samsungOvenCommon, line 86
				//	if samsungce.ovenMode, use that, otherwise use // library marker replica.samsungOvenCommon, line 87
				//	ovenMode.  Format always hh:mm:ss. // library marker replica.samsungOvenCommon, line 88
					if (state.deviceCapabilities.contains("samsungce.ovenMode")) { // library marker replica.samsungOvenCommon, line 89
						if (event.capability == "samsungce.ovenMode") { // library marker replica.samsungOvenCommon, line 90
							setState(event) // library marker replica.samsungOvenCommon, line 91
						} // library marker replica.samsungOvenCommon, line 92
					} else { // library marker replica.samsungOvenCommon, line 93
						setState(event) // library marker replica.samsungOvenCommon, line 94
					} // library marker replica.samsungOvenCommon, line 95
					break // library marker replica.samsungOvenCommon, line 96
				case "supportedBrightnessLevel": // library marker replica.samsungOvenCommon, line 97
					setState(event) // library marker replica.samsungOvenCommon, line 98
					break // library marker replica.samsungOvenCommon, line 99
				case "supportedCooktopOperatingState": // library marker replica.samsungOvenCommon, line 100
					break // library marker replica.samsungOvenCommon, line 101
				default: // library marker replica.samsungOvenCommon, line 102
					setEvent(event) // library marker replica.samsungOvenCommon, line 103
					break // library marker replica.samsungOvenCommon, line 104
			} // library marker replica.samsungOvenCommon, line 105
		} // library marker replica.samsungOvenCommon, line 106
	} // library marker replica.samsungOvenCommon, line 107
} // library marker replica.samsungOvenCommon, line 108

def setState(event) { // library marker replica.samsungOvenCommon, line 110
	def attribute = event.attribute // library marker replica.samsungOvenCommon, line 111
	if (state."${attribute}" != event.value) { // library marker replica.samsungOvenCommon, line 112
		state."${event.attribute}" = event.value // library marker replica.samsungOvenCommon, line 113
		logInfo("setState: [event: ${event}]") // library marker replica.samsungOvenCommon, line 114
	} // library marker replica.samsungOvenCommon, line 115
} // library marker replica.samsungOvenCommon, line 116

def setEvent(event) { // library marker replica.samsungOvenCommon, line 118
	logTrace("<b>setEvent</b>: ${event}") // library marker replica.samsungOvenCommon, line 119
	sendEvent(name: event.attribute, value: event.value, unit: event.unit) // library marker replica.samsungOvenCommon, line 120
	if (device.currentValue(event.attribute).toString() != event.value.toString()) { // library marker replica.samsungOvenCommon, line 121
		logInfo("setEvent: [event: ${event}]") // library marker replica.samsungOvenCommon, line 122
	} // library marker replica.samsungOvenCommon, line 123
} // library marker replica.samsungOvenCommon, line 124

//	===== Device Commands ===== // library marker replica.samsungOvenCommon, line 126
def setOvenMode(mode) { // library marker replica.samsungOvenCommon, line 127
	def ovenMode = checkMode(mode) // library marker replica.samsungOvenCommon, line 128
	def hasAdvCap =  state.deviceCapabilities.contains("samsungce.ovenOperatingState") // library marker replica.samsungOvenCommon, line 129
	Map cmdStatus = [mode: mode, ovenMode: ovenMode, hasAdvCap: hasAdvCap] // library marker replica.samsungOvenCommon, line 130
	if (ovenMode == "notSupported") { // library marker replica.samsungOvenCommon, line 131
		cmdStatus << [FAILED: ovenMode] // library marker replica.samsungOvenCommon, line 132
	} else if (hasAdvCap) { // library marker replica.samsungOvenCommon, line 133
		cmdStatus << sendRawCommand(getDataValue("componentId"),  // library marker replica.samsungOvenCommon, line 134
									"samsungce.ovenMode", "setOvenMode", [ovenMode]) // library marker replica.samsungOvenCommon, line 135
	} else { // library marker replica.samsungOvenCommon, line 136
		cmdStatus << sendRawCommand(getDataValue("componentId"),  // library marker replica.samsungOvenCommon, line 137
									"ovenMode", "setOvenMode", [ovenMode]) // library marker replica.samsungOvenCommon, line 138
	} // library marker replica.samsungOvenCommon, line 139
	logInfo("setOvenMode: ${cmdStatus}") // library marker replica.samsungOvenCommon, line 140
} // library marker replica.samsungOvenCommon, line 141

def checkMode(mode) { // library marker replica.samsungOvenCommon, line 143
	mode = state.supportedOvenModes.find { it.toLowerCase() == mode.toLowerCase() } // library marker replica.samsungOvenCommon, line 144
	if (mode == null) { // library marker replica.samsungOvenCommon, line 145
		mode = "notSupported" // library marker replica.samsungOvenCommon, line 146
	} // library marker replica.samsungOvenCommon, line 147
	return mode // library marker replica.samsungOvenCommon, line 148
} // library marker replica.samsungOvenCommon, line 149

def setOvenSetpoint(setpoint) { // library marker replica.samsungOvenCommon, line 151
	setpoint = setpoint.toInteger() // library marker replica.samsungOvenCommon, line 152
	Map cmdStatus = [setpoint: setpoint] // library marker replica.samsungOvenCommon, line 153
	if (setpoint >= 0) { // library marker replica.samsungOvenCommon, line 154
		cmdStatus << sendRawCommand(getDataValue("componentId"), "ovenSetpoint", "setOvenSetpoint", [setpoint]) // library marker replica.samsungOvenCommon, line 155
		logInfo("setOvenSetpoint: ${setpoint}") // library marker replica.samsungOvenCommon, line 156
	} else { // library marker replica.samsungOvenCommon, line 157
		cmdStatus << [FAILED: "invalidSetpoint"] // library marker replica.samsungOvenCommon, line 158
	} // library marker replica.samsungOvenCommon, line 159
	logInfo("setOvenSetpoint: ${cmdStatus}") // library marker replica.samsungOvenCommon, line 160
} // library marker replica.samsungOvenCommon, line 161

def setOperationTime(opTime) { // library marker replica.samsungOvenCommon, line 163
	def hasAdvCap =  state.deviceCapabilities.contains("samsungce.ovenOperatingState") // library marker replica.samsungOvenCommon, line 164
	Map cmdStatus = [opTime: opTime, hasAdvCap: hasAdvCap] // library marker replica.samsungOvenCommon, line 165
	def success = true // library marker replica.samsungOvenCommon, line 166
	def hhmmss = formatTime(opTime, "hhmmss", "setOperationTime") // library marker replica.samsungOvenCommon, line 167
	if (hasAdvCap) { // library marker replica.samsungOvenCommon, line 168
		cmdStatus << [formatedOpTime: opTime] // library marker replica.samsungOvenCommon, line 169
		if (opTime == "invalidEntry") { // library marker replica.samsungOvenCommon, line 170
			cmdStatus << [FAILED: opTime] // library marker replica.samsungOvenCommon, line 171
			success = false // library marker replica.samsungOvenCommon, line 172
		} else { // library marker replica.samsungOvenCommon, line 173
			cmdStatus << sendRawCommand(getDataValue("componentId"),  // library marker replica.samsungOvenCommon, line 174
										"samsungce.ovenOperatingState",  // library marker replica.samsungOvenCommon, line 175
										"setOperationTime", [hhmmss]) // library marker replica.samsungOvenCommon, line 176
		} // library marker replica.samsungOvenCommon, line 177
	} else { // library marker replica.samsungOvenCommon, line 178
		opTime = formatTime(opTime, "seconds", "setOperationTime") // library marker replica.samsungOvenCommon, line 179
		cmdStatus << [formatedOpTime: opTime] // library marker replica.samsungOvenCommon, line 180
		if (opTime == "invalidEntry") { // library marker replica.samsungOvenCommon, line 181
			cmdStatus << [FAILED: opTime] // library marker replica.samsungOvenCommon, line 182
			success = false // library marker replica.samsungOvenCommon, line 183
		} else { // library marker replica.samsungOvenCommon, line 184
			Map opCmd = [time: opTime] // library marker replica.samsungOvenCommon, line 185
			cmdStatus << sendRawCommand(getDataValue("componentId"),  // library marker replica.samsungOvenCommon, line 186
										"ovenOperatingState",  // library marker replica.samsungOvenCommon, line 187
										"start", [opCmd]) // library marker replica.samsungOvenCommon, line 188
		} // library marker replica.samsungOvenCommon, line 189
	} // library marker replica.samsungOvenCommon, line 190
	logInfo("setOperationTime: ${cmdStatus}") // library marker replica.samsungOvenCommon, line 191
	if (success) { // library marker replica.samsungOvenCommon, line 192
		runIn(10, checkAttribute, [data: ["setOperationTime", "operationTime", hhmmss]]) // library marker replica.samsungOvenCommon, line 193
	} // library marker replica.samsungOvenCommon, line 194
} // library marker replica.samsungOvenCommon, line 195

def stop() { // library marker replica.samsungOvenCommon, line 197
	def hasAdvCap =  state.deviceCapabilities.contains("samsungce.ovenOperatingState") // library marker replica.samsungOvenCommon, line 198
	Map cmdStatus = [hasAdvCap: hasAdvCap] // library marker replica.samsungOvenCommon, line 199
	if (hasAdvCap) { // library marker replica.samsungOvenCommon, line 200
		cmdStatus << sendRawCommand(getDataValue("componentId"),  // library marker replica.samsungOvenCommon, line 201
									"samsungce.ovenOperatingState", "stop") // library marker replica.samsungOvenCommon, line 202
	} else { // library marker replica.samsungOvenCommon, line 203
		cmdStatus << sendRawCommand(getDataValue("componentId"),  // library marker replica.samsungOvenCommon, line 204
									"ovenOperatingState", "stop") // library marker replica.samsungOvenCommon, line 205
	} // library marker replica.samsungOvenCommon, line 206
	logInfo("stop: ${cmdStatus}") // library marker replica.samsungOvenCommon, line 207
} // library marker replica.samsungOvenCommon, line 208

def pause() { // library marker replica.samsungOvenCommon, line 210
	def hasAdvCap =  state.deviceCapabilities.contains("samsungce.ovenOperatingState") // library marker replica.samsungOvenCommon, line 211
	Map cmdStatus = [hasAdvCap: hasAdvCap] // library marker replica.samsungOvenCommon, line 212
	if (hasAdvCap) { // library marker replica.samsungOvenCommon, line 213
		cmdStatus << sendRawCommand(getDataValue("componentId"),  // library marker replica.samsungOvenCommon, line 214
									"samsungce.ovenOperatingState", "pause") // library marker replica.samsungOvenCommon, line 215
		runIn(10, checkAttribute, [data: ["pause", "operatingState", "paused"]]) // library marker replica.samsungOvenCommon, line 216
	} else { // library marker replica.samsungOvenCommon, line 217
		cmdStatus << [FAILED: "pause not available on device"] // library marker replica.samsungOvenCommon, line 218
	} // library marker replica.samsungOvenCommon, line 219
	logInfo("pause: ${cmdStatus}") // library marker replica.samsungOvenCommon, line 220
} // library marker replica.samsungOvenCommon, line 221

def start(mode = null, opTime = null, setpoint = null) { // library marker replica.samsungOvenCommon, line 223
	def hasAdvCap =  state.deviceCapabilities.contains("samsungce.ovenOperatingState") // library marker replica.samsungOvenCommon, line 224
	Map cmdStatus = [hasAdvCap: hasAdvCap, input:  // library marker replica.samsungOvenCommon, line 225
					 [mode: mode, opTime: opTime, setpoint: setpoint]] // library marker replica.samsungOvenCommon, line 226
	if (hasAdvCap) { // library marker replica.samsungOvenCommon, line 227
		if (mode != null) { // library marker replica.samsungOvenCommon, line 228
			setOvenMode(mode) // library marker replica.samsungOvenCommon, line 229
			pauseExecution(2000) // library marker replica.samsungOvenCommon, line 230
		} // library marker replica.samsungOvenCommon, line 231
		if (setpoint != null) { // library marker replica.samsungOvenCommon, line 232
			setOvenSetpoint(setpoint) // library marker replica.samsungOvenCommon, line 233
			pauseExecution(2000) // library marker replica.samsungOvenCommon, line 234
		} // library marker replica.samsungOvenCommon, line 235
		if (opTime != null) { // library marker replica.samsungOvenCommon, line 236
			setOperationTime(opTime) // library marker replica.samsungOvenCommon, line 237
			pauseExecution(2000) // library marker replica.samsungOvenCommon, line 238
		} // library marker replica.samsungOvenCommon, line 239
		cmdStatus << sendRawCommand(getDataValue("componentId"), // library marker replica.samsungOvenCommon, line 240
									"samsungce.ovenOperatingState", "start", []) // library marker replica.samsungOvenCommon, line 241
		runIn(10, checkAttribute, [data: ["start", "operatingState", "running"]]) // library marker replica.samsungOvenCommon, line 242
	} else { // library marker replica.samsungOvenCommon, line 243
		Map opCmd = [:] // library marker replica.samsungOvenCommon, line 244
		def failed = false // library marker replica.samsungOvenCommon, line 245
		if (mode != null) { // library marker replica.samsungOvenCommon, line 246
			def ovenMode = checkMode(mode) // library marker replica.samsungOvenCommon, line 247
			cmdStatus << [cmdMode: ovenMode] // library marker replica.samsungOvenCommon, line 248
			opCmd << [mode: ovenMode] // library marker replica.samsungOvenCommon, line 249
			if (ovenMode == "notSupported") { // library marker replica.samsungOvenCommon, line 250
				failed = true // library marker replica.samsungOvenCommon, line 251
			} // library marker replica.samsungOvenCommon, line 252
		} // library marker replica.samsungOvenCommon, line 253
		if (opTime != null) { // library marker replica.samsungOvenCommon, line 254
			opTime = formatTime(opTime, "seconds", "setOperationTime") // library marker replica.samsungOvenCommon, line 255
			cmdStatus << [cmdOpTime: opTime] // library marker replica.samsungOvenCommon, line 256
			opCmd << [time: opTime] // library marker replica.samsungOvenCommon, line 257
			if (opTime == "invalidEntry") { // library marker replica.samsungOvenCommon, line 258
				failed = true // library marker replica.samsungOvenCommon, line 259
			} // library marker replica.samsungOvenCommon, line 260
		} // library marker replica.samsungOvenCommon, line 261
		if (setpoint != null) { // library marker replica.samsungOvenCommon, line 262
			setpoint = setpoint.toInteger() // library marker replica.samsungOvenCommon, line 263
			cmdStatus << [cmdSetpoint: setpoint] // library marker replica.samsungOvenCommon, line 264
			opCmd << [setpoint: setpoint] // library marker replica.samsungOvenCommon, line 265
			if (setpoint < 0) { // library marker replica.samsungOvenCommon, line 266
				failed = true // library marker replica.samsungOvenCommon, line 267
			} // library marker replica.samsungOvenCommon, line 268
		} // library marker replica.samsungOvenCommon, line 269
		if (failed == false) { // library marker replica.samsungOvenCommon, line 270
			cmdStatus << sendRawCommand(getDataValue("componentId"), // library marker replica.samsungOvenCommon, line 271
										"ovenOperatingState", "start", [opCmd]) // library marker replica.samsungOvenCommon, line 272
			runIn(10, checkAttribute, [data: ["start", "operatingState", "running"]]) // library marker replica.samsungOvenCommon, line 273
		} else { // library marker replica.samsungOvenCommon, line 274
			cmdStatus << [FAILED: "invalidInput"] // library marker replica.samsungOvenCommon, line 275
		} // library marker replica.samsungOvenCommon, line 276
	} // library marker replica.samsungOvenCommon, line 277
	logInfo("start: ${cmdStatus}") // library marker replica.samsungOvenCommon, line 278
} // library marker replica.samsungOvenCommon, line 279

def checkAttribute(setCommand, attrName, attrValue) { // library marker replica.samsungOvenCommon, line 281
	def checkValue = device.currentValue(attrName).toString() // library marker replica.samsungOvenCommon, line 282
	if (checkValue != attrValue.toString()) { // library marker replica.samsungOvenCommon, line 283
		Map warnTxt = [command: setCommand, // library marker replica.samsungOvenCommon, line 284
					   attribute: attrName, // library marker replica.samsungOvenCommon, line 285
					   checkValue: checkValue, // library marker replica.samsungOvenCommon, line 286
					   attrValue: attrValue, // library marker replica.samsungOvenCommon, line 287
					   failed: "Function may be disabled by SmartThings"] // library marker replica.samsungOvenCommon, line 288
		logWarn("checkAttribute: ${warnTxt}") // library marker replica.samsungOvenCommon, line 289
	} // library marker replica.samsungOvenCommon, line 290
} // library marker replica.samsungOvenCommon, line 291

def formatTime(timeValue, desiredFormat, callMethod) { // library marker replica.samsungOvenCommon, line 293
	timeValue = timeValue.toString() // library marker replica.samsungOvenCommon, line 294
	def currentFormat = "seconds" // library marker replica.samsungOvenCommon, line 295
	if (timeValue.contains(":")) { // library marker replica.samsungOvenCommon, line 296
		currentFormat = "hhmmss" // library marker replica.samsungOvenCommon, line 297
	} // library marker replica.samsungOvenCommon, line 298
	def formatedTime // library marker replica.samsungOvenCommon, line 299
	if (currentFormat == "hhmmss") { // library marker replica.samsungOvenCommon, line 300
		formatedTime = formatHhmmss(timeValue) // library marker replica.samsungOvenCommon, line 301
		if (desiredFormat == "seconds") { // library marker replica.samsungOvenCommon, line 302
			formatedTime = convertHhMmSsToInt(formatedTime) // library marker replica.samsungOvenCommon, line 303
		} // library marker replica.samsungOvenCommon, line 304
	} else { // library marker replica.samsungOvenCommon, line 305
		formatedTime = timeValue // library marker replica.samsungOvenCommon, line 306
		if (desiredFormat == "hhmmss") { // library marker replica.samsungOvenCommon, line 307
			formatedTime = convertIntToHhMmSs(timeValue) // library marker replica.samsungOvenCommon, line 308
		} // library marker replica.samsungOvenCommon, line 309
	} // library marker replica.samsungOvenCommon, line 310
	if (formatedTime == "invalidEntry") { // library marker replica.samsungOvenCommon, line 311
		Map errorData = [callMethod: callMethod, timeValue: timeValue, // library marker replica.samsungOvenCommon, line 312
						 desiredFormat: desiredFormat] // library marker replica.samsungOvenCommon, line 313
		logWarn("formatTime: [error: ${formatedTime}, data: ${errorData}") // library marker replica.samsungOvenCommon, line 314
	} // library marker replica.samsungOvenCommon, line 315
	return formatedTime // library marker replica.samsungOvenCommon, line 316
} // library marker replica.samsungOvenCommon, line 317

def formatHhmmss(timeValue) { // library marker replica.samsungOvenCommon, line 319
	def timeArray = timeValue.split(":") // library marker replica.samsungOvenCommon, line 320
	def hours = 0 // library marker replica.samsungOvenCommon, line 321
	def minutes = 0 // library marker replica.samsungOvenCommon, line 322
	def seconds = 0 // library marker replica.samsungOvenCommon, line 323
	if (timeArray.size() != timeValue.count(":") + 1) { // library marker replica.samsungOvenCommon, line 324
		return "invalidEntry" // library marker replica.samsungOvenCommon, line 325
	} else { // library marker replica.samsungOvenCommon, line 326
		try { // library marker replica.samsungOvenCommon, line 327
			if (timeArray.size() == 3) { // library marker replica.samsungOvenCommon, line 328
				hours = timeArray[0].toInteger() // library marker replica.samsungOvenCommon, line 329
				minutes = timeArray[1].toInteger() // library marker replica.samsungOvenCommon, line 330
				seconds = timeArray[2].toInteger() // library marker replica.samsungOvenCommon, line 331
			} else if (timeArray.size() == 2) { // library marker replica.samsungOvenCommon, line 332
				minutes = timeArray[0].toInteger() // library marker replica.samsungOvenCommon, line 333
				seconds = timeArray[1].toInteger() // library marker replica.samsungOvenCommon, line 334
			} // library marker replica.samsungOvenCommon, line 335
		} catch (error) { // library marker replica.samsungOvenCommon, line 336
			return "invalidEntry" // library marker replica.samsungOvenCommon, line 337
		} // library marker replica.samsungOvenCommon, line 338
	} // library marker replica.samsungOvenCommon, line 339
	if (hours < 10) { hours = "0${hours}" } // library marker replica.samsungOvenCommon, line 340
	if (minutes < 10) { minutes = "0${minutes}" } // library marker replica.samsungOvenCommon, line 341
	if (seconds < 10) { seconds = "0${seconds}" } // library marker replica.samsungOvenCommon, line 342
	return "${hours}:${minutes}:${seconds}" // library marker replica.samsungOvenCommon, line 343
} // library marker replica.samsungOvenCommon, line 344

def convertIntToHhMmSs(timeSeconds) { // library marker replica.samsungOvenCommon, line 346
	def hhmmss // library marker replica.samsungOvenCommon, line 347
	try { // library marker replica.samsungOvenCommon, line 348
		hhmmss = new GregorianCalendar( 0, 0, 0, 0, 0, timeSeconds.toInteger(), 0 ).time.format( 'HH:mm:ss' ) // library marker replica.samsungOvenCommon, line 349
	} catch (error) { // library marker replica.samsungOvenCommon, line 350
		hhmmss = "invalidEntry" // library marker replica.samsungOvenCommon, line 351
	} // library marker replica.samsungOvenCommon, line 352
	return hhmmss // library marker replica.samsungOvenCommon, line 353
} // library marker replica.samsungOvenCommon, line 354

def convertHhMmSsToInt(timeValue) { // library marker replica.samsungOvenCommon, line 356
	def timeArray = timeValue.split(":") // library marker replica.samsungOvenCommon, line 357
	def seconds = 0 // library marker replica.samsungOvenCommon, line 358
	if (timeArray.size() != timeValue.count(":") + 1) { // library marker replica.samsungOvenCommon, line 359
		return "invalidEntry" // library marker replica.samsungOvenCommon, line 360
	} else { // library marker replica.samsungOvenCommon, line 361
		try { // library marker replica.samsungOvenCommon, line 362
			if (timeArray.size() == 3) { // library marker replica.samsungOvenCommon, line 363
				seconds = timeArray[0].toInteger() * 3600 + // library marker replica.samsungOvenCommon, line 364
				timeArray[1].toInteger() * 60 + timeArray[2].toInteger() // library marker replica.samsungOvenCommon, line 365
			} else if (timeArray.size() == 2) { // library marker replica.samsungOvenCommon, line 366
				seconds = timeArray[0].toInteger() * 60 + timeArray[1].toInteger() // library marker replica.samsungOvenCommon, line 367
			} // library marker replica.samsungOvenCommon, line 368
		} catch (error) { // library marker replica.samsungOvenCommon, line 369
			seconds = "invalidEntry" // library marker replica.samsungOvenCommon, line 370
		} // library marker replica.samsungOvenCommon, line 371
	} // library marker replica.samsungOvenCommon, line 372
	return seconds // library marker replica.samsungOvenCommon, line 373
} // library marker replica.samsungOvenCommon, line 374

// ~~~~~ end include (1307) replica.samsungOvenCommon ~~~~~

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
			def childDni = "dni-${designChild.key}" // library marker replica.samsungReplicaCommon, line 96
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
