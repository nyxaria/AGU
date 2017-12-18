package nyxaria.github.com.agu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import org.eazegraph.lib.charts.ValueLineChart;
import org.eazegraph.lib.models.ValueLinePoint;
import org.eazegraph.lib.models.ValueLineSeries;

/**
 * Created by georgehartt on 18/10/2017.
 */

public class GardenView extends View implements View.OnTouchListener {

    public static int SEED_BANKS = 10;

    static int offset = 40;
    private int curY, curX;
    private int startX, startY; //for clicks

    MainActivity main;
    private int seedBankHeight = 40;
    private int yGap = 5;
    private boolean rotated;
    private String tempSeed;
    private int tempType, tempxDim, tempyDim, origTempxDim, origTempyDim, tempX, tempY;
    public static float yRatio, xRatio;
    public int creatingX = -1, creatingY = -1;
    private boolean updated;
    private Bitmap bitmap;
    private String selectZoneColor = "#dddde0";
    private String highlightedColor = "#62727b";
    public int alpha;
    private Pot selectedPot;
    public boolean transitioning = true;


    public GardenView(Context context) {
        super(context);
        init();
    }

    public GardenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public GardenView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void init() {
        setOnTouchListener(this);
        main = (MainActivity) getContext();
        this.setDrawingCacheEnabled(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
    }


    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    Path path = new Path();

    int yOff;

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setAntiAlias(true);
        paint.setStrokeWidth(1);
        if(yOff == 0) {
            yOff = getHeight() / 2 - getWidth() / 2 + offset;

        }


        //draw bed

        if(main.creatingPot)
            paint.setColor(Color.parseColor("#15252c"));
        else
            paint.setColor(Color.parseColor("#44444466"));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(offset, yOff, getWidth() - offset, yOff + getWidth() - offset*2, paint);
        paint.setColor(Color.parseColor("#222233"));
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(offset, yOff, getWidth() - offset, yOff + getWidth() - offset*2, paint);


        paint.setColor(Color.parseColor(selectZoneColor));
        paint.setStyle(Paint.Style.FILL);
        if(main.creatingPot) {
            tempxDim = origTempxDim;
            tempyDim = origTempyDim;

            if(tempType == Pot.RECT) {
                tempyDim /= 2;
                tempxDim /= 2;
            }

            tempxDim = (int) ((float) tempxDim * xRatio);
            tempyDim = (int) ((float) tempyDim * yRatio);


            canvas.drawRect(offset, yOff, offset + tempxDim * 2, yOff + getWidth() - offset * 2, paint);
            canvas.drawRect(getWidth() - offset - tempxDim * 2 + 1, yOff, getWidth() - offset, yOff + getWidth() - offset * 2, paint);
            canvas.drawRect(offset, yOff, getWidth() - offset - tempxDim * 1, yOff + tempyDim * 2, paint);
            canvas.drawRect(offset, yOff + getWidth() - offset * 2 - tempyDim * 2 + 1, getWidth() - offset, yOff + getWidth() - offset * 2, paint);

            for(Pot pots : main.pots) {
                if(pots.type == Pot.CIRCLE) {
                    pots.x -= pots.xDim;
                    pots.y -= pots.yDim;
                    pots.xDim *= 2;
                    pots.yDim *= 2;
                }

                if(pots.x > tempxDim * 2 && pots.y > tempyDim * 2 && pots.y + pots.yDim < getWidth() - offset * 2 - tempyDim * 2) { //left side
                    canvas.drawRect(offset + pots.x - tempxDim * 2, yOff + pots.y + 1 - (tempyDim * 2 >= pots.yDim ? tempyDim * 2 - pots.yDim : 0) / 2, offset + pots.x + pots.xDim, yOff + pots.y + pots.yDim + (tempyDim * 2 >= pots.yDim ? tempyDim * 2 - pots.yDim : 0) / 2, paint);

                }
                if(pots.x + pots.xDim < getWidth() - offset * 2 - tempxDim * 2 && pots.y > tempyDim * 2 && pots.y + pots.yDim < getWidth() - offset * 2 - tempyDim * 2) { //right
                    canvas.drawRect(offset + pots.x + pots.xDim, yOff + pots.y - (tempyDim * 2 >= pots.yDim ? tempyDim * 2 - pots.yDim : 0) / 2, offset + pots.x + pots.xDim + tempxDim * 2, yOff + pots.y + pots.yDim + (tempyDim * 2 >= pots.yDim ? tempyDim * 2 - pots.yDim : 0) / 2, paint);

                }
                if(pots.y >= 0 && pots.x >= tempxDim * 2 && pots.x + pots.xDim <= getWidth() - offset) { //top

                    canvas.drawRect(offset + pots.x + 1- (tempxDim * 2 > pots.xDim ? tempxDim * 2 - pots.xDim : 0) / 2, yOff + pots.y - tempyDim * 2 + 1, offset + pots.x + pots.xDim + (tempxDim * 2 > pots.xDim ? tempxDim * 2 - pots.xDim / 2 : 0) / 2, yOff + pots.y, paint);
                }

                if(pots.y + pots.yDim <= (getWidth() - offset * 2) * yRatio && pots.x >= tempxDim * 2 && pots.x + pots.xDim <= (getWidth() - offset * 2) * yRatio) { //bottom
                    canvas.drawRect(offset + pots.x + 1- (tempxDim * 2 > pots.xDim ? tempxDim * 2 - pots.xDim : 0) / 2, yOff + pots.y + pots.yDim, offset + pots.x + pots.xDim + (tempxDim * 2 > pots.xDim ? tempxDim * 2 - pots.xDim / 2 : 0) / 2, yOff + pots.y + pots.yDim + tempyDim * 2 -1, paint);
                }
                if(pots.type == Pot.CIRCLE) {
                    pots.xDim /= 2;
                    pots.yDim /= 2;
                    pots.x += pots.xDim;
                    pots.y += pots.yDim;
                }
            }
        }

        if(main.pots.size() != 0)
            transitioning = false;
        for(Pot pot : main.pots) {
            if(MainActivity.HEIGHT != 0) {
                if(!pot.scaled) {
                    pot.xDim = (int) ((float) pot.xDim * GardenView.xRatio);
                    pot.yDim = (int) ((float) pot.yDim * GardenView.yRatio);
                    pot.scaled = true;
                }
                if(pot.alpha != 255) {
                    transitioning = true;
                    paint.setAlpha(pot.alpha);
                }
                int index = main.getSeedBank(pot.seed);
                if(((MainActivity) getContext()).seedBanks != null && index != -1) {
                    paint.setColor(main.seedBanks.get(index).color);
                } else {
                    paint.setColor(Color.parseColor("#11333344"));
                }
                paint.setStyle(Paint.Style.FILL);

                if(pot.type == Pot.CIRCLE) {
                    if(main.creatingPot) {
                        paint.setColor(Color.parseColor("#ffcc0000"));
                        if(pot.alpha != 255)
                            paint.setAlpha(pot.alpha);
                        paint.setStyle(Paint.Style.FILL);
                        canvas.drawRect(offset + pot.x - pot.xDim, yOff + pot.y - pot.xDim, offset + pot.x + pot.xDim, yOff + pot.y + pot.yDim, paint);
                        if(((MainActivity) getContext()).seedBanks != null && index != -1) {
                            paint.setColor(main.seedBanks.get(index).color);
                        } else {
                            paint.setColor(Color.parseColor("#11333344"));
                        }
                    }
                    if(pot.alpha != 255)
                        paint.setAlpha(pot.alpha);
                    canvas.drawCircle(offset + pot.x, yOff + pot.y, pot.xDim, paint);
                    paint.setColor(Color.parseColor("#111111"));
                    if(pot.alpha != 255)
                        paint.setAlpha(pot.alpha);
                    paint.setStyle(Paint.Style.STROKE);
                    canvas.drawCircle(offset + pot.x, yOff + pot.y, pot.xDim, paint);
                } else if(pot.type == Pot.RECT) {
                    if(main.creatingPot) {
                        paint.setColor(Color.parseColor("#ffcc0000"));
                    }
                    if(pot.alpha != 255)
                        paint.setAlpha(pot.alpha);
                    canvas.drawRect(offset + pot.x + 1, yOff + pot.y + 1, offset + pot.x + pot.xDim - 1, yOff + pot.y + pot.yDim - 1, paint);
                    paint.setColor(Color.parseColor("#111111"));
                    paint.setStyle(Paint.Style.STROKE);
                    if(pot.alpha != 255)
                        paint.setAlpha(pot.alpha);
                    canvas.drawRect(offset + pot.x + 1, yOff + pot.y + 1, offset + pot.x + pot.xDim - 1, yOff + pot.y + pot.yDim - 1, paint);
                }
            }
        }

        paint.setAlpha(255);
        if(creatingX != -1 && main.creatingPot) {
            int index = main.getSeedBank(tempSeed);
            if(((MainActivity) getContext()).seedBanks != null && index != -1) {
                paint.setColor(main.seedBanks.get(index).color);
            } else {
                paint.setColor(Color.parseColor("#11333344"));
            }
            paint.setStyle(Paint.Style.FILL);
            tempxDim = (int) (origTempxDim * xRatio);
            tempyDim = (int) (origTempyDim * yRatio);
            if(tempType == Pot.CIRCLE) {
                paint.setColor(Color.parseColor(highlightedColor));
                paint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(creatingX, creatingY, tempxDim, paint);
            } else if(tempType == Pot.RECT) {
                paint.setColor(Color.parseColor(highlightedColor));
                paint.setStyle(Paint.Style.FILL);
                canvas.drawRect(creatingX - tempxDim / 2, creatingY - tempyDim / 2, creatingX + tempxDim / 2, creatingY + tempyDim / 2, paint);
            }
        }

        path.reset();
        path.moveTo(offset, yOff);
        path.lineTo(offset - offset / 2, yOff - offset);
        path.lineTo(getWidth() - offset / 2, yOff - offset);
        path.lineTo(getWidth() - offset, yOff);

        paint.setColor(Color.parseColor("#05333344"));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(path, paint);
        paint.setColor(Color.parseColor("#222233"));
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, paint);

