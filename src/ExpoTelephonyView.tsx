import { requireNativeView } from 'expo';
import * as React from 'react';

import { ExpoTelephonyViewProps } from './ExpoTelephony.types';

const NativeView: React.ComponentType<ExpoTelephonyViewProps> =
  requireNativeView('ExpoTelephony');

export default function ExpoTelephonyView(props: ExpoTelephonyViewProps) {
  return <NativeView {...props} />;
}
