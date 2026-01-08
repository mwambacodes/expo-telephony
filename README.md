# expo-telephony

A robust Expo module for managing Android Telephony features (SMS, Calls, USSD) and maintaining a persistent background service.

## Features

- **SMS**: Send and Receive SMS (via BroadcastReceiver).
- **Calls**: Detect missed calls.
- **USSD**: Send USSD requests and receive responses (Android O+).
- **Background Service**: Persistent Foreground Service with customizable notifications.

## Installation

```bash
npm install expo-telephony
```

### Android Configuration

This module handles most of its configuration automatically via Manifest Merging. However, you must ensure you request the necessary permissions at runtime in your application.

#### Permissions used:
- `RECEIVE_SMS`, `READ_SMS`, `SEND_SMS`
- `READ_CALL_LOG`
- `CALL_PHONE`
- `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_DATA_SYNC`
- `POST_NOTIFICATIONS`

## Usage

```typescript
import ExpoTelephony from 'expo-telephony';

// Start the background service
await ExpoTelephony.startService("Gateway is Monitoring");

// Send an SMS
await ExpoTelephony.sendSms("+1234567890", "Hello from Expo!");

// Exec USSD
const response = await ExpoTelephony.sendUssd("*149*01#");

// Events
import { DeviceEventEmitter } from 'react-native';

DeviceEventEmitter.addListener('SmsReceived', (event) => {
  console.log("New SMS:", event.body, "from", event.originatingAddress);
});
```

## License

MIT
