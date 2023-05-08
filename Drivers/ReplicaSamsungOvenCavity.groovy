/*	HubiThings Replica Samsung Oven cavity Driver
	HubiThings Replica Applications Copyright 2023 by Bloodtick
	Replica RangeOvenCavity Copyright 2023 by Dave Gutheinz


	See parent driver for added information.
==========================================================================*/
def driverVer() { return "1.0-1" }
def appliance() { return "ReplicaSamsungOvenCavity" }

metadata {
	definition (name: appliance(),
				namespace: "replica",
				author: "David Gutheinz",
				importUrl: "https://raw.githubusercontent.com/DaveGut/HubithingsReplica/main/Drivers/${appliance()}.groovy"
			   ){
		capability "Refresh"
		attribute "ovenCavityStatus", "string"
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

//	===== Event Parse Interface s=====
def designCapabilities() {
	return ["ovenSetpoint", "ovenMode", "ovenOperatingState", "temperatureMeasurement",
			"samsungce.ovenMode", "samsungce.ovenOperatingState", "custom.ovenCavityStatus"]
}

def sendRawCommand(component, capability, command, arguments = []) {
	Map status = [:]
	def cavityInst = device.currentValue("ovenCavityStatus")
	if (cavityInst == "on") {
		status << parent.sendRawCommand(component, capability, command, arguments)
	} else {
		status << [FAILED: [cavityInst: cavityInst]]
	}
	return status
}

//	===== Device Commands =====
//	Common parent/child Oven commands are in library replica.samsungReplicaOvenCommon

//	===== Libraries =====





// ~~~~~ start include (1315) replica.samsungOvenCommon ~~~~~
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

// ~~~~~ end include (1315) replica.samsungOvenCommon ~~~~~

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