        path.reset();
        path.moveTo(getWidth() - offset, yOff);
        path.lineTo(getWidth() - offset / 2, yOff - offset);
        path.lineTo(getWidth() - offset / 2, yOff + getWidth() - offset);
        path.lineTo(getWidth() - offset, yOff + getWidth() - offset * 2);

        paint.setColor(Color.parseColor("#11333344"));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(path, paint);
        paint.setColor(Color.parseColor("#222233"));
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, paint);

        path.reset();
        path.moveTo(getWidth() - offset, yOff + getWidth() - offset * 2);
        path.lineTo(offset, yOff + getWidth() - offset * 2);
        path.lineTo(offset / 2, yOff + getWidth() - offset);
        path.lineTo(getWidth() - offset / 2, yOff + getWidth() - offset);

        paint.setColor(Color.parseColor("#22333344"));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(path, paint);
        paint.setColor(Color.parseColor("#222233"));
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, paint);

        path.reset();
        path.moveTo(offset, yOff);
        path.lineTo(offset / 2, yOff - offset);
        path.lineTo(offset / 2, yOff + getWidth() - offset);
        path.lineTo(offset, yOff + getWidth() - offset * 2);

        paint.setColor(Color.parseColor("#11333344"));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(path, paint);
        paint.setColor(Color.parseColor("#222233"));
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, paint);

        //seedbank base
        if(!main.creatingPot)
            paint.setColor(Color.parseColor("#9999aa"));
        else
            paint.setColor(Color.parseColor("#ccccbb"));

        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, yOff + offset / 4 - yGap / 2, offset, yOff + (seedBankHeight * (SEED_BANKS)) + offset / 4 + yGap / 2, paint);
        paint.setColor(Color.parseColor("#222233"));
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(0, yOff + offset / 4 - yGap / 2, offset, yOff + (seedBankHeight * (SEED_BANKS)) + offset / 4 + yGap / 2, paint);

        for(int i = 0; i < SEED_BANKS; i++) {
            paint.setAlpha(255);
            if(((MainActivity) getContext()).seedBanks != null) {
                paint.setColor(main.seedBanks.get(i).color);
                if(main.seedBanks.get(i).alpha != 255 && !main.seedBanks.get(i).seed.equals("Empty"))
                    paint.setAlpha(((MainActivity) getContext()).seedBanks.get(i).alpha);
            } else {
                paint.setColor(Color.parseColor("#11333344"));
            }
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(5, yOff + offset / 4 + (seedBankHeight) * i + yGap, offset - 5, yOff + offset / 4 + (seedBankHeight) * (i + 1) - yGap, paint);
            paint.setColor(Color.parseColor("#222233"));
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(5, yOff + offset / 4 + (seedBankHeight) * i + yGap, offset - 5, yOff + offset / 4 + (seedBankHeight) * (i + 1) - yGap, paint);
        }


        if(pw != null) {
            if(pw.isShowing()) {
                paint.setColor(Color.parseColor("#000000"));
                paint.setAlpha(alpha);

                paint.setStyle(Paint.Style.FILL);
                canvas.drawRect(0,0,getWidth(),getHeight(),paint);
                paint.setAlpha(255);
                if(selectedPot != null) {
                    int index = main.getSeedBank(selectedPot.seed);
                    if(((MainActivity) getContext()).seedBanks != null && index != -1) {
                        paint.setColor(main.seedBanks.get(index).color);
                    } else {
                        paint.setColor(Color.parseColor("#66aaaabb"));
                    }
                    paint.setStyle(Paint.Style.FILL);

                    if(selectedPot.type == Pot.CIRCLE) {
                        canvas.drawCircle(offset + selectedPot.x, yOff + selectedPot.y, selectedPot.xDim, paint);
                        paint.setColor(Color.parseColor("#111111"));
                        paint.setStyle(Paint.Style.STROKE);
                        canvas.drawCircle(offset + selectedPot.x, yOff + selectedPot.y, selectedPot.xDim, paint);
                    } else if(selectedPot.type == Pot.RECT) {
                        canvas.drawRect(offset + selectedPot.x + 1, yOff + selectedPot.y + 1, offset + selectedPot.x + selectedPot.xDim - 1, yOff + selectedPot.y + selectedPot.yDim - 1, paint);
                        paint.setColor(Color.parseColor("#111111"));
                        paint.setStyle(Paint.Style.STROKE);
                        canvas.drawRect(offset + selectedPot.x + 1, yOff + selectedPot.y + 1, offset + selectedPot.x + selectedPot.xDim - 1, yOff + selectedPot.y + selectedPot.yDim - 1, paint);
                    }
                }
            }
        }

        //canvas.drawCircle(curX, curY, 5, paint);
        //canvas.drawText(width + ", " + height, 100, 100, paint);
    }

    public static Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if(bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return returnedBitmap;
    }

    public void addPot(Pot pot) {
        Log.d("d", main.pots.size()+"");
        main.pots.add(pot);
        main.save();
    }

    public void removePot(Pot pot) {
        main.lastRemoved = pot;
        main.pots.remove(pot);
        main.save();
    }

    public void undoRemove() {
        main.pots.add(main.lastRemoved);
        main.save();
    }
    boolean active = false;

    public boolean onTouch(View v, MotionEvent event) {
        curX = (int) event.getX();
        curY = (int) event.getY();

        if(main.pots.size() != 0 && transitioning) return true;

        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            startX = (int) event.getX();
            startY = (int) event.getY();
            if(!main.creatingPot) {
                active = false;
                for(Pot pot : main.pots) {
                    if(pot.type == Pot.CIRCLE) {
                        if(Math.sqrt(Math.pow(offset + pot.x - curX, 2) + Math.pow(yOff + pot.y - curY, 2)) < pot.xDim) {
                            pot.clicked = true;
                            showPopup(pot);
                            active = true;
                        }
                    } else if(pot.type == Pot.RECT) {
                        if(curX > pot.x + offset && curX < offset + pot.x + pot.xDim && curY > pot.y + yOff && curY < pot.y + pot.yDim + yOff) {
                            showPopup(pot);
                            active = true;
                        }
                    }
                }
                if(!active) {
                    alpha = 0;
                    invalidate();
                }
            }
        }


        if(event.getAction() == MotionEvent.ACTION_UP) {
            int endX = (int) event.getX();
            int endY = (int) event.getY();
            int dX = Math.abs(endX - startX);
            int dY = Math.abs(endY - startY);


            if(Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2)) <= ViewConfiguration.get(getContext()).getScaledTouchSlop()) {
                if(main.creatingPot) {
                    if(curX > 0 && curY > 0)
                    if(Color.parseColor(selectZoneColor) == (bitmap = getBitmapFromView(this)).getPixel(curX, curY)) {
                        int i = 0;
                        int top = -1, left = -1;
                        while(i++ <= (tempType == Pot.CIRCLE ? origTempxDim * xRatio : Math.max(origTempxDim * xRatio, origTempyDim * yRatio) / 2)) {
                            if(curX + i < getWidth() && left == -1)
                                if(bitmap.getPixel(curX + i, curY) != Color.parseColor(selectZoneColor) && bitmap.getPixel(curX + i, curY) != Color.parseColor(highlightedColor)) { //right
                                    left = 1+curX + i - (int) (tempType == Pot.CIRCLE ? origTempxDim * xRatio * 2 : origTempxDim * xRatio);
                                }

                            if(curX - i > 0 && left == -1)
                                if(bitmap.getPixel(curX - i, curY) != Color.parseColor(selectZoneColor) && bitmap.getPixel(curX - i, curY) != Color.parseColor(highlightedColor)) { //left
                                    left = 1 + curX - i;
                                }

                            if(curY + i < getHeight() && top == -1)
                                if(bitmap.getPixel(curX, curY + i) != Color.parseColor(selectZoneColor) && bitmap.getPixel(curX, curY + i) != Color.parseColor(highlightedColor)) { //bottom
                                    top = 1 + curY + i - (int) (tempType == Pot.CIRCLE ? origTempxDim * xRatio * 2 : origTempyDim * yRatio);
                                }

                            if(curY - i > 0 && top == -1)
                                if(bitmap.getPixel(curX, curY - i) != Color.parseColor(selectZoneColor) && bitmap.getPixel(curX, curY - i) != Color.parseColor(highlightedColor)) { //top
                                    top = 1 + curY - i;
                                }
                        }
                        creatingX = (left == -1 ? curX : left + (int) (tempType == Pot.CIRCLE ? origTempxDim * xRatio : Math.max(origTempxDim * xRatio, origTempyDim * yRatio) / 2));
                        creatingY = (top == -1 ? curY : top + (int) (tempType == Pot.CIRCLE ? origTempxDim * xRatio : Math.max(origTempxDim * xRatio, origTempyDim * yRatio) / 2));
                        invalidate();
                    }

                    return true;
                }

                if(curX > 0 && curX < offset + 5 && curY > yOff + offset / 4 - yGap / 2 - 5 && curY < yOff + (seedBankHeight * (SEED_BANKS)) + offset / 4 + yGap / 2 + 5) {
                    main.createFragment(SeedBankFragment.class);
                }

                invalidate();
            }

        }
        //showPopup(null);

        return true;

    }


    private PopupWindow pw;
    Button close;

    int popWidth = 200, popHeight = 400;
    Runnable updater;
    long timeSinceLast;
    private void showPopup(Pot pot) {
        try {
            if(pw != null) {
                if(pw.isShowing()) {
                    pw.dismiss();
                    ((MainActivity) getContext()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

                }
            }
            final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View layout = inflater.inflate(R.layout.pot_popup, null);

            if(System.currentTimeMillis() - timeSinceLast > 100) {
                alpha = 0;
            } else {
                alpha = 255/3;
            }

            selectedPot = pot;

            //populate

            ((TextView) layout.findViewById(R.id.popup_title)).setText(selectedPot.seed);

            ((TextView) layout.findViewById(R.id.popup_waterField)).setText("72" + "%");

            //((TextView) layout.findViewById(R.id.popup_waterField)).setText(selectedPot.water + "%");
            if(selectedPot.water >= selectedPot.threshold) {
                ((TextView) layout.findViewById(R.id.popup_waterField)).setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
            } else {
                ((TextView) layout.findViewById(R.id.popup_waterField)).setTextColor(getResources().getColor(android.R.color.holo_orange_light));
            }
            ((TextView) layout.findViewById(R.id.popup_waterField)).setTextColor(getResources().getColor(android.R.color.holo_blue_dark));

            if(selectedPot.active == 1) {
                ((TextView) layout.findViewById(R.id.popup_dateField)).setText("1 day");
            } else {
                ((TextView) layout.findViewById(R.id.popup_dateField)).setText(selectedPot.active + " days");
            }
            ((TextView) layout.findViewById(R.id.popup_dateField)).setText("11 days");

            final EditText freqField = layout.findViewById(R.id.popup_frequencyField);
            freqField.setText("2");
            if(selectedPot.frequency == 1) {
                ((TextView) layout.findViewById(R.id.popup_frequencyUnit)).setText("day");
            } else {
                ((TextView) layout.findViewById(R.id.popup_frequencyUnit)).setText("days");
            }
            ((TextView) layout.findViewById(R.id.popup_frequencyUnit)).setText("days");

            freqField.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if(s.length() > 0)
                    if(Integer.parseInt(s.toString()) != 0){
                        freqField.setTextColor(getResources().getColor(R.color.textSecondary));
                        if(Integer.parseInt(s.toString()) == 1) {
                            ((TextView) layout.findViewById(R.id.popup_frequencyUnit)).setText("day");
                        } else {
                            ((TextView) layout.findViewById(R.id.popup_frequencyUnit)).setText("days");
                        }
                        selectedPot.frequency = Integer.parseInt(s.toString());
                    } else {
                        freqField.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    }
                }
            });


            pw = new PopupWindow(layout, getWidth() / 2 + 50, getHeight() / 5 + 100, true);
            //pw.setFocusable(false);
            pw.setOutsideTouchable(true);
            final Handler timerHandler = new Handler();


            //TODO populate popup window, create graph data in api.
            updater = new Runnable() {
                @Override
                public void run() {
                    if(alpha < 255/3) {
                        invalidate();
                        alpha+=2;
                        timerHandler.postDelayed(updater, 1);
                    } else {
                        if(System.currentTimeMillis() - timeSinceLast > 100 && alpha != 0) {
                            alpha = 0;
                        }
                    }
                }
            };
            timerHandler.post(updater);
            pw.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    alpha = 0;
                    timeSinceLast = System.currentTimeMillis();
                    ((MainActivity) getContext()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                    invalidate();
                }
            });
            pw.showAtLocation(layout, Gravity.NO_GRAVITY, offset + pot.x + pot.xDim/2 - pw.getWidth()/2 < getWidth() - offset - 10 ? offset + pot.x + pot.xDim/2 - pw.getWidth()/2: getWidth() - pw.getWidth() - offset - 10,
                    yOff + pot.y + 30 + pw.getHeight() < yOff + getWidth()-offset*2 ? yOff + pot.y + 30 + pw.getHeight() : pot.y + pot.yDim);
            //close = (Button) layout.findViewById(R.id.close_popup);
            //close.setOnClickListener(cancel_button);
            pw.setFocusable(true);
            pw.update();
            final ValueLineChart mCubicValueLineChart = (ValueLineChart) layout.findViewById(R.id.cubiclinechart);

            ValueLineSeries series = new ValueLineSeries();
            mCubicValueLineChart.setShowIndicator(false);
            series.setColor(0xFF56B7F1);

