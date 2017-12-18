package nyxaria.github.com.agu;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;


public class BluetoothController {

    private static final String TAG = "nyxaria.github.com.aug";

    BluetoothAdapter mBluetoothAdapter;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    private Handler mHandler; // handler that gets info from Bluetooth service

    public static final String UUID = "E77FA4A0-B21C-11E7-8F1A-0800200C9A66";

    ArrayList<BluetoothDevice> devices = new ArrayList<>();
    public Fragment currentFragment;

    public BluetoothController(Handler mHandler) {
        this.mHandler = mHandler;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        startDiscovery();
    }

    public void write(String str) {
        if (str.length() > 0) {
            connectedThread.write(str.getBytes());

        }
    }

    public void startDiscovery() {
        mBluetoothAdapter.startDiscovery();
    }
    public void stopDiscovery() {
        mBluetoothAdapter.cancelDiscovery();
    }

    public boolean isEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    public void enable() {
        mBluetoothAdapter.enable();
    }

    public void disable() {
        mBluetoothAdapter.disable();
        devices.clear();
    }

    public void cancelDiscovery() {
        mBluetoothAdapter.cancelDiscovery();
    }

    public BluetoothDevice[] getPairedDevies() {
        Set<BluetoothDevice> pairs = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairs) {
            if (!devices.contains(device)) {
                devices.add(device);
            }
        }
        return pairs.toArray(new BluetoothDevice[0]);
    }

    public void addDevice(BluetoothDevice device) { //add device and ping UI
        devices.add(device);
        Log.d("found", device.getAddress());

        if(currentFragment instanceof SetupFragment) {
            ((SetupFragment) currentFragment).updateBluetoothList(device);
        }
    }

    public BluetoothDevice[] getDevices() {
        return devices.toArray(new BluetoothDevice[0]);
    }

    public void connectToDevice(BluetoothDevice device) {
        Log.d("d", "connecting to " + device.getAddress());
        connectThread = new ConnectThread(device);
        connectThread.start();
    }


    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;

            Message writtenMsg = mHandler.obtainMessage(
                    MainActivity.Constants.MESSAGE_STATUS, "CONNECTED");
            writtenMsg.sendToTarget();
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    Message readMsg = mHandler.obtainMessage(
                            MainActivity.Constants.MESSAGE_READ, numBytes, -1,
                            mmBuffer);
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Message writtenMsg = mHandler.obtainMessage(
                            MainActivity.Constants.MESSAGE_ERROR, "Device disconnected");
                    writtenMsg.sendToTarget();
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);

                // Share the sent message with the UI activity.
//                Message writtenMsg = mHandler.obtainMessage(
//                        MainActivity.Constants.MESSAGE_WRITE, -1, -1, mmBuffer);
//                writtenMsg.sendToTarget();
                sleep(10);
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
                Message writtenMsg = mHandler.obtainMessage(
                        MainActivity.Constants.MESSAGE_ERROR, "Can't connect to device");
                writtenMsg.sendToTarget();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                tmp = device.createRfcommSocketToServiceRecord(java.util.UUID.fromString(UUID));
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            mBluetoothAdapter.cancelDiscovery();

            try {
                mmSocket.connect();
            } catch (IOException connectException) {
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }

                Message writtenMsg = mHandler.obtainMessage(
                        MainActivity.Constants.MESSAGE_ERROR, "Can't connect to " + mmDevice.getName());
                writtenMsg.sendToTarget();

                return;
            }

            connectedThread = new ConnectedThread(mmSocket);
            connectedThread.start();


        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }
}
