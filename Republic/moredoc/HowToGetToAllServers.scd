

(//!! stop all that is playing on the current server: 
r.myServer.freeAll(true); // or	
s.freeAll(true);
)
	// get to specific servers in patterns: 
Pbind(\server, r.s, \where, \otto, \instrument, \zzz, \note, Prand([0, 2, 3, 5], 8), \dur, 0.25).play;

Pbind(\server, r.s, \where, r.nameList.choose, \instrument, \zzz, \note, Prand([0, 2, 3, 5], 8), \dur, 0.25).play;

Pbind(\server, r.servers.choose, \instrument, \zzz, \note, Prand([0, 2, 3, 5], 8), \dur, 0.25).play;

(//!! in get to specific servers in events:
( server: r.servers.choose, \instrument, \xxx, \note, Prand([0, 2, 3, 5], 8), \dur, 0.25).play;
)

(//!! in patterns: 
Pbind(\server, r.s, \where, \petra).play;
)