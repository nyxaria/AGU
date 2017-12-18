package nyxaria.github.com.agu;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.github.amlcurran.showcaseview.targets.Target;
import com.mindorks.placeholderview.PlaceHolderView;

import net.steamcrafted.materialiconlib.MaterialIconView;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {


    public static final int DEFAULT_THRESHOLD = 50;
    final String PREFS_NAME = "AGU";

    public static int WIDTH, HEIGHT;


    private ShowcaseView showcaseView;

    public static ArrayList<String> SEED_TYPES;
    private PlaceHolderView mDrawerView;
    private DrawerLayout mDrawer;
    protected Toolbar mToolbar;
    private TextView mToolbarTitle;
    private Class currentFragmentType;
    private Fragment currentFragment;

    private ActionBarDrawerToggle drawerToggle;

    public static boolean firstRun;

    ArrayList<Pot> pots = new ArrayList<>();
    ArrayList<SeedBank> seedBanks = new ArrayList<>();
    public Pot lastRemoved;

    private boolean showingDialog;
    public boolean creatingPot;
    private boolean debugging;
    private long timeSinceClosing;

    public int getSeedBank(String seed) {
        if(seed.equals("Empty")) return -1;
        for(SeedBank s : seedBanks) {
            if(s.seed.equals(seed)) {
                return s.index;
            }
        }
        return -1;
    }

    final Handler handler = new Handler();
    Runnable gardenTransitionRunnable = new Runnable() {
        @Override
        public void run() {
            Fragment f = null;
            try {
                f = MainActivity.this.getSupportFragmentManager().findFragmentById(R.id.fragment);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(f instanceof GardenFragment) {
                if(((GardenFragment) f).gardenView.transitioning) {
                    ((GardenFragment) f).gardenView.invalidate();
                    mHandler.postDelayed(this, 5);
                } else {
                    handler.removeCallbacks(gardenTransitionRunnable);
                }
            }
        }
    };

    interface Constants {
        int MESSAGE_WRITE = 0;
        int MESSAGE_READ = 1;
        int MESSAGE_ERROR = 2;
        int MESSAGE_STATUS = 3;
    }

    public final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            switch(msg.what) {
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
//                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    Log.d("write", writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Log.d("read", readMessage);
                    //bluetoothController.write(readMessage);
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    int width = displayMetrics.widthPixels;

                    Fragment f = null;
                    try {
                        f = MainActivity.this.getSupportFragmentManager().findFragmentById(R.id.fragment);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    //META:seed|types|data,width|height
                    if(readMessage.startsWith("META:")) {
                        pots.clear();

                        readMessage = readMessage.split(":")[1];
                        String[] data = readMessage.split(",");
                        // 1st = seed types one|two|three
                        SEED_TYPES = new ArrayList<>();
                        SEED_TYPES.add("Empty");
                        for(String s : data[0].split("~")) {
                            SEED_TYPES.add(s);
                        }


                        String[] dims = data[1].split("~");

                        WIDTH = Integer.parseInt(dims[0]);
                        HEIGHT = Integer.parseInt(dims[1]);

                        Log.d("meta", WIDTH + "," + HEIGHT);
                        GardenView.xRatio = (width - GardenView.offset * 2) / ((float) MainActivity.WIDTH);
                        GardenView.yRatio = (width - GardenView.offset * 2) / ((float) MainActivity.HEIGHT);
                        handler.post(gardenTransitionRunnable);


                    } else if(readMessage.startsWith("SEED:")) {
                        readMessage = readMessage.split(":")[1];
                        //format: index,seed,color
                        SeedBank.load(readMessage, seedBanks);
                        final Fragment finalFra = f;
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(finalFra instanceof GardenFragment)
                                    ((GardenFragment) finalFra).gardenView.invalidate();
                            }
                        }, 1);
                    } else if(readMessage.startsWith("POT:")) {

                        readMessage = readMessage.split(":")[1];
                        Pot.load(readMessage, pots);

                        final Handler handler = new Handler();
                        final Fragment finalFra = f;
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(finalFra instanceof GardenFragment)
                                    ((GardenFragment) finalFra).gardenView.invalidate();
                            }
                        }, 1);

                    }
                    //Toast.makeText(MainActivity.this, readMessage, Toast.LENGTH_LONG).show();

                    break;
                case Constants.MESSAGE_ERROR:
                    runOnUiThread(new Runnable() {
                        public void run() {
                            try {
                                pots = new ArrayList<Pot>();
                                seedBanks = new ArrayList<SeedBank>();
                                for(int i = 0; i < GardenView.SEED_BANKS; i++) {
                                    seedBanks.add(new SeedBank(i, "Empty", Color.parseColor("#11333344")));
                                }
                                SEED_TYPES = null;

                                Fragment f = null;
                                try {
                                    f = MainActivity.this.getSupportFragmentManager().findFragmentById(R.id.fragment);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if(f instanceof SetupFragment) //check if on setup screen
                                    ((SetupFragment) f).restart();
                                else {
                                    save();
                                    createFragment(SetupFragment.class);

                                }

                                //Log.d("AGU", "Restarting setup");
                                //Toast.makeText(MainActivity.this, (String) msg.obj, Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                            }
                        }
                    });
                    break;
                case Constants.MESSAGE_STATUS:
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if(msg.obj.equals("CONNECTED")) {
                                //MainActivity.this.setDrawerState(true);
                                if(firstRun) {
                                    startTutorial();
                                }
                                Fragment fra = null;
                                try {
                                    fra = MainActivity.this.getSupportFragmentManager().findFragmentById(R.id.fragment);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if(fra instanceof SetupFragment) //check if on setup screen
                                    ((SetupFragment) fra).connectButton.setVisibility(View.GONE);

                                MainActivity.this.createFragment(GardenFragment.class);
                            }
                        }
                    });
            }
        }
    };


    public void save() {
        if(pots == null || seedBanks == null) return;
        if(pots.size() == 0 && seedBanks.size() == 0) return;
        String data = "";
        if(pots.size() != 0) {
            data = "POTS:";
            for(Pot pot : pots) {
                Log.d("d,", pot.x + "," + pot.y);
                if(pot.x >= 0 && pot.y >= 0 && pot.xDim > 0 && pot.yDim > 0)
                    data += pot.toString() + "~";
            }

            data = data.substring(0, data.length() - 1);
            bluetoothController.write(data);
        }

        if(seedBanks.size() != 0) {
            data = "SEEDS:";

            for(SeedBank seed : seedBanks) {
                data += seed.toString() + "~";
            }
            data = data.substring(0, data.length() - 1);

            bluetoothController.write(data);
        }
    }

    public BluetoothController bluetoothController = new BluetoothController(mHandler);
    int n = 0;

    @Override
    public void onStart() {
        super.onStart();
        WIDTH = 0;
        HEIGHT = 0;
        pots = new ArrayList<Pot>();
        seedBanks = new ArrayList<SeedBank>();
        for(int i = 0; i < GardenView.SEED_BANKS; i++) {
            seedBanks.add(new SeedBank(i, "Empty", Color.parseColor("#11333344")));
        }
        SEED_TYPES = null;

        Fragment f = null;
        try {
            f = MainActivity.this.getSupportFragmentManager().findFragmentById(R.id.fragment);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(f instanceof SetupFragment) //check if on setup screen
            ((SetupFragment) f).restart();
        else {
            save();
            createFragment(SetupFragment.class);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);

        final SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        if(settings.getBoolean("firstRun", true)) {
            firstRun = true;
            settings.edit().putBoolean("firstRun", false).apply();
        }


        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

        currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment);
        bluetoothController.currentFragment = currentFragment;

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mDrawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        //mDrawer.getForeground().setAlpha( 0);
        // mDrawer.setPadding(20,0,0,0);
        mDrawerView = (PlaceHolderView) findViewById(R.id.drawerView);
        drawerToggle = new ActionBarDrawerToggle(this, mDrawer,
                R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setupDrawer();
        mToolbarTitle = (TextView) findViewById(R.id.toolbar_title);

        View.OnTouchListener alphaListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(currentFragmentType == GardenFragment.class) {
                    ((GardenFragment) currentFragment).gardenView.alpha = 0;
                    ((GardenFragment) currentFragment).gardenView.invalidate();
                }
                return false;
            }
        };
        mToolbarTitle.setOnTouchListener(alphaListener);

        mToolbar.setOnTouchListener(alphaListener);

        findViewById(R.id.toolbar_x).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(currentFragmentType == GardenFragment.class) {
                    if(pots.size() != 0)
                        if(((GardenFragment) currentFragment).gardenView.transitioning) {
                            return true;
                        }
                }
                timeSinceClosing = System.currentTimeMillis();

                creatingPot = false;
                findViewById(R.id.toolbar_title).setVisibility(View.VISIBLE);
                findViewById(R.id.toolbar_plus).setVisibility(View.VISIBLE);
                findViewById(R.id.toolbar_x).setVisibility(View.GONE);
                findViewById(R.id.toolbar_rotate).setVisibility(View.GONE);
                findViewById(R.id.toolbar_tick).setVisibility(View.GONE);

                ((GardenFragment) currentFragment).cancelAddingPot();
                setDrawerState(true);
                return false;
            }
        });

        findViewById(R.id.toolbar_rotate).setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ((GardenFragment) currentFragment).rotate();
                return false;
            }
        });

        findViewById(R.id.toolbar_tick).setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                timeSinceClosing = System.currentTimeMillis();

                findViewById(R.id.toolbar_title).setVisibility(View.VISIBLE);
                findViewById(R.id.toolbar_plus).setVisibility(View.VISIBLE);
                findViewById(R.id.toolbar_x).setVisibility(View.GONE);
                findViewById(R.id.toolbar_rotate).setVisibility(View.GONE);
                findViewById(R.id.toolbar_tick).setVisibility(View.GONE);
                setDrawerState(true);
                ((GardenFragment) currentFragment).finalise();
                ((GardenFragment) currentFragment).gardenView.invalidate();
                creatingPot = false;

                return false;
            }
        });


        findViewById(R.id.toolbar_plus).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("d", showingDialog + "||" + creatingPot + "||" + (SEED_TYPES == null) + "||" + (seedBanks == null) + "||" + (System.currentTimeMillis() - timeSinceClosing < 1000));
                if(currentFragmentType == GardenFragment.class) {
                    if(pots.size() != 0)
                        if(((GardenFragment) currentFragment).gardenView.transitioning) {
                            return true;
                        }
                }
                if(showingDialog || creatingPot || SEED_TYPES == null || seedBanks == null || System.currentTimeMillis() - timeSinceClosing < 1000)
                    return true;
                n = 0;
                LayoutInflater layoutInflaterAndroid = LayoutInflater.from(MainActivity.this);
                final View mView = layoutInflaterAndroid.inflate(R.layout.pot_creation_dialog, null);
                final AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilderUserInput.setView(mView);

                final Spinner seedType = mView.findViewById(R.id.pot_spinner_seed);
                final ArrayList<String> seeds = new ArrayList();
                int arrIndex = 0;
                for(int i = 0; i < SEED_TYPES.size() - 1; i++) { //remove Empty
                    if(getSeedBank(SEED_TYPES.get(i + 1)) != -1) {
                        seeds.add(SEED_TYPES.get(i + 1));
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                        android.R.layout.simple_spinner_item, seeds.toArray(new String[0]));
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                seedType.setAdapter(adapter);
                if(settings.getInt("popup_seedPos", 0) < seeds.size())
                    seedType.setSelection(settings.getInt("popup_seedPos", 0));

                final boolean[] first = {true};
                ((EditText) mView.findViewById(R.id.pot_dim1)).addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if(first[0])
                            ((EditText) mView.findViewById(R.id.pot_dim2)).setText(s.toString());
                    }
                });
                ((EditText) mView.findViewById(R.id.pot_dim1)).setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if(first[0]) first[0] = hasFocus;
                    }
                });
                final android.support.v7.widget.SwitchCompat potType = mView.findViewById(R.id.pot_switch_type);
                potType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked) { //circle
                            mView.findViewById(R.id.pot_dim).setVisibility(View.VISIBLE);
                            mView.findViewById(R.id.pot_dim1).setVisibility(View.INVISIBLE);
                            mView.findViewById(R.id.pot_dim2).setVisibility(View.INVISIBLE);
                        } else {
                            mView.findViewById(R.id.pot_dim).setVisibility(View.INVISIBLE);
                            mView.findViewById(R.id.pot_dim1).setVisibility(View.VISIBLE);
                            mView.findViewById(R.id.pot_dim2).setVisibility(View.VISIBLE);
                        }
                    }
                });


                final CheckBox check = mView.findViewById(R.id.checkbox_pop);
                check.setChecked(settings.getBoolean("popup_Remember", false));

                potType.setChecked(settings.getBoolean("popup_Type", false));
                if(potType.isChecked()) {//circle
                    ((EditText) mView.findViewById(R.id.pot_dim)).setText(settings.getString("popup_d1", ""));

                    mView.findViewById(R.id.pot_dim).setVisibility(View.VISIBLE);
                    mView.findViewById(R.id.pot_dim1).setVisibility(View.INVISIBLE);
                    mView.findViewById(R.id.pot_dim2).setVisibility(View.INVISIBLE);
                } else {
                    ((EditText) mView.findViewById(R.id.pot_dim1)).setText(settings.getString("popup_d1", ""));
                    ((EditText) mView.findViewById(R.id.pot_dim2)).setText(settings.getString("popup_d2", ""));

                    mView.findViewById(R.id.pot_dim).setVisibility(View.INVISIBLE);
                    mView.findViewById(R.id.pot_dim1).setVisibility(View.VISIBLE);
                    mView.findViewById(R.id.pot_dim2).setVisibility(View.VISIBLE);
                }

                if(seeds.size() == 0) {
                    potType.setVisibility(View.GONE);
                    seedType.setVisibility(View.GONE);
                    mView.findViewById(R.id.checkbox_pop).setVisibility(View.GONE);
                    mView.findViewById(R.id.pot_dim).setVisibility(View.GONE);
                    mView.findViewById(R.id.popup_labelRect).setVisibility(View.GONE);
                    mView.findViewById(R.id.popup_labelCirlce).setVisibility(View.GONE);

                    mView.findViewById(R.id.pot_dim1).setVisibility(View.GONE);
                    mView.findViewById(R.id.pot_dim2).setVisibility(View.GONE);
                    mView.findViewById(R.id.pot_warning).setVisibility(View.VISIBLE);

                    alertDialogBuilderUserInput
                            .setCancelable(false)
                            .setNegativeButton("Go to Seed Bank", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogBox, int id) {
                                    dialogBox.cancel();
                                    showingDialog = false;
                                    setDrawerState(true);
                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                                    imm.hideSoftInputFromWindow(((EditText) mView.findViewById(R.id.pot_dim)).getWindowToken(), 0);
                                    imm.hideSoftInputFromWindow(((EditText) mView.findViewById(R.id.pot_dim1)).getWindowToken(), 0);
                                    imm.hideSoftInputFromWindow(((EditText) mView.findViewById(R.id.pot_dim2)).getWindowToken(), 0);
                                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


                                    createFragment(SeedBankFragment.class);

                                }
                            });
                } else {
                    alertDialogBuilderUserInput
                            .setCancelable(false)
                            .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogBox, int id) {
                                    if(System.currentTimeMillis() - timeSinceClosing < 100) {
                                        showingDialog = false;
                                        return;
                                    }
                                    if(n > 0) {
                                        showingDialog = false;
                                        return;
                                    }

                                    n = 1;
                                    if(creatingPot) {
                                        showingDialog = false;
                                        return;
                                    }
                                    String d1, d2;

                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    if(potType.isChecked()) {
                                        d1 = d2 = ((EditText) mView.findViewById(R.id.pot_dim)).getText().toString();

                                        imm.hideSoftInputFromWindow(((EditText) mView.findViewById(R.id.pot_dim)).getWindowToken(), 0);

                                    } else {

                                        d1 = ((EditText) mView.findViewById(R.id.pot_dim1)).getText().toString();
                                        d2 = ((EditText) mView.findViewById(R.id.pot_dim2)).getText().toString();
                                        imm.hideSoftInputFromWindow(((EditText) mView.findViewById(R.id.pot_dim1)).getWindowToken(), 0);
                                        imm.hideSoftInputFromWindow(((EditText) mView.findViewById(R.id.pot_dim2)).getWindowToken(), 0);

                                    }
                                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

                                    int result = ((GardenFragment) currentFragment).addPot(seeds.get(seedType.getSelectedItemPosition()), potType.isChecked() ? Pot.CIRCLE : Pot.RECT, d1, d2);

                                    if(result == 1) {
                                        showingDialog = false;
                                        creatingPot = true;
                                        findViewById(R.id.toolbar_plus).setVisibility(View.GONE);
                                        findViewById(R.id.toolbar_title).setVisibility(View.GONE);

                                        findViewById(R.id.toolbar_tick).setVisibility(View.VISIBLE);
                                        findViewById(R.id.toolbar_x).setVisibility(View.VISIBLE);
                                        if(!potType.isChecked() && !d1.equals(d2))
                                            findViewById(R.id.toolbar_rotate).setVisibility(View.VISIBLE);

                                        setDrawerState(false);
                                        settings.edit().putBoolean("popup_Remember", check.isChecked()).apply();
                                        if(check.isChecked()) {
                                            settings.edit().putBoolean("popup_Type", potType.isChecked()).apply();
                                            settings.edit().putInt("popup_seedPos", seedType.getSelectedItemPosition()).apply();
                                            settings.edit().putString("popup_d1", d1).apply();
                                            settings.edit().putString("popup_d2", d2).apply();
                                        }
                                    } else {
                                        showingDialog = false;
                                    }

                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogBox, int id) {
                            dialogBox.cancel();
                            showingDialog = false;
                            setDrawerState(true);
                            n = 0;
                        }
                    });
                }

                AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
                alertDialogAndroid.show();
                showingDialog = true;
                return false;
            }
        });
        setDrawerState(true);

        for(int i = 0; i < GardenView.SEED_BANKS; i++) {
            seedBanks.add(new SeedBank(i, "Empty", Color.parseColor("#11333344")));
        }
        debugging();

    }

    private void debugging() {
        debugging = true;
//        seedBanks = new ArrayList<>();
//        for(int i = 0; i < GardenView.SEED_BANKS; i++) {
//            seedBanks.add(new SeedBank(i, "Empty", Color.parseColor("#11333344")));
//        }
//        setDrawerState(true);
//
//        SEED_TYPES = new ArrayList<>();
//        SEED_TYPES.add("Empty");
//        SEED_TYPES.add("Mint");
//        SEED_TYPES.add("Potato");
//
//        WIDTH = 750;
//        HEIGHT = 750;
//        firstRun = true;
    }

    private int counter = 0;

    public void startTutorial() {

//        if (MainActivity.firstRun) {
//            Target homeTarget = new Target() {
//                @Override
//                public Point getPoint() {
//                    Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
//                    Point size = new Point();
//                    display.getSize(size);
//
//                    return new Point(GardenView.offset / 2, (size.y - ((MainActivity) getActivity()).mToolbar.getHeight() - 20) / 2);
//                }
//            };
//            new ShowcaseView.Builder(getActivity())
//                    .setTarget(new ActionViewTarget(getActivity(), ActionViewTarget.Type.HOME))
//                    .setContentTitle("Seed Bank")
//                    .setContentText("Tap on the right of the screen to go back.")
//                    .hideOnTouchOutside()
//                    .build();
//        }
        switch(counter) {
            case 0:
//                showcaseView = new ShowcaseView.Builder(this)
//                        .setTarget(new ViewTarget(findViewById(R.id.textView)))
//                        .setOnClickListener(this)
//                        .build();

        }
    }

    DrawerMenuItem.DrawerCallBack menuCallback = new DrawerMenuItem.DrawerCallBack() {
        @Override
        public void onGardenMenuSelected() {
            createFragment(GardenFragment.class);
        }

        @Override
        public void onSeedBankMenuSelected() {
            createFragment(SeedBankFragment.class);
        }
    };

    public void createFragment(Class type) {
        if(type == currentFragmentType) return;
        currentFragmentType = type;

        Fragment fragment = null;
        try {
            fragment = (Fragment) type.newInstance();
        } catch (Exception e) { //api version issues
            e.printStackTrace();
        }
        currentFragment = fragment;

        bluetoothController.currentFragment = fragment;
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, fragment).commit();

        if(type.equals(GardenFragment.class)) {
            mToolbarTitle.setText("Garden");
        } else if(type.equals(SeedBankFragment.class)) {
            mToolbarTitle.setText("Seed Bank");
        } else if(type.equals(SetupFragment.class)) {
            mToolbarTitle.setText("Setup");
        }


        mDrawer.closeDrawers();
    }

    private void setupDrawer() {
        mDrawerView.addView(new DrawerMenuItem(this.getApplicationContext(), DrawerMenuItem.DRAWER_MENU_ITEM_GARDEN, menuCallback))
                .addView(new DrawerMenuItem(this.getApplicationContext(), DrawerMenuItem.DRAWER_MENU_ITEM_SEED_BANK, menuCallback));


        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, mDrawer, mToolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };

        mDrawer.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

    }

    Drawable navIconCache;

    public void setDrawerState(boolean isEnabled) {
        if(isEnabled) {
            mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            drawerToggle.onDrawerStateChanged(DrawerLayout.LOCK_MODE_UNLOCKED);
            drawerToggle.setDrawerIndicatorEnabled(true);
            mToolbar.setNavigationIcon(navIconCache);
            mToolbar.invalidate();
            drawerToggle.syncState();
        } else {

            mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            drawerToggle.onDrawerStateChanged(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            drawerToggle.setDrawerIndicatorEnabled(false);
            drawerToggle.syncState();
            if(mToolbar.getNavigationIcon() != null)
                navIconCache = mToolbar.getNavigationIcon();
            mToolbar.setNavigationIcon(null);
        }
    }


}
