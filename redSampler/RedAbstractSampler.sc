// f.olofsson 041119-22, 041208, 041217
// released under gnu gpl v2 license
// 060916 rewrite from scratch
// 070711 major rewrite again -thanks Till.  generalised, object style, fix for the voice release issue, using nodewatcher, abstract classes, RedSampler class etc.  but only one interface change: preload is not prepareForPlay.
// 081220 made into a quark, added loop feature
// 081221 now 8ch soudfiles maximum, added freeKey

//todo:
//		gui quad player with listview, xfadetime, play/stop, pause/resume, vol
//		gui stereo player with listview, xfadetime, play/stop, pause/resume, vol


RedAbstractSampler {
	var	<server, <>overlaps= 2, <keys;
	*new {|server|								//if server=nil it will use Server.default
		^super.new.init(server)
	}
	init {|argServer|
		keys= ();
		server= argServer ?? Server.default;
	}
	prepareForPlay {|key, path, startFrame= 0|		//read file headers and allocate in advance
		var sf;
		if(server.serverRunning.not, {(this.class.asString++": server not running").error; this.halt});
		sf= SoundFile.new;
		if(sf.openRead(path).not, {(this.class.asString++": file not found_ "++path).error; sf.close; this.halt});
		keys.put(key, {							//associate key with array of voice objects
			this.prCreateVoice(sf, startFrame);
		}.dup(overlaps));
		sf.close;
	}
	preload {|...args| this.prepareForPlay(*args)}		//for backwards compability
	loadedKeys {^keys.keys}
	length {|key|
		var voices;
		if((voices= keys[key]).notNil, {^voices[0].length}, {^nil});
	}
	channels {|key|
		var voices;
		if((voices= keys[key]).notNil, {^voices[0].channels}, {^nil});
	}
	buffers {|key|
		var voices;
		if((voices= keys[key]).notNil, {^voices.collect{|x| x.buffer}}, {^nil});
	}
	voicesLeft {|key|
		var voices;
		if((voices= keys[key]).notNil, {^voices.count{|x| x.isPlaying.not}}, {^nil});
	}
	
	//play with finite duration - if sustain=nil then use file length
	play {|key, attack= 0, sustain, release= 0, amp= 0.7, out= 0, group, loop= 0|
		var voc= this.prVoices(key).detect{|x|
			x.isPlaying.not;						//find first voice ready to play
		};
		if(voc.isNil, {
			(this.class.asString++": no free slots -increase overlaps or play slower").warn;
		}, {
			voc.play(attack, sustain, release, amp, out, group, loop);
		});
	}
	stop {|key, release= 0.4|
		var voc= this.prVoices(key).detect{|x|
			x.isPlaying and:{x.isReleased.not};		//find first voice playing and not released
		};
		if(voc.isNil, {							//perhaps remove this warning???
			(this.class.asString++": all voices already stopped").warn;
		}, {
			voc.stop(release);
		});
	}
	flush {|release= 0.4|
		keys.do{|voices| voices.do{|x| x.stop(release)}}
	}
	freeKey {|key|
		var voices= keys.removeAt(key);
		if(voices.notNil, {voices.do{|x| x.free}});
	}
	free {										//free synths, close files and free buffers
		keys.do{|voices| voices.do{|x| x.free}};
		keys= ();
	}
	
	//--private
	prVoices {|key|
		var voices= keys[key];						//find all voices objects for this key
		if(voices.isNil, {
			(this.class.asString++": key not found").warn;
		});
		^voices									//returns an array of voice objects
	}
	prCreateVoice {|sf, startFrame|
		^this.subclassResponsibility(thisMethod)
	}
}

RedAbstractSamplerVoice {
	var server, path, <channels, startFrame, numFrames, <length,
		<buffer, <synth, <isPlaying= false, <isReleased= false;
	*new {|server, path, channels, startFrame, numFrames, length|
		^super.newCopyArgs(server, path, channels, startFrame, numFrames, length).prAllocBuffer
	}
	play {|attack= 0, sustain, release= 0, amp= 0.7, out= 0, group, loop= 0|
		^this.subclassResponsibility(thisMethod)
	}
	stop {|release= 0.4|
		isReleased= true;
		synth.release(release);
	}
	free {|action|
		isReleased= false;
		synth.free;
		buffer.free({action.value});
	}
	defName {
		^this.subclassResponsibility(thisMethod)
	}
	prAllocBuffer {|action|
		^this.subclassResponsibility(thisMethod)
	}
}

/*	-- debugcode --
s.boot;
s.stopAliveThread;
s.dumpOSC(1)
d= RedDiskInSampler(s);	//normal
d= RedDiskInSamplerGiga(s);	//normal
d.preload(\e, "sounds/bosen44100/56/E2.aif");
d.play(\e, 0, 0.11, 0.1);
d.play(\e, 0, 0.3, 0);
d.play(\e, 0, 0.1, 0);
d.play(\e, 0, nil, 0);
d.play(\e, 2.1, 0.3, 0);
d.stop(\e, 5)
d.stop(\e, 0)
d.flush
d.free
*/


/*	-- testcode --
s.boot;
d= RedDiskInSampler(s);	//normal
d= RedDiskInSamplerGiga(s);	//or fast trigger version
d.preload(\a, "sounds/bosen44100/56/A2.aif");
d.preload(\f, "sounds/bosen44100/40/F2.aif");
d.preload(\e, "sounds/bosen44100/56/E2.aif");
d.voicesLeft(\e)
d.play(\e, 0.2, 0.5, 1);	//key, attackTime, amp, outbus
d.stop(\e, 1.0);			//key, releaseTime
d.play(\e, 0.1, amp:1);
d.stop(\e, 2.1);
d.play(\e);
d.stop(\e);
d.play(\a, attack: 1);
d.play(\e, attack: 1);
d.stop(\e, release: 0);
d.stop(\a, release: 0);

d.play(\e);
d.play(\a);
d.play(\f);
d.play(\e);
d.play(\a);
d.play(\f);
d.flush(release: 0.2)

d.free;
*/


/*	-- more testcode --
s.boot;
~srcGrp= Group.head(s);
~efxGrp= Group.tail(s);
d= RedDiskInSampler(s);	//normal
d= RedDiskInSamplerGiga(s);	//or fast trigger version
d.preload(\e, "sounds/bosen44100/56/E2.aif");
e= SynthDef("ringmod", {Out.ar(0, In.ar(0, 2)*LPF.ar(BrownNoise.ar, 90))}).play(~efxGrp);
d.play(\e);
d.stop(\e);
d.play(\e, group: ~srcGrp);
d.stop(\e);
s.queryAllNodes;
d.free;
e.free;

i= Server.internal.boot;
d= RedDiskInSampler(i);	//normal
d= RedDiskInSamplerGiga(i);	//or fast trigger version
d.preload(\e, "sounds/bosen44100/56/E2.aif");
d.play(\e);
d.stop(\e);
d.free;
i.quit;
*/