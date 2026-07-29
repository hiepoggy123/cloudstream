const fs = require('fs');

let js = fs.readFileSync('streamfree_app.js', 'utf-16le');

let match = js.match(/(function a0_0x2be0.*?})/s);
let array_def = js.match(/(function a0_0x3142\(\)\{var _0x466855=.*?return a0_0x3142\(\);\})/s);
let shuffle = js.match(/\(function\(_0x362604,_0x3b2391\).*?}\(a0_0x3142,.*?\)\);/s);

if (array_def && shuffle && match) {
    let script = array_def[0] + '\n' + shuffle[0] + '\n' + match[0] + '\n';
    
    // Evaluate it
    eval(script);

    let strings = [];
    for (let i = -1000; i < 5000; i++) {
        try {
            let res = a0_0x2be0(i);
            if (typeof res === 'string' && res.length > 0) {
                strings.push(res);
            }
        } catch (e) {
        }
    }
    
    let apis = strings.filter(s => s.includes('/api/'));
    console.log("Found APIs:");
    apis.forEach(a => console.log(a));
    
    fs.writeFileSync('decoded_strings.txt', strings.join('\n'), 'utf-8');
} else {
    console.log("Could not find the obfuscation functions.");
}
