//redFrik


PLine : Pattern {
	var <>start, <>end, <>dur, <>mul, <>add, <>length;
	*new {|start= 0, end= 1, dur= 1, mul= 1, add= 0, length= inf|
		^super.newCopyArgs(start, end, dur, mul, add, length);
	}
	storeArgs {^[start, end, dur, mul, add, length]}
	embedInStream {|inval|
		var mulStr= mul.asStream;
		var addStr= add.asStream;
		var mulVal, addVal;
		var counter= 0;
		length.value.do{
			addVal= addStr.next(inval);
			mulVal= mulStr.next(inval);
			if(addVal.isNil or:{mulVal.isNil}, {^inval});
			inval= (counter.linlin(0, 1, start, end)*mulVal+addVal).yield;
			counter= (counter+dur.reciprocal).min(1);
		};
		^inval;
	}
}

PXLine : PLine {
	*new {|start= 1, end= 2, dur= 1, mul= 1, add= 0, length= inf|
		^super.newCopyArgs(start, end, dur, mul, add, length);
	}
	embedInStream {|inval|
		var mulStr= mul.asStream;
		var addStr= add.asStream;
		var mulVal, addVal;
		var counter= 0;
		length.value.do{
			addVal= addStr.next(inval);
			mulVal= mulStr.next(inval);
			if(addVal.isNil or:{mulVal.isNil}, {^inval});
			inval= (counter.linexp(0, 1, start, end)*mulVal+addVal).yield;
			counter= (counter+dur.reciprocal).min(1);
		};
		^inval;
	}
}

PLinExp : FilterPattern {
	var <>srclo, <>srchi, <>dstlo, <>dsthi;
	*new {|pattern, srclo= 0, srchi= 1, dstlo= 1, dsthi= 2|
		^super.newCopyArgs(pattern, srclo, srchi, dstlo, dsthi);
	}
	storeArgs {^[pattern, srclo, srchi, dstlo, dsthi]}
	embedInStream {|inval|
		var evtStr= pattern.asStream;
		var sloStr= srclo.asStream;
		var shiStr= srchi.asStream;
		var dloStr= dstlo.asStream;
		var dhiStr= dsthi.asStream;
		var outVal, sloVal, shiVal, dloVal, dhiVal;
		loop{
			outVal= evtStr.next(inval);
			if(outVal.isNil, {^inval});
			sloVal= sloStr.next(outVal);
			if(sloVal.isNil, {^inval});
			shiVal= shiStr.next(outVal);
			if(shiVal.isNil, {^inval});
			dloVal= dloStr.next(outVal);
			if(dloVal.isNil, {^inval});
			dhiVal= dhiStr.next(outVal);
			if(dhiVal.isNil, {^inval});
			
			inval= outVal.linexp(sloVal, shiVal, dloVal, dhiVal).yield;
		};
	}
}

PLinLin : FilterPattern {
	var <>srclo, <>srchi, <>dstlo, <>dsthi;
	*new {|pattern, srclo= 0, srchi= 1, dstlo= 1, dsthi= 2|
		^super.newCopyArgs(pattern, srclo, srchi, dstlo, dsthi);
	}
	storeArgs {^[pattern, srclo, srchi, dstlo, dsthi]}
	embedInStream {|inval|
		var evtStr= pattern.asStream;
		var sloStr= srclo.asStream;
		var shiStr= srchi.asStream;
		var dloStr= dstlo.asStream;
		var dhiStr= dsthi.asStream;
		var outVal, sloVal, shiVal, dloVal, dhiVal;
		loop{
			outVal= evtStr.next(inval);
			if(outVal.isNil, {^inval});
			sloVal= sloStr.next(outVal);
			if(sloVal.isNil, {^inval});
			shiVal= shiStr.next(outVal);
			if(shiVal.isNil, {^inval});
			dloVal= dloStr.next(outVal);
			if(dloVal.isNil, {^inval});
			dhiVal= dhiStr.next(outVal);
			if(dhiVal.isNil, {^inval});
			
			inval= outVal.linlin(sloVal, shiVal, dloVal, dhiVal).yield;
		};
	}
}
