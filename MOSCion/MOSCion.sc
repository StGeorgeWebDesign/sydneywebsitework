MOSCion {
	var sendingPort, baseAddr, <pongResp, <listenResp;
	var <responders;
	var <accelX, <accelY, <accelZ;
	var <gyroX, <gyroY, <gyroZ;
	var <magX, <magY, <magZ;
	var <pitch, <yaw, <roll;
	var <speed, <course;
	var <trueHeading, <magneticHeading;
	var <lat, <long, <alt;
	var <amp, <freq, <keynum;
	var <touches, touchEndedFuncs;

	classvar tmpFunc;

	*new {arg sendingPort, baseAddr = "/mOSCion";
		^super.newCopyArgs(sendingPort, baseAddr).initMOSCion;
	}

	initMOSCion {
		responders = IdentityDictionary.new;
		[\accelerometer, \gyroscope, \magnetometer, \attitude, \gps, \speed, \heading, \microphone, \touchBegan, \touchMoved, \touchEnded].do({arg thisKey;
			responders.add(thisKey -> IdentityDictionary.new);
		});
		touches = IdentityDictionary.new;
		touchEndedFuncs = IdentityDictionary.new;
	}

	listen {arg action, remove = true;
		"Listen for '/pong' from mOSCion".postln;
		listenResp = OSCdef(\mOSCionListen, {arg msg, time, addr, recvPort;
			sendingPort = addr.port;
			("Heard mOSCion! Filtering messages to port: " ++ sendingPort).postln;
			action.value;
			remove.if({listenResp.free});
		}, '/pong');
	}

	*showComputerIP {
		var tmpDef;
		var before = NetAddr.broadcastFlag;
		NetAddr.broadcastFlag = true;
		tmpDef = OSCdef(\tmp, { arg msg,time, addr, recvPort;
			NetAddr.broadcastFlag = before;
			("Set the IP in mOSCion to: " ++ addr.ip ++ " and port: "++NetAddr.langPort).postln;
			tmpDef.free;
		}, '/getMyIP');

		NetAddr("255.255.255.255", NetAddr.langPort).sendMsg('/getMyIP');
	}

	*dumpAllOSC {arg bool = true;
		bool.if({
			thisProcess.addOSCRecvFunc(tmpFunc = { arg msg, time, replyAddr, port;
				if(msg[0] != '/status.reply') {
					"At time %s received message % from % on port %\n".postf( time, msg, replyAddr )
				}
			})
		}, {
			thisProcess.removeOSCRecvFunc(tmpFunc)
		});
	}

	clearResponders {
		responders.do({arg responderDict;
			var keysToRemove = [];
			responderDict.keysValuesDo({arg key, resp;
				keysToRemove = keysToRemove.add(key);
				resp.free;
			});
			keysToRemove.do({arg thisKey;
				responderDict[thisKey] = nil;
			});
		});
	}

	removeResponderForType {arg type, key;
		(responders[type][key].notNil).if({
			responders[type][key].free;
			responders[type][key] = nil;
		})
	}

	addResponder {arg respDictKey, key, resp;
		var respDict, curResp;
		respDict = responders[respDictKey];
		curResp = respDict[key];
		curResp.notNil.if({
			curResp.free;
		});
		respDict.add(key -> resp);
	}

	check {arg addr;
		var check;
		^(sendingPort.notNil and:{addr.port != sendingPort}).not;
	}

	addResponderForAccelerometer {arg key, allFunc, xFunc, yFunc, zFunc;
		var resp;
		resp = OSCdef(\accelerometer, {arg msg, time, addr, recvPort;
			this.check(addr).if({
				accelX = msg[2];
				accelY = msg[4];
				accelZ = msg[6];
				xFunc.value(accelX);
				yFunc.value(accelY);
				zFunc.value(accelZ);
				allFunc.value(accelX, accelY, accelZ);
			});
		}, baseAddr ++ "/accelerometer");
		this.addResponder(\accelerometer, key, resp);
	}

	removeAccelerometerResponder {arg key;
		this.removeResponderForType(\accelerometer, key);
	}

	addResponderForGyroscope {arg key, allFunc, xFunc, yFunc, zFunc;
		var resp;
		resp = OSCdef(\gyroscope, {arg msg, time, addr, recvPort;
			this.check(addr).if({
				gyroX = msg[2];
				gyroY = msg[4];
				gyroZ = msg[6];
				xFunc.value(gyroX);
				yFunc.value(gyroY);
				zFunc.value(gyroZ);
				allFunc.value(gyroX, gyroY, gyroZ);
			});
		}, baseAddr ++ "/gyroscope");
		this.addResponder(\gyroscope, key, resp);
	}

	removeGyroscopeResponder {arg key;
		this.removeResponderForType(\gyroscope, key);
	}

	addResponderForMagnetometer {arg key, allFunc, xFunc, yFunc, zFunc;
		var resp;
		resp = OSCdef(\magnetometer, {arg msg, time, addr, recvPort;
			this.check(addr).if({
				magX = msg[2];
				magY = msg[4];
				magZ = msg[6];
				xFunc.value(magX);
				yFunc.value(magY);
				zFunc.value(magZ);
				allFunc.value(magX, magY, magZ);
			});
		}, baseAddr ++ "/magnetometer");
		this.addResponder(\magnetometer, key, resp);
	}

	removeMagnetometerResponder {arg key;
		this.removeResponderForType(\magnetometer, key);
	}

	addResponderForAttitude {arg key, allFunc, pitchFunc, yawFunc, rollFunc;
		var resp;
		resp = OSCdef(\attitude, {arg msg, time, addr, recvPort;
			this.check(addr).if({
				pitch = msg[2];
				yaw = msg[4];
				roll = msg[6];
				pitchFunc.value(pitch);
				yawFunc.value(yaw);
				rollFunc.value(roll);
				allFunc.value(pitch, yaw, roll);
			});
		}, baseAddr ++ "/attitude");
		this.addResponder(\attitude, key, resp);
	}

	removeAttitudeResponder {arg key;
		this.removeResponderForType(\attitude, key);
	}

	addResponderForSpeed {arg key, allFunc, speedFunc, courseFunc;
		var resp;
		resp = OSCdef(\speed, {arg msg, time, addr, recvPort;
			this.check(addr).if({
				speed = msg[2];
				course = msg[4];
				speedFunc.value(speed);
				courseFunc.value(course);
				allFunc.value(speed, course);
			});
		}, baseAddr ++ "/speed");
		this.addResponder(\speed, key, resp);
	}

	removeSpeedResponder {arg key;
		this.removeResponderForType(\attitude, key);
	}

	addResponderForHeading {arg key, allFunc, trueFunc, magneticFunc;
		var resp;
		resp = OSCdef(\heading, {arg msg, time, addr, recvPort;
			this.check(addr).if({
				trueHeading = msg[2];
				magneticHeading = msg[4];
				trueFunc.value(trueHeading);
				magneticFunc.value(magneticHeading);
				allFunc.value(trueHeading, magneticHeading);
			});
		}, baseAddr ++ "/heading");
		this.addResponder(\heading, key, resp);
	}

	removeHeadingResponder {arg key;
		this.removeResponderForType(\heading, key);
	}

	addResponderForGPS {arg key, allFunc, latFunc, longFunc, altFunc;
		var resp;
		resp = OSCdef(\gps, {arg msg, time, addr, recvPort;
			this.check(addr).if({
				lat = msg[2];
				long = msg[4];
				alt = msg[6];
				latFunc.value(lat);
				longFunc.value(long);
				altFunc.value(alt);
				allFunc.value(lat, long, alt);
			});
		}, baseAddr ++ "/gps");
		this.addResponder(\gps, key, resp);
	}

	removeGPSResponder {arg key;
		this.removeResponderForType(\gps, key);
	}

	addResponderForMicrophone {arg key, allFunc, ampFunc, freqFunc, keynumFunc;
		var resp;
		resp = OSCdef(\microphone, {arg msg, time, addr, recvPort;

			this.check(addr).if({
				amp = msg[2];
				freq = msg[4];
				keynum = msg[6];
				ampFunc.value(amp);
				freqFunc.value(freq);
				keynumFunc.value(keynum);
				allFunc.value(amp, freq, keynum);
			});
		}, baseAddr ++ "/microphone");
		this.addResponder(\microphone, key, resp);
	}

	removeMicrophoneResponder {arg key;
		this.removeResponderForType(\microphone, key);
	}

	addRespondersForTouch {arg key, touchBeganFunc, touchMovedFunc, touchEndedFunc;
		var resp;
		resp = OSCdef(\touchBegan, {arg msg, time, addr, recvPort;
			var touchID, touchX, touchY;
			this.check(addr).if({
				touchID = msg[2];
				touchX = msg[4];
				touchY = msg[6];
				touchBeganFunc.value(touchID, touchX, touchY);
				touches.add(touchID -> [touchX, touchY]);
			});
		}, baseAddr ++ "/touchBegan");
		this.addResponder(\touchBegan, key, resp);
		resp = OSCdef(\touchMoved, {arg msg, time, addr, recvPort;
			var touchID, touchX, touchY;
			this.check(addr).if({
				touchID = msg[2];
				touchX = msg[4];
				touchY = msg[6];
				touchMovedFunc.value(touchID, touchX, touchY);
				touches.add(touchID -> [touchX, touchY]);
			});
		}, baseAddr ++ "/touchMoved");
		touchEndedFuncs.add(key -> touchEndedFunc);
		this.addResponder(\touchMoved, key, resp);
		resp = OSCdef(\touchEnded, {arg msg, time, addr, recvPort;
			var touchID, touchX, touchY;
			this.check(addr).if({
				touchID = msg[2];
				touchX = msg[4];
				touchY = msg[6];
				touchEndedFunc.value(touchID, touchX, touchY);
				this.cleanupTouch(touchID);
			});
		}, baseAddr ++ "/touchEnded");
		this.addResponder(\touchEnded, key, resp);
	}

	removeTouchResponders {arg key;
		[\touchBegan, \touchMoved, \touchEnded].do({arg thisKey;
			this.removeResponderForType(thisKey, key);
		});
	}

	clearAllTouches {
		touches.keysValuesDo({arg key, value;
			touchEndedFuncs[key].value(key, value[0], value[1]);
			this.cleanupTouch(key);
		});
	}

	cleanupTouch {arg key;
		touchEndedFuncs.removeAt(key);
		touches.removeAt(key)
	}
}