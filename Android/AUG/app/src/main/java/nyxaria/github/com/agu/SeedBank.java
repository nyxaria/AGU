package nyxaria.github.com.agu;

import android.util.Log;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by georgehartt on 06/11/2017.
 */

class SeedBank {

    protected int index;
    public String seed;
    public int color;
    public int alpha;

    public SeedBank(int index, String seed, int color) {
        this.index = index;
        this.seed = seed;
        this.color = color;

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                alpha++;

                if(alpha>254) {
                    alpha = 255;
                    cancel();
                }
            }
        };
        new Timer().schedule(task, 0,5);
    }

    @Override
    public String toString() {
        return "index=" + index +
                ",seed=" + seed  +
                ",color=" + color;
    }

    public static void load(String d, ArrayList<SeedBank> seedBanks) {
        int i = 0, c = 0;
        String s = "Empty";
        for (String fields : d.split(",")) {

            String[] data = fields.split("=");
            if(data[0].startsWith("b\"")) {
                data[0] = data[0].substring(2);
            }
            if(data[1].startsWith("b'") && data[1].length() > 2) {
                data[1] = data[1].substring(2, data[1].length()-1);
            } else if(data[1].startsWith("b'")){
                data[1] = data[1].substring(2, data[1].length());

            }
            switch (data[0]) {
                case "index":
                    i = Integer.parseInt(data[1]);
                    break;
                case "seed":
                    s = data[1];
                    break;
                case "color":
                    data[1] = data[1].replace("\"", "").replace("'", "");
                    if(!data[1].equals(""))
                        c = Integer.parseInt(data[1]);
                    break;
            }
        }
        Log.d("seed", i + ", " + s + ", " + c);
        seedBanks.set(i, new SeedBank(i, s, c));


    }
}
