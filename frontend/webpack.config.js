const { shareAll, withModuleFederationPlugin } = require('@angular-architects/module-federation/webpack');

module.exports = withModuleFederationPlugin({

  remotes: {
    "authMfe": "http://localhost:4201/remoteEntry.js",
    "claimsMfe": "http://localhost:4202/remoteEntry.js",
    "intelligenceMfe": "http://localhost:4203/remoteEntry.js",
    "vendorMfe": "http://localhost:4205/remoteEntry.js",
  },

  shared: {
    ...shareAll({ singleton: true, strictVersion: true, requiredVersion: 'auto' }),
  },

});
