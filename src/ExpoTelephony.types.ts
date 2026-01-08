export type SmsReceivedEvent = {
  originatingAddress: string;
  body: string;
};

export type CallReceivedEvent = {
  number: string | null;
  type: 'missed';
};

export type MissedCall = {
  number: string;
  date: number;
};

export type ExpoTelephonyModuleEvents = {
  SmsReceived: (event: SmsReceivedEvent) => void;
  CallReceived: (event: CallReceivedEvent) => void;
};
