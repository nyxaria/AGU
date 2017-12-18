package nyxaria.github.com.agu;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import net.steamcrafted.materialiconlib.MaterialIconView;

public class GardenFragment extends Fragment {
    GardenView gardenView;
    public boolean transitioning = true;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(
                R.layout.fragment_garden, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        gardenView = getView().findViewById(R.id.gardenView);
        ((MainActivity) getContext()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        ((MaterialIconView) ((MainActivity) getActivity()).findViewById(R.id.toolbar_plus)).setVisibility(View.VISIBLE);
    }

    public int addPot(String seed, int type,  String dim1, String dim2) {
        if(dim1.equals("") || dim2.equals("")) return 0;
        gardenView.preparePot(seed, type, dim1, dim2);
        gardenView.invalidate();
        return 1;
    }

    public void rotate() {
        gardenView.rotate();
    }

    public void cancelAddingPot() {
        gardenView.creatingX = gardenView.creatingY = -1;
        gardenView.invalidate();
    }

    public void finalise() {
        gardenView.finalise();
        gardenView.creatingX = gardenView.creatingY = -1;
    }
}