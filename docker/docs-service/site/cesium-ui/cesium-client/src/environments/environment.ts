// This file can be replaced during build by using the `fileReplacements` array.
// `ng build --prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.


import { KeycloakConfig } from 'keycloak-angular';


const KEYCLOAK_CONFIG: KeycloakConfig = {
  url: 'http://10.0.0.188:9080/auth',
  realm: 'Kelly',
  clientId: 'kelly-app',
  credentials: {
    secret: 'a382c8ac-94b2-4489-a416-031ba61efc6b'
  }
};


export const environment = {
  production: false,
  API_ENDPOINT: 'http://10.0.0.188:25116',
  CLASSIFICATION_BANNER: {
    text: 'UNCLASSIFIED',
    class: 'unclassified'
  },
  KEEP_ALIVE: 'KEEP_ALIVE',
  KEYCLOAK_CONFIG,
  KEYCLOACK_ACTIVATE: true,
};

/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
// import 'zone.js/dist/zone-error';  // Included with Angular CLI.
