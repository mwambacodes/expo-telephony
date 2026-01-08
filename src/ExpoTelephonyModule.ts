import { NativeModule, requireNativeModule } from 'expo';

import { ExpoTelephonyModuleEvents, MissedCall } from './ExpoTelephony.types';

declare class ExpoTelephonyModule extends NativeModule<ExpoTelephonyModuleEvents> {
  startService(message: string): Promise<void>;
  stopService(): Promise<void>;
  sendSms(phoneNumber: string, message: string): Promise<string>;
  getMissedCalls(limit: number): Promise<MissedCall[]>;
  sendUssd(ussdCode: string): Promise<string>;
}

// This call loads the native module object from the JSI.
export default requireNativeModule<ExpoTelephonyModule>('ExpoTelephony');
