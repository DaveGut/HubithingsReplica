/*	HubiThings Replica Soundbar Driver
	HubiThings Replica Applications Copyright 2023 by Bloodtick
	Replica Color Bulb Copyright 2023 by Dave Gutheinz

	Licensed under the Apache License, Version 2.0 (the "License"); 
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at:
	      http://www.apache.org/licenses/LICENSE-2.0
	Unless required by applicable law or agreed to in writing, software 
	distributed under the License is distributed on an "AS IS" BASIS, 
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
	implied. See the License for the specific language governing 
	permissions and limitations under the License.

Sample Audio Notifications Streams are at the bottom of this driver.

Issues with this driver: Contact davegut via Private Message on the
Hubitat Community site: https://community.hubitat.com/
==========================================================================*/
import groovy.json.JsonOutput
def driverVer() { return "1.0" }
def appliance() { return "ReplicaSamsungSoundbar" }

metadata {
	definition (name: appliance(),
				namespace: "replica",
				author: "David Gutheinz",
				importUrl: "https://raw.githubusercontent.com/DaveGut/HubitatActive/master/HubiThingsReplica/Drivers/${appliance()}.groovy"
			   ){
		capability "Switch"
		capability "MediaInputSource"
		command "toggleInputSource"
		capability "MediaTransport"
		capability "AudioVolume"
		attribute "audioTrackData", "JSON_OBJECT"
		attribute "trackDescription", "STRING"
		capability "Refresh"
		capability "Configuration"
		attribute "healthStatus", "enum", ["offline", "online"]
		//	===== Audio Notification Function =====
		capability "AudioNotification"
		//	Test Phase Only
		command "testAudioNotify"
	}
	preferences {
		//	===== Audio Notification Preferences =====
		//	if !deviceIp, SmartThings Notify, else Local Notify
		//	Alt TTS Available under all conditions
		input ("altTts", "bool", title: "Use Alternate TTS Method", defalutValue: false)
		if (altTts) {
			input ("ttsApiKey", "string", title: "TTS Site Key", defaultValue: null,
				   description: "From http://www.voicerss.org/registration.aspx")
			input ("ttsLang", "enum", title: "TTS Language", options: ttsLanguages(), defaultValue: "en-us")
		}
		input ("deviceIp", "string", title: "Device IP. For Local Notification.")
		//	===== End Audio Notification Preferences =====
		input ("logEnable", "bool",  title: "Enable debug logging for 30 minutes", defaultValue: false)
		input ("infoLog", "bool", title: "Enable information logging${helpLogo()}",defaultValue: true)
		input ("traceLog", "bool", title: "Enable trace logging as directed by developer", defaultValue: false)
	}
}

String helpLogo() {
	return """<a href="https://github.com/DaveGut/HubitatActive/blob/master/HubiThingsReplica/Docs/SamsungSoundbarReadme.md">""" +
		"""<div style="position: absolute; top: 20px; right: 150px; height: 80px; font-size: 28px;">Soundbar Help</div></a>"""
}

def installed() {
	runIn(1, updated)
}

def updated() {
	unschedule()
	def updStatus = [:]
	initialize()
	if (!getDataValue("driverVersion") || getDataValue("driverVersion") != driverVer()) {
		updateDataValue("driverVersion", driverVer())
		updStatus << [driverVer: driverVer()]
	}
	if (logEnable) { runIn(1800, debugLogOff) }
	if (traceLog) { runIn(600, traceLogOff) }
	updStatus << [logEnable: logEnable, infoLog: infoLog, traceLog: traceLog]
	clearQueue()
	runEvery15Minutes(kickStartQueue)

	runIn(10, refresh)
	pauseExecution(5000)
	listAttributes(true)
	logInfo("updated: ${updStatus}")
}

def initialize() {
    updateDataValue("triggers", groovy.json.JsonOutput.toJson(getReplicaTriggers()))
    updateDataValue("commands", groovy.json.JsonOutput.toJson(getReplicaCommands()))
	logInfo("initialize: initialize device-specific data")
}

//	===== HubiThings Device Settings =====
Map getReplicaCommands() {
    return ([
		"replicaEvent":[[name:"parent*",type:"OBJECT"],[name:"event*",type:"JSON_OBJECT"]],
		"replicaStatus":[[name:"parent*",type:"OBJECT"],[name:"event*",type:"JSON_OBJECT"]],
//		"replicaHealth":[[name:"parent*",type:"OBJECT"],[name:"health*",type:"JSON_OBJECT"]],
		"setHealthStatusValue":[[name:"healthStatus*",type:"ENUM"]]])
}

