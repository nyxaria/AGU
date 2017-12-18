package nyxaria.github.com.agu;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Spinner;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static android.R.attr.offset;
import static nyxaria.github.com.agu.GardenView.SEED_BANKS;

/**
 * Created by georgehartt on 18/10/2017.
 */

public class SeedBankView extends View implements View.OnTouchListener, AdapterView.OnItemSelectedListener {
    private int margin = 10;

    int yOff = 20;
    int xOff = 37 + GardenView.SEED_BANKS * 5 - (GardenView.SEED_BANKS <= 4 ? 15 : 0);

    int xShift;
    private int yGap = 0;

    private int curY, curX;
    private int startX, startY; //for clicks
    private SeedBank currentSeedBank;
    private int alpha = 0;
    private ScheduledExecutorService scheduleTaskExecutor;

    public SeedBankView(Context context) {
        super(context);
        init();
    }

    public SeedBankView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public SeedBankView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
        init();
    }

    public void init() {
        setOnTouchListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        scheduleTaskExecutor = Executors.newScheduledThreadPool(1);
    }


    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Path path = new Path();


    @Override
    protected void onDraw(Canvas canvas) {
        paint.setAntiAlias(true);

        if(yGap == 0) {
            yGap = Math.abs(xOff * 2 - margin * 2 - ((getHeight() - yOff * 2) / GardenView.SEED_BANKS));
            xShift = getWidth() / 5;
        }


        path.reset();
        path.moveTo(xShift + getWidth() / 2, 0);
        path.lineTo(xShift + getWidth() / 2, getHeight());
        path.lineTo(xShift + getWidth(), getHeight());
        path.lineTo(xShift + getWidth(), 0);
        paint.setColor(Color.parseColor("#11333344"));

        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(path, paint);
        paint.setColor(Color.parseColor("#222233"));
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, paint);


        paint.setColor(Color.parseColor("#9999aa"));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(xShift + getWidth() / 2 - xOff, yOff - 2, xShift + getWidth() / 2 + xOff, getHeight() - yOff + 4, paint);
        paint.setColor(Color.parseColor("#222233"));
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(xShift + getWidth() / 2 - xOff, yOff - 2, xShift + getWidth() / 2 + xOff, getHeight() - yOff + 4, paint);

        path.reset();
        path.moveTo(getWidth() * 7 / 8, 0);
        path.lineTo(getWidth(), getHeight() / (SEED_BANKS * 2));
        path.lineTo(getWidth() + 1, 0);
        paint.setColor(Color.parseColor("#22333344"));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(path, paint);
        paint.setColor(Color.parseColor("#222233"));
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, paint);


        for(int i = 0; i < SEED_BANKS; i++) {
            //paint.setColor(Color.parseColor("#112233"));

            if(((MainActivity) getContext()).seedBanks != null) {
                paint.setColor(((MainActivity) getContext()).seedBanks.get(i).color);
            } else {
                paint.setColor(Color.parseColor("#11333344"));
            }
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(xShift + getWidth() / 2 - xOff + margin, yOff + 4 + (((getHeight() - yOff * 2) - 12 / 2) * i / SEED_BANKS) + yGap, xShift + getWidth() / 2 + xOff - margin, yOff + 4 + (((getHeight() - yOff * 2) - 12 / 2) * (1 + i) / SEED_BANKS) - yGap, paint);
            paint.setColor(Color.parseColor("#222233"));
            paint.setStyle(Paint.Style.STROKE);
//            canvas.drawRect(xOff + margin, yOff + 4 + (((getHeight() - yOff * 2) - 12 / 2) * i / SEED_BANKS) + yGap + 1, getWidth() - xOff - margin, yOff + 4 + (((getHeight() - yOff * 2) - 12) * (i + 1) / SEED_BANKS) - yGap + 1, paint);
            canvas.drawRect(xShift + getWidth() / 2 - xOff + margin, yOff + 4 + (((getHeight() - yOff * 2) - 12 / 2) * i / SEED_BANKS) + yGap, xShift + getWidth() / 2 + xOff - margin, yOff + 4 + (((getHeight() - yOff * 2) - 12 / 2) * (1 + i) / SEED_BANKS) - yGap, paint);
//            canvas.drawRect(20*i, 20*i, 20*(i+1), 20*(i+1), paint);
        }

        if(pw != null) {
            if(pw.isShowing()) {
                paint.setColor(Color.parseColor("#000000"));
                paint.setAlpha(alpha);

                paint.setStyle(Paint.Style.FILL);
                canvas.drawRect(0,0,getWidth(),getHeight(),paint);

                if(currentSeedBank != null) {
                    paint.setColor(currentSeedBank.color);
                } else {
                    paint.setColor(Color.parseColor("#66aaaabb"));
                }
                paint.setStyle(Paint.Style.FILL);
                canvas.drawRect(xShift + getWidth() / 2 - xOff + margin, yOff + 4 + (((getHeight() - yOff * 2) - 12 / 2) * currentSeedBank.index / SEED_BANKS) + yGap, xShift + getWidth() / 2 + xOff - margin, yOff + 4 + (((getHeight() - yOff * 2) - 12 / 2) * (1 + currentSeedBank.index) / SEED_BANKS) - yGap, paint);
                paint.setColor(Color.parseColor("#222233"));
                paint.setStyle(Paint.Style.STROKE);
//            canvas.drawRect(xOff + margin, yOff + 4 + (((getHeight() - yOff * 2) - 12 / 2) * i / SEED_BANKS) + yGap + 1, getWidth() - xOff - margin, yOff + 4 + (((getHeight() - yOff * 2) - 12) * (i + 1) / SEED_BANKS) - yGap + 1, paint);
                canvas.drawRect(xShift + getWidth() / 2 - xOff + margin, yOff + 4 + (((getHeight() - yOff * 2) - 12 / 2) * currentSeedBank.index / SEED_BANKS) + yGap, xShift + getWidth() / 2 + xOff - margin, yOff + 4 + (((getHeight() - yOff * 2) - 12 / 2) * (1 + currentSeedBank.index) / SEED_BANKS) - yGap, paint);
//            canvas.drawRect(20*i, 20*i, 20*(i+1), 20*(i+1), paint);

            }
        }

        //768 1280
        //paint.setColor(getResources().getColor(R.color.drawerBackground));
        //canvas.drawRect(getLeft() + 5,getTop() + 5 ,getRight() - 5,getBottom() - 5,paint);
    }

    public boolean onTouch(View v, MotionEvent event) {
        curX = (int) event.getX();
        curY = (int) event.getY();
        invalidate();

        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            startX = (int) event.getX();
            startY = (int) event.getY();

            Log.d("dd", "" + ((MainActivity) getContext()).seedBanks + "," + ((MainActivity) getContext()).pots);
            for(SeedBank seedBank : ((MainActivity) getContext()).seedBanks) {
                int y = yOff + 4 + (((getHeight() - yOff * 2) - 12 / 2) * seedBank.index / SEED_BANKS);
                int height = (((getHeight() - yOff * 2) - 12 / 2) / SEED_BANKS);

                if(curX > xShift + getWidth() / 2 - xOff && curX < xShift + getWidth() / 2 + xOff && curY > y && curY < y + height) {
                    showPopup(seedBank, y, height);
                }

            }
        }

        if(event.getAction() == MotionEvent.ACTION_UP) {
            int endX = (int) event.getX();
            int endY = (int) event.getY();
            int dX = Math.abs(endX - startX);
            int dY = Math.abs(endY - startY);


            if(Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2)) <= ViewConfiguration.get(getContext()).getScaledTouchSlop()) {
                if(curX > getWidth() / 2 + xOff + xShift + 20 && curX < getWidth()) {
                    ((MainActivity) getContext()).createFragment(GardenFragment.class);
                }
            }

        }

        return true;

    }

    private PopupWindow pw;
    Button colorPicker;
    private Spinner spinner;
    boolean showingPicker = false;
    int popHeight = 280;
    Runnable updater;
    long timeSinceLast;
    private void showPopup(final SeedBank seedBank, int y, int height) {
        try {
            if(pw != null) {
                if(pw.isShowing()) {
                    pw.dismiss();
                } else {
                    //alpha = 0;
                }
            } else {
                alpha = 0;
            }

            if(System.currentTimeMillis() - timeSinceLast > 100) {
                alpha = 0;
            }

            final Handler timerHandler = new Handler();

            updater = new Runnable() {
                @Override
                public void run() {
                    if(alpha < 255/3) {
                        invalidate();
                        alpha+=2 ;
                        timerHandler.postDelayed(updater, 1);
                        //Log.d("d", alpha+"");
                    } else {
                        //updater = null;
                    }
                }
            };
            timerHandler.post(updater);


            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.seed_popup, null);
            pw = new PopupWindow(layout, getWidth() - (getWidth() / 2 - xOff), popHeight, true);
            pw.setFocusable(false);
            pw.setOutsideTouchable(true);
            pw.showAtLocation(layout, Gravity.NO_GRAVITY, 20, y + height / 2 + ((MainActivity) getContext()).mToolbar.getHeight() < getHeight() - popHeight / 4 + 4 ? y + height / 2 + ((MainActivity) getContext()).mToolbar.getHeight() : getHeight() - popHeight / 4 + 4);
            //close = (Button) layout.findViewById(R.id.close_popup);
            //close.setOnClickListener(cancel_button);

            pw.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    timeSinceLast = System.currentTimeMillis();
                }
            });
            spinner = (Spinner) layout.findViewById(R.id.seed_spinner);
            ArrayList<String> seeds = new ArrayList<>();
            if(!seedBank.seed.equals("Empty"))
                seeds.add(seedBank.seed);
            for(String s : MainActivity.SEED_TYPES) {
                if(((MainActivity) getContext()).getSeedBank(s) == -1) {
                    seeds.add(s);
                }
            }


            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                    android.R.layout.simple_spinner_item, seeds.toArray(new String[0]));


            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            currentSeedBank = seedBank;
            spinner.setOnItemSelectedListener(this);
            spinner.setSelection(seeds.indexOf(seedBank.seed));

            final int prevColor = seedBank.color;

            colorPicker = (Button) layout.findViewById(R.id.seed_color);

            if(seedBank.seed.equals("Empty")) {
                colorPicker.setEnabled(false);
            }

            colorPicker.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(showingPicker) return false;
                    showingPicker = true;
                    ColorPickerDialogBuilder
                            .with(getContext())
                            .initialColor(Color.parseColor("#ffffff"))
                            .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                            .density(12)
                            .setOnColorSelectedListener(new OnColorSelectedListener() {
                                @Override
                                public void onColorSelected(int selectedColor) {
                                    seedBank.color = selectedColor;
                                    invalidate();
                                }
                            })
                            .setPositiveButton("ok", new ColorPickerClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                    seedBank.color = selectedColor;
                                    invalidate();
                                    showingPicker = false;
                                }
                            })
                            .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    seedBank.color = prevColor;
                                    showingPicker = false;
                                }
                            })
                            .build()
                            .show();
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        currentSeedBank.seed = (String) spinner.getSelectedItem();
        if(colorPicker != null)
        if(currentSeedBank.seed.equals("Empty")) {
            colorPicker.setEnabled(false);
        } else {
            colorPicker.setEnabled(true);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }


}


