// Reexport the native module. On web, it will be resolved to ExpoTelephonyModule.web.ts
// and on native platforms to ExpoTelephonyModule.ts
export { default } from './src/ExpoTelephonyModule';
export { default as ExpoTelephonyView } from './src/ExpoTelephonyView';
export * from  './src/ExpoTelephony.types';