Map getReplicaTriggers() {
	def replicaTriggers = [
		off:[],
		on:[],
		setInputSource: [[name:"inputName*", type: "STRING"]],
		play:[],
		pause:[],
		stop:[],
		volumeUp:[],
		volumeDown:[],
		setVolume: [[name:"volumelevel*", type: "NUMBER"]],
		mute:[],
		unmute:[],
		playTrack:[
			[name:"trackuri*", type: "STRING"],
			[name:"volumelevel", type:"NUMBER", data:"volumelevel"]],
		playTrackAndRestore:[
			[name:"trackuri*", type: "STRING"],
			[name:"volumelevel", type:"NUMBER", data:"volumelevel"]],
		playTrackAndResume:[
			[name:"trackuri*", type: "STRING"],
			[name:"volumelevel", type:"NUMBER", data:"volumelevel"]],
		refresh:[],
		deviceRefresh:[]
	]
	return replicaTriggers
}

def configure() {
    initialize()
	setReplicaRules()
	sendCommand("configure")
	logInfo("configure: configuring default rules")
}

String setReplicaRules() {
	def rules = """{"version":1,"components":[{"trigger":{"type":"attribute","properties":{"value":{"title":"HealthState","type":"string"}},"additionalProperties":false,"required":["value"],"capability":"healthCheck","attribute":"healthStatus","label":"attribute: healthStatus.*"},"command":{"name":"setHealthStatusValue","label":"command: setHealthStatusValue(healthStatus*)","type":"command","parameters":[{"name":"healthStatus*","type":"ENUM"}]},"type":"smartTrigger"},
{"trigger":{"name":"deviceRefresh","label":"command: deviceRefresh()","type":"command"},"command":{"name":"refresh","type":"command","capability":"refresh","label":"command: refresh()"},"type":"hubitatTrigger"},
{"trigger":{"name":"mute","label":"command: mute()","type":"command"},"command":{"name":"mute","type":"command","capability":"audioMute","label":"command: mute()"},"type":"hubitatTrigger"},
{"trigger":{"name":"off","label":"command: off()","type":"command"},"command":{"name":"off","type":"command","capability":"switch","label":"command: off()"},"type":"hubitatTrigger"},
{"trigger":{"name":"on","label":"command: on()","type":"command"},"command":{"name":"on","type":"command","capability":"switch","label":"command: on()"},"type":"hubitatTrigger"},
{"trigger":{"name":"pause","label":"command: pause()","type":"command"},"command":{"name":"pause","type":"command","capability":"mediaPlayback","label":"command: pause()"},"type":"hubitatTrigger"},
{"trigger":{"name":"play","label":"command: play()","type":"command"},"command":{"name":"play","type":"command","capability":"mediaPlayback","label":"command: play()"},"type":"hubitatTrigger"},
{"trigger":{"name":"playTrack","label":"command: playTrack(trackuri*, volumelevel)","type":"command","parameters":[{"name":"trackuri*","type":"STRING"},{"name":"volumelevel","type":"NUMBER","data":"volumelevel"}]},"command":{"name":"playTrack","arguments":[{"name":"uri","optional":false,"schema":{"title":"URI","type":"string","format":"uri"}},{"name":"level","optional":true,"schema":{"type":"integer","minimum":0,"maximum":100}}],"type":"command","capability":"audioNotification","label":"command: playTrack(uri*, level)"},"type":"hubitatTrigger"},
{"trigger":{"name":"playTrackAndRestore","label":"command: playTrackAndRestore(trackuri*, volumelevel)","type":"command","parameters":[{"name":"trackuri*","type":"STRING"},{"name":"volumelevel","type":"NUMBER","data":"volumelevel"}]},"command":{"name":"playTrackAndRestore","arguments":[{"name":"uri","optional":false,"schema":{"title":"URI","type":"string","format":"uri"}},{"name":"level","optional":true,"schema":{"type":"integer","minimum":0,"maximum":100}}],"type":"command","capability":"audioNotification","label":"command: playTrackAndRestore(uri*, level)"},"type":"hubitatTrigger"},
{"trigger":{"name":"playTrackAndResume","label":"command: playTrackAndResume(trackuri*, volumelevel)","type":"command","parameters":[{"name":"trackuri*","type":"STRING"},{"name":"volumelevel","type":"NUMBER","data":"volumelevel"}]},"command":{"name":"playTrackAndResume","arguments":[{"name":"uri","optional":false,"schema":{"title":"URI","type":"string","format":"uri"}},{"name":"level","optional":true,"schema":{"type":"integer","minimum":0,"maximum":100}}],"type":"command","capability":"audioNotification","label":"command: playTrackAndResume(uri*, level)"},"type":"hubitatTrigger"},
{"trigger":{"name":"refresh","label":"command: refresh()","type":"command"},"command":{"name":"refresh","type":"command","capability":"refresh","label":"command: refresh()"},"type":"hubitatTrigger"},
{"trigger":{"name":"setVolume","label":"command: setVolume(volumelevel*)","type":"command","parameters":[{"name":"volumelevel*","type":"NUMBER"}]},"command":{"name":"setVolume","arguments":[{"name":"volume","optional":false,"schema":{"type":"integer","minimum":0,"maximum":100}}],"type":"command","capability":"audioVolume","label":"command: setVolume(volume*)"},"type":"hubitatTrigger"},
{"trigger":{"name":"setInputSource","label":"command: setInputSource(inputSource*)","type":"command","parameters":[{"name":"inputName*","type":"string"}]},"command":{"name":"setInputSource","arguments":[{"name":"mode","optional":false,"schema":{"title":"MediaSource","enum":["AM","CD","FM","HDMI","HDMI1","HDMI2","HDMI3","HDMI4","HDMI5","HDMI6","digitalTv","USB","YouTube","aux","bluetooth","digital","melon","wifi"],"type":"string"}}],"type":"command","capability":"mediaInputSource","label":"command: setInputSource(mode*)"},"type":"hubitatTrigger"},
{"trigger":{"name":"stop","label":"command: stop()","type":"command"},"command":{"name":"stop","type":"command","capability":"mediaPlayback","label":"command: stop()"},"type":"hubitatTrigger"},
{"trigger":{"name":"unmute","label":"command: unmute()","type":"command"},"command":{"name":"unmute","type":"command","capability":"audioMute","label":"command: unmute()"},"type":"hubitatTrigger"},
{"trigger":{"name":"volumeDown","label":"command: volumeDown()","type":"command"},"command":{"name":"volumeDown","type":"command","capability":"audioVolume","label":"command: volumeDown()"},"type":"hubitatTrigger"},
{"trigger":{"name":"volumeUp","label":"command: volumeUp()","type":"command"},"command":{"name":"volumeUp","type":"command","capability":"audioVolume","label":"command: volumeUp()"},"type":"hubitatTrigger"}]}"""

	updateDataValue("rules", rules)
}

