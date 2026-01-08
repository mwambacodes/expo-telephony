import { registerWebModule, NativeModule } from 'expo';

import { ChangeEventPayload } from './ExpoTelephony.types';

type ExpoTelephonyModuleEvents = {
  onChange: (params: ChangeEventPayload) => void;
}

class ExpoTelephonyModule extends NativeModule<ExpoTelephonyModuleEvents> {
  PI = Math.PI;
  async setValueAsync(value: string): Promise<void> {
    this.emit('onChange', { value });
  }
  hello() {
    return 'Hello world! 👋';
  }
};

export default registerWebModule(ExpoTelephonyModule, 'ExpoTelephonyModule');
