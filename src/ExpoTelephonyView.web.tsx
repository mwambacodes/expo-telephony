import * as React from 'react';

import { ExpoTelephonyViewProps } from './ExpoTelephony.types';

export default function ExpoTelephonyView(props: ExpoTelephonyViewProps) {
  return (
    <div>
      <iframe
        style={{ flex: 1 }}
        src={props.url}
        onLoad={() => props.onLoad({ nativeEvent: { url: props.url } })}
      />
    </div>
  );
}
