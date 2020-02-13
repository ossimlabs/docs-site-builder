const PROXY_CONFIG = [{
    context: [
      "/czml",
    ],
    target: "http://localhost:8081",
    secure: false
  },
  /*{
    context: [
      "/geoserver",
    ],
    target: "http://10.0.0.191:8081",
    secure: false
  }*/
]

module.exports = PROXY_CONFIG;