//            int i = 0;
//            for(Integer val : pot.archive) {
//                series.addPoint(new ValueLinePoint(i++ + "", val / 100f));
//            }
            series.addPoint(new ValueLinePoint("0", 0f));
            series.addPoint(new ValueLinePoint("1", 1f));
            series.addPoint(new ValueLinePoint("2", .9f));
            series.addPoint(new ValueLinePoint("3", 1.1f));
            series.addPoint(new ValueLinePoint("4", 1.1f));
            series.addPoint(new ValueLinePoint("5", 0.8f));
            series.addPoint(new ValueLinePoint("6", 1f));
            series.addPoint(new ValueLinePoint("7", 1f));
            series.addPoint(new ValueLinePoint("8", 1.1f));
            series.addPoint(new ValueLinePoint("9", 1f));
            series.addPoint(new ValueLinePoint("10", 0.9f));
            series.addPoint(new ValueLinePoint("11", 1f));

            mCubicValueLineChart.addSeries(series);
            mCubicValueLineChart.startAnimation();
            final boolean[] open = {false};
            layout.findViewById(R.id.popup_collapse).setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    open[0] = !open[0];
                    if(open[0]) {
                        ((MaterialIconView) layout.findViewById(R.id.popup_collapse)).setIcon(MaterialDrawableBuilder.IconValue.ARROW_COLLAPSE_LEFT);
                        mCubicValueLineChart.setVisibility(VISIBLE);
                        //pw.setHeight(pw.getHeight());
                        layout.findViewById(R.id.popup_dateField).setVisibility(GONE);
                        layout.findViewById(R.id.popup_dateLabel).setVisibility(GONE);

                        layout.findViewById(R.id.popup_waterField).setVisibility(GONE);
                        layout.findViewById(R.id.popup_waterLabel).setVisibility(GONE);


                        layout.findViewById(R.id.popup_frequencyField).setVisibility(GONE);
                        layout.findViewById(R.id.popup_frequencyLabel).setVisibility(GONE);
                        layout.findViewById(R.id.popup_frequencyUnit).setVisibility(GONE);

                        pw.update(pw.getWidth(), pw.getHeight()+300);
                    } else {
                        ((MaterialIconView) layout.findViewById(R.id.popup_collapse)).setIcon(MaterialDrawableBuilder.IconValue.ARROW_COLLAPSE_RIGHT);
                        mCubicValueLineChart.setVisibility(GONE);

                        layout.findViewById(R.id.popup_dateField).setVisibility(VISIBLE);
                        layout.findViewById(R.id.popup_dateLabel).setVisibility(VISIBLE);

                        layout.findViewById(R.id.popup_waterField).setVisibility(VISIBLE);
                        layout.findViewById(R.id.popup_waterLabel).setVisibility(VISIBLE);

                        layout.findViewById(R.id.popup_frequencyField).setVisibility(VISIBLE);
                        layout.findViewById(R.id.popup_frequencyLabel).setVisibility(VISIBLE);
                        layout.findViewById(R.id.popup_frequencyUnit).setVisibility(VISIBLE);

                        pw.update(pw.getWidth(), pw.getHeight()-300);

                    }
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*private void showPopup(Pot pot) {
        try {
            if(pw != null) {
                if(pw.isShowing()) {
                    pw.dismiss();
                    ((MainActivity) getContext()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

                }
            }
            final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View layout = inflater.inflate(R.layout.pot_popup, null);

            if(System.currentTimeMillis() - timeSinceLast > 100) {
                alpha = 0;
            } else {
                alpha = 255/3;
            }

            selectedPot = pot;

            //populate

            ((TextView) layout.findViewById(R.id.popup_title)).setText(selectedPot.seed);

            ((TextView) layout.findViewById(R.id.popup_waterField)).setText("72" + "%");

            //((TextView) layout.findViewById(R.id.popup_waterField)).setText(selectedPot.water + "%");
            if(selectedPot.water >= selectedPot.threshold) {
                ((TextView) layout.findViewById(R.id.popup_waterField)).setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
            } else {
                ((TextView) layout.findViewById(R.id.popup_waterField)).setTextColor(getResources().getColor(android.R.color.holo_orange_light));
            }
            ((TextView) layout.findViewById(R.id.popup_waterField)).setTextColor(getResources().getColor(android.R.color.holo_blue_dark));

            if(selectedPot.active == 1) {
                ((TextView) layout.findViewById(R.id.popup_dateField)).setText("1 day");
            } else {
                ((TextView) layout.findViewById(R.id.popup_dateField)).setText(selectedPot.active + " days");
            }
            ((TextView) layout.findViewById(R.id.popup_dateField)).setText("11 days");

            final EditText freqField = layout.findViewById(R.id.popup_frequencyField);
            freqField.setText(selectedPot.frequency+"");
            if(selectedPot.frequency == 1) {
                ((TextView) layout.findViewById(R.id.popup_frequencyUnit)).setText("day");
            } else {
                ((TextView) layout.findViewById(R.id.popup_frequencyUnit)).setText("days");
            }

            freqField.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if(s.length() > 0)
                    if(Integer.parseInt(s.toString()) != 0){
                        freqField.setTextColor(getResources().getColor(R.color.textSecondary));
                        if(Integer.parseInt(s.toString()) == 1) {
                            ((TextView) layout.findViewById(R.id.popup_frequencyUnit)).setText("day");
                        } else {
                            ((TextView) layout.findViewById(R.id.popup_frequencyUnit)).setText("days");
                        }
                        selectedPot.frequency = Integer.parseInt(s.toString());
                    } else {
                        freqField.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    }
                }
            });


            pw = new PopupWindow(layout, getWidth() / 2 + 50, getHeight() / 5 + 100, true);
            //pw.setFocusable(false);
            pw.setOutsideTouchable(true);
            final Handler timerHandler = new Handler();


            //TODO populate popup window, create graph data in api.
            updater = new Runnable() {
                @Override
                public void run() {
                    if(alpha < 255/3) {
                        invalidate();
                        alpha+=2;
                        timerHandler.postDelayed(updater, 1);
                    } else {
                        if(System.currentTimeMillis() - timeSinceLast > 100 && alpha != 0) {
                            alpha = 0;
                        }
                    }
                }
            };
            timerHandler.post(updater);
            pw.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    alpha = 0;
                    timeSinceLast = System.currentTimeMillis();
                    ((MainActivity) getContext()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                    invalidate();
                }
            });
            pw.showAtLocation(layout, Gravity.NO_GRAVITY, offset + pot.x + pot.xDim/2 - pw.getWidth()/2 < getWidth() - offset - 10 ? offset + pot.x + pot.xDim/2 - pw.getWidth()/2: getWidth() - pw.getWidth() - offset - 10,
                    yOff + pot.y + 30 + pw.getHeight() < yOff + getWidth()-offset*2 ? yOff + pot.y + 30 + pw.getHeight() : pot.y + pot.yDim);
            //close = (Button) layout.findViewById(R.id.close_popup);
            //close.setOnClickListener(cancel_button);
            pw.setFocusable(true);
            pw.update();
            final ValueLineChart mCubicValueLineChart = (ValueLineChart) layout.findViewById(R.id.cubiclinechart);

            ValueLineSeries series = new ValueLineSeries();
            mCubicValueLineChart.setShowIndicator(false);
            series.setColor(0xFF56B7F1);

