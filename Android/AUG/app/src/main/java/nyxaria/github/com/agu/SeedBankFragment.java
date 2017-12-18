package nyxaria.github.com.agu;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.github.amlcurran.showcaseview.targets.Target;

import net.steamcrafted.materialiconlib.MaterialIconView;

import static nyxaria.github.com.agu.R.id.toolbar;

public class SeedBankFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_seed_bank, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MaterialIconView) ((MainActivity) getActivity()).findViewById(R.id.toolbar_plus)).setVisibility(View.GONE);
        ((MainActivity) getContext()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    }
}
