DayTimer { 	classvar <all, <top;	var <dates, <skip, <name;		*new { |name, id, test, func| 		var res = this.at(name); 		if (id.notNil) { res.putDate(id, [test, func]) };		^res;	}	*at { |name| 		^all[name] ?? { ^super.new.init(name) };	}	init { |inname|		name = inname;		dates = ();		all.put(name, this);		skip = SkipJack({ this.checkList; }, 1, name: name).stop;	}		*initClass { 		all = ();		top = this.new(\top);	}
		*start { 	top.skip.start }	*stop { 	top.skip.stop }		*putDate { |id, test, func| top.putDate(id, test, func) }	*removeAt { |id| ^top.removeAt(id) }	start { 	skip.start }	stop { 	skip.stop }	putDate { |id, test, func| dates.put(id, [test, func]) }	removeAt { |id| ^dates.removeAt(id) }		checkList { 		var date = Date.getDate;		var timeNow = date.hour * 60 + date.minute * 60 + date.second;		dates.keysValuesDo { |id, pair| 			var test, func; #test, func = pair; 			try { 				if (test.isArray) { test = (test * [3600, 60, 1]).sum };				if (test.isNumber) { 					if (test == timeNow) { func.value(date, timeNow) }				} { 				//	"test is a function...".postln;					if (test.value(date) == true) { func.value }				}			} { |err| 				("DayTimer(\\" ++ name ++ ") failed to execute a function:").warn; 				func.postcs;								err.reportError;			}		};	}}