//            int i = 0;
//            for(Integer val : pot.archive) {
//                series.addPoint(new ValueLinePoint(i++ + "", val / 100f));
//            }
            series.addPoint(new ValueLinePoint("0", 0f));
            series.addPoint(new ValueLinePoint("1", 1f));
            series.addPoint(new ValueLinePoint("2", .9f));
            series.addPoint(new ValueLinePoint("3", 1.1f));
            series.addPoint(new ValueLinePoint("4", 1.1f));
            series.addPoint(new ValueLinePoint("5", 0.9f));
            series.addPoint(new ValueLinePoint("6", 1f));
            series.addPoint(new ValueLinePoint("7", 1f));
            series.addPoint(new ValueLinePoint("8", 1.1f));
            series.addPoint(new ValueLinePoint("9", 1f));
            series.addPoint(new ValueLinePoint("10", 0.9f));
            series.addPoint(new ValueLinePoint("11", 1f));

            mCubicValueLineChart.addSeries(series);
            mCubicValueLineChart.startAnimation();
            final boolean[] open = {false};
            layout.findViewById(R.id.popup_collapse).setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    open[0] = !open[0];
                    if(open[0]) {
                        ((MaterialIconView) layout.findViewById(R.id.popup_collapse)).setIcon(MaterialDrawableBuilder.IconValue.ARROW_COLLAPSE_LEFT);
                        mCubicValueLineChart.setVisibility(VISIBLE);
                        //pw.setHeight(pw.getHeight());
                        layout.findViewById(R.id.popup_dateField).setVisibility(GONE);
                        layout.findViewById(R.id.popup_dateLabel).setVisibility(GONE);

                        layout.findViewById(R.id.popup_waterField).setVisibility(GONE);
                        layout.findViewById(R.id.popup_waterLabel).setVisibility(GONE);


                        layout.findViewById(R.id.popup_frequencyField).setVisibility(GONE);
                        layout.findViewById(R.id.popup_frequencyLabel).setVisibility(GONE);
                        layout.findViewById(R.id.popup_frequencyUnit).setVisibility(GONE);

                        pw.update(pw.getWidth(), pw.getHeight()+300);
                    } else {
                        ((MaterialIconView) layout.findViewById(R.id.popup_collapse)).setIcon(MaterialDrawableBuilder.IconValue.ARROW_COLLAPSE_RIGHT);
                        mCubicValueLineChart.setVisibility(GONE);

                        layout.findViewById(R.id.popup_dateField).setVisibility(VISIBLE);
                        layout.findViewById(R.id.popup_dateLabel).setVisibility(VISIBLE);

                        layout.findViewById(R.id.popup_waterField).setVisibility(VISIBLE);
                        layout.findViewById(R.id.popup_waterLabel).setVisibility(VISIBLE);

                        layout.findViewById(R.id.popup_frequencyField).setVisibility(VISIBLE);
                        layout.findViewById(R.id.popup_frequencyLabel).setVisibility(VISIBLE);
                        layout.findViewById(R.id.popup_frequencyUnit).setVisibility(VISIBLE);

                        pw.update(pw.getWidth(), pw.getHeight()-300);

                    }
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
*/
    public void rotate() {
        int temp = origTempxDim;
        origTempxDim = origTempyDim;
        origTempyDim = temp;

        rotated = !rotated;
    }

    public void preparePot(String seed, int type, String d1, String d2) {
        tempSeed = seed;
        tempType = type;
        origTempxDim = Integer.parseInt(d1);
        origTempyDim = Integer.parseInt(d2);

    }

    public void finalise() {
        if(creatingX == -1) return;

        if(tempType == Pot.RECT) {
            creatingX -= origTempxDim * xRatio / 2;
            creatingY -= origTempyDim * yRatio / 2;
        }


//        creatingX= (int) ((float) creatingX * xRatio);
//        creatingY = (int) ((float) creatingY * yRatio);
        origTempxDim = (int) ((float) origTempxDim * xRatio);
        origTempyDim = (int) ((float) origTempyDim * xRatio);

        if(creatingX - offset < 0) creatingX = offset;
        if(creatingY - yOff < 0) creatingY = yOff;
        if(creatingX - offset + origTempxDim > getWidth() - offset) creatingX = getWidth() - origTempxDim;
        if(creatingY - yOff + origTempyDim > getWidth() - offset) creatingY = yOff + getWidth() - offset - origTempyDim;

        addPot(new Pot(creatingX - offset, creatingY - yOff, tempType, origTempxDim, origTempyDim, tempSeed, true));

    }


}
