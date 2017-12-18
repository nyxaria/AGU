package nyxaria.github.com.agu;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;

import java.util.ArrayList;

public class SetupFragment extends Fragment {

    BluetoothController btController;
    Switch bluetoothSwitch;

    Button connectButton;

    private ArrayAdapter<String> devicesArrayAdapter;
    ArrayList<String> macAdresses = new ArrayList<>();
    private ListView bluetoothList;
    private int mSelectedItem;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setup, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        btController = ((MainActivity) getActivity()).bluetoothController;
        bluetoothList = (ListView) getView().findViewById(R.id.devicesList);
        devicesArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.device_name);
        bluetoothList.setAdapter(devicesArrayAdapter);
        bluetoothList.setVisibility(btController.isEnabled() ? View.VISIBLE : View.INVISIBLE);
        bluetoothList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);


        bluetoothList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectedItem = position;
                for (int i = 0; i < bluetoothList.getChildCount(); i++) {
                    if (position == i) {
                        bluetoothList.getChildAt(i).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.secondaryNegative));
                    } else {
                        bluetoothList.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
                    }
                }
                if (!getView().findViewById(R.id.connectButton).isEnabled()) {
                    getView().findViewById(R.id.connectButton).setEnabled(true);
                }

                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

            }
        });

        connectButton = getView().findViewById(R.id.connectButton);
        //if(!btController.isEnabled()) connectButton.setEnabled(false);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = false;
                for(int i = 0; i < bluetoothList.getAdapter().getCount(); i++) {
                    if(bluetoothList.isItemChecked(i)) {
                        checked = true;
                    }
                }
                if(!checked) return;

                btController.cancelDiscovery();

                for (BluetoothDevice device : btController.getDevices()) {
                    if (device.getAddress().equals(macAdresses.get(mSelectedItem))) {
                        btController.connectToDevice(device);
                        //getView().findViewById(R.id.bluetoothProgressBar).setVisibility(View.INVISIBLE);
                        getView().findViewById(R.id.connectButton).setEnabled(false);
                    }
                }
            }
        });

        BluetoothDevice[] pairedDevices = btController.getPairedDevies();
        for (BluetoothDevice device : pairedDevices) {
            devicesArrayAdapter.add(device.getName());
            macAdresses.add(device.getAddress());
        }


        bluetoothSwitch = (Switch) getView().findViewById(R.id.bluetoothSwitch);
        bluetoothSwitch.setChecked(btController.isEnabled());

        bluetoothSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //connectButton.setEnabled(true);
                    BluetoothDevice[] pairedDevices = btController.getPairedDevies();
                    for (BluetoothDevice device : pairedDevices) {
                        if(!macAdresses.contains(device.getAddress())) {
                            devicesArrayAdapter.add(device.getName());
                            macAdresses.add(device.getAddress());
                        }
                    }
                    btController.enable();
                } else {
                    //connectButton.setEnabled(false);
                    btController.disable();
                }
            }
        });

        if (btController.isEnabled()) {
            getView().findViewById(R.id.bluetoothProgressBar).setVisibility(View.VISIBLE);
            bluetoothList.setVisibility(View.VISIBLE);
        } else {
            getView().findViewById(R.id.bluetoothProgressBar).setVisibility(View.INVISIBLE);
            bluetoothList.setVisibility(View.INVISIBLE);
        }

    }


    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) { //discovering new devices
                btController.addDevice((BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
            }

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) { //Bt state change
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        bluetoothSwitch.setChecked(false);
                        bluetoothList.setVisibility(View.INVISIBLE);
                        getView().findViewById(R.id.bluetoothProgressBar).setVisibility(View.INVISIBLE);
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        bluetoothSwitch.setChecked(false);
                        getView().findViewById(R.id.bluetoothProgressBar).setVisibility(View.INVISIBLE);
                        bluetoothList.setVisibility(View.INVISIBLE);
                        break;
                    case BluetoothAdapter.STATE_ON:
                        bluetoothSwitch.setChecked(true);
                        getView().findViewById(R.id.bluetoothProgressBar).setVisibility(View.VISIBLE);
                        bluetoothList.setVisibility(View.VISIBLE);
                        btController.startDiscovery();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        bluetoothSwitch.setChecked(true);
                        getView().findViewById(R.id.bluetoothProgressBar).setVisibility(View.VISIBLE);
                        bluetoothList.setVisibility(View.VISIBLE);
                        break;
                }
            }

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //getView().findViewById(R.id.bluetoothProgressBar).setVisibility(View.INVISIBLE);
                btController.startDiscovery();
            }
        }
    };


    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        getActivity().registerReceiver(mBroadcastReceiver, filter);

        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(mBroadcastReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(mBroadcastReceiver);
    }

    public void updateBluetoothList(BluetoothDevice device) {
        for (String address : macAdresses) {
            if (device.getAddress().equals(address)) { //already added
                return;
            }
        }
        if (device.getName() != null) {
            devicesArrayAdapter.add(device.getName());
        } else {
            devicesArrayAdapter.add(device.getAddress());
        }
        macAdresses.add(device.getAddress());
    }

    public void restart() {
        getView().findViewById(R.id.bluetoothProgressBar).setVisibility(View.VISIBLE);
        connectButton.setVisibility(View.VISIBLE);
        if (!getView().findViewById(R.id.connectButton).isEnabled()) {
            getView().findViewById(R.id.connectButton).setEnabled(true);
        }
    }
}