PFKtl : MIDIKtl {
	classvar <>verbose = false; 
	var <>softWithin = 0.05, <lastVals;

	
	init { 
		super.init;
		lastVals = ();
	}

	*makeDefaults { 

		// just one bank of sliders
		defaults.put(this, 
			(
				sl01: '0_7', sl02: '1_7', sl03: '2_7', sl04: '3_7', 
				sl05: '4_7', sl06: '5_7', sl07: '6_7', sl08: '7_7', 
				sl09: '8_7', sl10: '9_7', sl11: '10_7', sl12: '11_7', 
				sl13: '12_7', sl14: '13_7', sl15: '14_7',sl16: '15_7'
			)
		);
	}
			// map to 
	mapToPxEdit { |editor, indices, lastIsVol = true| 
		var elementKeys, lastKey; 
		indices = indices ? (1..8); 
		
		elementKeys = ctlNames.keys.asArray.sort[indices - 1]; 
		
		if (lastIsVol) { 
			lastKey = elementKeys.pop;
			
				// use last slider for proxy volume
			this.mapCC(lastKey, { |ch, cc, val| 
				var lastVal = lastVals[lastKey];
				var mappedVol = \amp.asSpec.map(val / 127);
				var proxy = editor.proxy;
				if (proxy.notNil) { proxy.softVol_(mappedVol, softWithin, lastVal: lastVal) };
				lastVals[lastKey] = mappedVol;
			});
		};
		
		elementKeys.do { |key, i|  	
			this.mapCC(key, 
				{ |ch, cc, val| 
					var proxy = editor.proxy;
					var parKey =  editor.editKeys[i];
					var normVal = val / 127;
					var lastVal = lastVals[key];
					if (parKey.notNil and: proxy.notNil) { 
						proxy.softSet(parKey, normVal, softWithin, lastVal: lastVal) 
					};
					lastVals.put(key, normVal);
				}
			)
		};
	}
	
	mapToPxMix { |mixer, splitIndex = 8, lastEdIsVol = true, lastIsMaster = true| 
 	
		var server = mixer.proxyspace.server;
		var 	elementKeys = ctlNames.keys.asArray.sort; 
		var lastKey; 
		
				// add master volume on slider 16
		if (lastIsMaster) { 
			lastKey = elementKeys.pop; 
			Spec.add(\mastaVol, [server.volume.min, server.volume.max, \db]);
			this.mapCC(lastKey, { |chan, cc, val| server.volume.volume_(\mastaVol.asSpec.map(val/127)) });
		};			

			// map first n sliders to volumes
		elementKeys.keep(splitIndex).do { |key, i| 
			this.mapCC(key, 
				{ |ch, cc, val| 
					var proxy = mixer.pxMons[i].proxy; 
					var lastVal, mappedVal, lastVol;
					if (proxy.notNil) { 
						lastVal = lastVals[key]; 
						mappedVal = \amp.asSpec.map(val / 127); 
						lastVol = if (lastVal.notNil) { \amp.asSpec.map(lastVal) }; 
						proxy.softVol_( \amp.asSpec.map(mappedVal), softWithin, true, lastVol ); 
					};
					lastVals[key] =  mappedVal;
				};
			)
		};
		
		this.mapToPxEdit(mixer.editor, (splitIndex + 1 .. elementKeys.size));
	}
}
