
/*

(
r = Republic.new;
a = RepublicStandardTime(r);
r.join(\test);

a.startListen;
a.phaseWrap = 4;
a.listenTo(\test);
a.update;
a.verbose = true;
)

a.others.first.update(1, 460);

a.update;
a.clocks;
a.tempo;

a.others

a.tempo = rrand(1.0, 0.5).postln; a.sendClockSignal(\all);

a.clocks;

a.empathy = 1; a.confidence = 0;

a.update;
a.sendClockSignal(\test);
a.update;
a.clocks.first

a.fadeTempo(2);

a.others;
a.tempo;
a.tempo = rrand(1.0, 2.5).postln; a.sendClockSignal(\test);
a.tempo;

a.rpcList

*/

RepublicStandardTime : ListeningClock {
	
	var <republic;
	var <clocks;
	var <>listeningTo;
	var <>rpcList = #[\listenTo, \listenOnlyTo, \dontListenTo, \updateClock];
	var responder;
	
	*new { |republic|
		^super.new.initClocks(republic)	
	}
	
	initClocks { |argRepublic|
		republic = argRepublic;
		clocks = ();
		listeningTo = [];
	}
	
	// RPC calls
	
	listenTo { |name|
		if(listeningTo.includes(name).not) {
			listeningTo = listeningTo.add(name);
		}
	}
	
	listenOnlyTo { |name|
		listeningTo = [name]
	}
	
	dontListenTo { |name|
		listeningTo.remove(name)
	}
	
	updateClock { |name, tempo, beats|
		var clock = clocks.at(name);
		clock !? {
			[\update, name, tempo, beats].postcs;
			clock.update(name, tempo, beats);
		};
	}
	
	// end rpc calls

	startListen {
		responder = 
			OSCresponderNode(nil, \RepublicStandardTime, { |t, r, msg|
					var selector = msg[1];
					msg.postcs;
					if(rpcList.includes(selector)) {
						this.performList(selector, msg[2..])
					};
			}).add;
		clocks.do(_.startListen);
		super.startListen;
	}
	
	stopListen {
		responder.remove	
	}
	
	myClock {
		^clocks.at(republic.nickname)	
	}
	
	myName {
		^republic.nickname	
	}
	
	sendClockSignal { |name|
		var clock = this.myClock;
		if(clock.isNil) { "clock missing - join the republic!".warn; ^this };
		republic.send(name, \RepublicStandardTime, \updateClock, 
			this.myName, clock.tempo, clock.elapsedBeats)
	}
	
	everyoneListenOnlyToMe {
		var myName = republic.nickname;
		republic.send(\all, \RepublicStandardTime, \listenOnlyTo, myName);
	}
	
	update {
		clocks.keysDo { |key|
			if(republic.hasJoined(key).not) {
				this.removeClock(key)	
			}
		};
		republic.addrs.keysDo { |key|
			if(clocks.at(key).isNil) {
				this.addClock(key)
			}	
		};
		others = listeningTo.collect { |name| clocks.at(name) }.select(_.notNil);
		clocks.postln;
	}
	
	addClock { |key|
		var myClock = this.myClock ? TempoClock.default; // use my clock as approximation
		var clock = ReferenceClock(myClock.tempo, myClock.elapsedBeats);
		clocks.put(key, clock);
		this.update;
	}
	
	removeClock { |key|
		clocks.removeAt(key);
		this.update;	
	}
	
	
	
	
}