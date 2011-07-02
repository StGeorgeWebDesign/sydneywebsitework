

// testing
(
//  warped analog bubbles: bend only args that are freq
{
	Bend({ |argument, argName, ugen|
		nil // should return untouched
	}, {
		f = LFSaw.kr(0.4, 0, 24, LFSaw.kr([8,7.23], 0, 3, 80)).midicps; // glissando function
		CombN.ar(SinOsc.ar(f, 0, 0.04), 0.2, 0.2, 4) // echoing sine wave
	})
}.play)

(
//  warped analog bubbles: bend only args that are freq
// ??
{
	var mx = MouseX.kr(0, 1);
	Bend({ |argument, argName, ugen|
		if(argName == \freq) { argument + (LFNoise2.kr(10 * mx) * 0.3 * ugen) }
	}, {
		f = LFSaw.kr(0.4, 0, 24, LFSaw.kr([8,7.23], 0, 3, 80)).midicps; // glissando function
		CombN.ar(SinOsc.ar(f, 0, 0.04), 0.2, 0.2, 4) // echoing sine wave
	})
}.play)
