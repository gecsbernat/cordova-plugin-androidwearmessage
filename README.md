# Cordova Plugin AndroidWearMessage

This plugin helps you communicate between Ionic/Cordova Android App and Android Wear OS App

## Install:
```bash
$ cordova plugin add https://github.com/gecsbernat/cordova-plugin-androidwearmessage.git
```

## Use:

### Ionic:
```typescript
import { Injectable } from "@angular/core";
import { Platform } from "@ionic/angular";
import { Observable } from "rxjs";
declare const AndroidWearMessage: any;

@Injectable({ providedIn: 'root' })
export class AndroidWearConnectService {

    wearConnectEnabled = false;

    constructor(
        private platform: Platform
    ) { }

    initializeAndroidWearConnection(): Promise<any> {
        return new Promise((resolve, reject) => {
            if (this.platform.is('cordova') && this.platform.is('android')) {
                AndroidWearMessage.initialize((success: any) => {
                    console.log(success);
                    this.wearConnectEnabled = true;
                    resolve(success)
                }, (error: any) => {
                    this.wearConnectEnabled = false;
                    reject(error);
                });
            } else {
                this.wearConnectEnabled = false;
                reject('NOT_CORDOVA_ON_ANDROID');
            }
        });
    }

    sendMessage(message: string): Promise<any> {
        return new Promise((resolve, reject) => {
            if (this.wearConnectEnabled) {
                AndroidWearMessage.sendMessage(message, (success: any) => {
                    resolve(success);
                }, (error: any) => {
                    reject(error);
                });
            } else {
                reject('WEAR_CONNECT_NOT_ENABLED');
            }
        });
    }

    listenMessage(): Observable<any> {
        return new Observable((observer) => {
            if (this.wearConnectEnabled) {
                AndroidWearMessage.listenMessage((message: any) => {
                    observer.next(message);
                }, (error: any) => {
                    observer.error(error);
                });
            } else {
                observer.error('WEAR_CONNECT_NOT_ENABLED');
            }
        });
    }

}

```

### Java:
```Java
public class MainActivity extends Activity implements MessageClient.OnMessageReceivedListener {

    public static final String MESSAGE_PATH = "/cordova_message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Wearable.getMessageClient(this).addListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.getMessageClient(this).removeListener(this);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(MESSAGE_PATH)) {
            String message = new String(messageEvent.getData());
            Log.d("MESSAGE", message);
        }
    }

    private class MessageRunnable implements Runnable {
        @Override
        public void run() {
            Task<List<Node>> nodesTask = Wearable.getNodeClient(MainActivity.this).getConnectedNodes();
            nodesTask.addOnSuccessListener(new OnSuccessListener<List<Node>>() {
                @Override
                public void onSuccess(List<Node> nodes) {
                    for (Node node : nodes) {
                        Wearable.getMessageClient(MainActivity.this).sendMessage(node.getId(), MESSAGE_PATH, "HELLO_CORDOVA".getBytes());
                    }
                }
            });
        }
    }
}
```

### Advertise capabilities
https://developer.android.com/training/wearables/data-layer/messages#advertise-capabilities
