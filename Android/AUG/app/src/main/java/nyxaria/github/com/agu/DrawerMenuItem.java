package nyxaria.github.com.agu;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.mindorks.placeholderview.annotations.Click;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;

@Layout(R.layout.drawer_item)
public class DrawerMenuItem {

    public static final int DRAWER_MENU_ITEM_GARDEN = 1;
    public static final int DRAWER_MENU_ITEM_SEED_BANK = 2;

    private int mMenuPosition;
    private Context mContext;
    private DrawerCallBack mCallBack;

    @View(R.id.itemNameTxt)
    private TextView itemNameTxt;

    @View(R.id.itemIcon)
    private ImageView itemIcon;

    public DrawerMenuItem(Context context, int menuPosition) {
        mContext = context;
        mMenuPosition = menuPosition;
    }

    public DrawerMenuItem(Context context, int menuPosition, DrawerCallBack callBack) {
        mContext = context;
        mMenuPosition = menuPosition;
        mCallBack = callBack;
    }

    @Resolve
    private void onResolved() {
        switch (mMenuPosition){
            case DRAWER_MENU_ITEM_GARDEN:
                itemNameTxt.setText("Garden");
                //itemIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_account_circle_black_18dp));
                break;
            case DRAWER_MENU_ITEM_SEED_BANK:
                itemNameTxt.setText("Seed Bank");
                //itemIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_compare_arrows_black_18dp));
                break;
        }
    }

    @Click(R.id.mainView)
    private void onMenuItemClick(){
        switch (mMenuPosition){

            case DRAWER_MENU_ITEM_GARDEN:
                if(mCallBack != null)mCallBack.onGardenMenuSelected();
                break;
            case DRAWER_MENU_ITEM_SEED_BANK:
                if(mCallBack != null)mCallBack.onSeedBankMenuSelected();
                break;
        }
    }

    public void setDrawerCallBack(DrawerCallBack callBack) {
        mCallBack = callBack;
    }

    public interface DrawerCallBack{
        void onGardenMenuSelected();
        void onSeedBankMenuSelected();
    }
}