//	===== Event Parse Interface s=====
void replicaStatus(def parent=null, Map status=null) {
	def logData = [parent: parent, status: status]
	if (state.refreshAttributes) {
		refreshAttributes(status.components.main)
	}
	logTrace("replicaStatus: ${logData}")
}

def refreshAttributes(mainData) {
	logDebug("refreshAttributes: ${mainData}")
	def value
	try {
		value = mainData.mediaInputSource.supportedInputSources.value
	} catch(e) {
		value = ["n/a"]
		pauseExecution(200)
	}
	parse_main([attribute: "supportedInputSources", value: value])
	pauseExecution(200)
	
	parse_main([attribute: "switch", value: mainData.switch.switch.value])
	pauseExecution(200)

	parse_main([attribute: "volume", value: mainData.audioVolume.volume.value.toInteger(), unit: "%"])
	pauseExecution(200)

	parse_main([attribute: "mute", value: mainData.audioMute.mute.value])
	pauseExecution(200)

	parse_main([attribute: "playbackStatus", value: mainData.mediaPlayback.playbackStatus.value])
	pauseExecution(200)

	try {
		value = mainData.mediaInputSource.inputSource.value
	} catch(e) {
		value = "n/a"
	}
	parse_main([attribute: "inputSource", value: value])
	pauseExecution(200)

	try {
		value = mainData.audioTrackData.audioTrackData.value
	} catch(e) {
		value = "n/a"
	}
	parse_main([attribute: "audioTrackData", value: value])
	
	state.refreshAttributes	= false
}

void replicaHealth(def parent=null, Map health=null) {
	if(parent) { logInfo("replicaHealth: ${parent?.getLabel()}") }
	if(health) { logInfo("replicaHealth: ${health}") }
}

