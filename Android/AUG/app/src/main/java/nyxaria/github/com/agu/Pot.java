package nyxaria.github.com.agu;

import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by georgehartt on 23/10/2017.
 */

class Pot {
    public static final int CIRCLE = 0;
    public static final int RECT = 1;

    int x, y, xDim, yDim, type;
    String seed;
    public boolean clicked;
    ArrayList<Integer> archive = new ArrayList<>();

    public boolean scaled = false;

    int water = 0; // percentage
    public int frequency;
    public int threshold;
    public int active;

    int alpha = 0;

    @Override
    public String toString() {
        return "x=" + x +
                ",y=" + y +
                ",type=" + type +
                ",xDim=" + (int) (xDim/(GardenView.xRatio != 0 ? GardenView.xRatio : 1) + 0.5f) +
                ",yDim=" + (int) (yDim/(GardenView.yRatio != 0 ? GardenView.yRatio : 1) + 0.5f) +
                ",seed=" + seed +
                ",water=" + water +
                ",frequency=" + frequency +
                ",threshold=" + threshold +
                ",active="+ active +
                ",archive="+parseArchive();
    }


    public String parseArchive() {
        if(archive == null) return "";
        if(archive.size() == 0)
            return "";
        String out = "";
        for(Integer i : archive) {
            out += i + ",";
        }
        return out.substring(0, out.length()-1);
    }


    public Pot(int x, int y, int type, int dim, int dim2, String seed, boolean scale) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.xDim = dim;
        this.yDim = dim2;
        this.seed = seed;
        frequency = 1;
        water = 0;
        active = 0;
        threshold = MainActivity.DEFAULT_THRESHOLD;
        this.scaled = scale;
        if(scaled) { // added manually
            alpha = 255;
        }

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                alpha++;

                if(alpha>252) {
                    alpha = 255;
                    cancel();
                }
            }
        };
        new Timer().schedule(task, 0,5);

    }

    public static void load(String d, ArrayList<Pot> pots) {
        d = d.substring(2, d.length());
        int x = 0, y = 0, xDim = 0, yDim = 0, type = 0, water = 0, frequency = 0, active = 1, threshold = 0;
        String seed = null;
        ArrayList<Integer> archive = null;
        for (String fields : d.split(",")) {
            String[] data = fields.split("=");
            if(data.length < 2) continue;
            if(data[1].startsWith("b'")) {
                data[1] = data[1].substring(2, data[1].length()-1);
            }
            if(data[1].length() == 0) continue;
            switch (data[0]) {
                case "x":
                    x = Integer.parseInt(data[1]);
                    break;
                case "y":
                    y = Integer.parseInt(data[1]);
                    break;
                case "xDim":
                    xDim = (Integer.parseInt(data[1]));
                    break;
                case "yDim":
                    yDim = (Integer.parseInt(data[1]));
                    break;
                case "type":
                    type = Integer.parseInt(data[1]);
                    break;
                case "water":
                    water = Integer.parseInt(data[1]);
                    break;
                case "seed":
                    seed = data[1];
                    break;
                case "frequency":
                    frequency = Integer.parseInt(data[1]);
                    break;
                case "active":
                     active = Integer.parseInt(data[1]);
                    break;
                case "archive":
                    if(data[1].length() > 0)
                    for(String s : data[1].split("\\+")) {
                        s = s.replace("\"", "");
                        if(s.length() > 0)
                            archive.add(Integer.parseInt(s));
                    }
                    break;
                case "threshold":
                    threshold = Integer.parseInt(data[1]);
            }
        }
        Pot pot = new Pot(x,y,type,xDim,yDim,seed,false);
        pot.water = water;
        pot.active = active;
        pot.frequency = frequency;
        pot.threshold = threshold;
        pot.archive = archive;

        pots.add(pot);
        Log.d("pot", pot.toString());
    }
}
