fs = require("fs");

var config = {
  firmwareVersion: 'PhotoPizza AP',
  wifiSsid: 'Photo360_AP001',
  wifiPassword: 'Photo360_AP001',
  wsPort: 8000,
  state: 'waiting',//started
  framesLeft: 36,
  frame: 36,
  allSteps: 109000,
  pause: 100,
  delay: 300,
  speed: 3000,
  acceleration: 100,
  shootingMode: 'inter',
  direction: 1
};



function saveConfig() {
  fs.writeFileSync('config.txt', JSON.stringify(config));
  console.log('Save config');
}


E.flashFatFS({ format: true });
saveConfig();



var riadTimer = setTimeout(function () {
  var readConfig = JSON.parse(fs.readFileSync("config.txt"));
  console.log(readConfig);
}, 1000);