void replicaEvent(def parent=null, Map event=null) {
	logDebug("replicaEvent: [parent: ${parent}, event: ${event}]")
	def eventData = event.deviceEvent
	try {
		"parse_${event.deviceEvent.componentId}"(event.deviceEvent)
	} catch (err) {
		logWarn("replicaEvent: [event = ${event}, error: ${err}")
	}
}

def parse_main(event) {
	logInfo("parse_main: <b>[attribute: ${event.attribute}, value: ${event.value}, unit: ${event.unit}]</b>")
	switch(event.attribute) {
		case "switch":
		case "mute":
			sendEvent(name: event.attribute, value: event.value)
			break
		case "audioTrackData":
			sendEvent(name: event.attribute, value: event.value)
			def title = " "
			if (event.value != "n/a" && event.value.title != null) {
				title = event.value.title
			}
			sendEvent(name: "trackDescription", value: title)
			break
		case "volume":
			sendEvent(name: event.attribute, value: event.value)
			sendEvent(name: "level", value: event.value)
			break
		case "inputSource":
			if (event.capability == "mediaInputSource") {
				sendEvent(name: "mediaInputSource", value: event.value)
			}
			break
		case "playbackStatus":
			sendEvent(name: "transportStatus", value: event.value)
			break
		case "supportedInputSources":
			state.inputSources = event.value
			break
		default:
			logDebug("parse_main: [unhandledEvent: ${event}]")
		break
	}
	logTrace("parse_main: [event: ${event}]")
}

//	===== HubiThings Send Command and Device Health =====
def setHealthStatusValue(value) {    
    sendEvent(name: "healthStatus", value: value)
}

private def sendCommand(String name, def value=null, String unit=null, data=[:]) {
	parent?.deviceTriggerHandler(device, [name:name, value:value, unit:unit, data:data, now:now])
}

def refresh() {
	state.refreshAttributes = true
	sendCommand("deviceRefresh")
	pauseExecution(500)
	sendCommand("refresh")
}

def deviceRefresh() {
	sendCommand("deviceRefresh")
}

//	===== Samsung Soundbar Commands =====
def on() {
	sendCommand("on")
}

def off() {
	sendCommand("off")
}

def setAttrSwitch(onOff) {
	sendEvent(name: "switch", value: onOff)
}

//	===== Media Input Source =====
def toggleInputSource() {
	if (state.inputSources) {
		def inputSources = state.inputSources
		def totalSources = inputSources.size()
		def currentSource = device.currentValue("mediaInputSource")
		def sourceNo = inputSources.indexOf(currentSource)
		def newSourceNo = sourceNo + 1
		if (newSourceNo == totalSources) { newSourceNo = 0 }
		def inputSource = inputSources[newSourceNo]
		setInputSource(inputSource)
	} else { 
		logWarn("toggleInputSource: [status: FAILED, reason: no state.inputSources, <b>correction: try running refresh</b>]")
	}
}

def setInputSource(inputSource) {
	if (inputSource == "n/a") {
		logWarn("setInputSource: [status: FAILED, reason: ST Device does not support input source]")
	} else {
		def inputSources = state.inputSources
		if (inputSources == null) {
			logWarn("setInputSource: [status: FAILED, reason: no state.inputSources, <b>correction: try running refresh</b>]")
		} else if (state.inputSources.contains(inputSource)) {
			sendCommand("setInputSource", inputSource)
		} else {
			logWarn("setInputSource: [status: FAILED, inputSource: ${inputSource}, inputSources: ${inputSources}]")
		}
	}
}

//	===== Media Transport =====
def play() {
	sendCommand("play")
	runIn(5, deviceRefresh)
}

def pause() { 
	sendCommand("pause") 
	runIn(5, deviceRefresh)
}

def stop() {
	sendCommand("stop")
	runIn(5, deviceRefresh)
}

//	===== Audio Volume =====
def volumeUp() { sendCommand("volumeUp") }

def volumeDown() { sendCommand("volumeDown") }

def setVolume(volume) {
	if (volume == null) { volume = device.currentValue("volume").toInteger() }
	if (volume < 0) { volume = 0 }
	else if (volume > 100) { volume = 100 }
	sendCommand("setVolume", volume)
}

def mute() { sendCommand("mute") }

def unmute() { sendCommand("unmute") }

//	===== Libraries =====
#include davegut.samsungAudioNotify
#include davegut.Logging
