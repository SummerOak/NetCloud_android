package com.summer.netcloud.window;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputFilter;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.summer.netcloud.R;
import com.summer.netcloud.utils.ResTools;
import com.summer.netcloud.utils.ScreenUtils;

/**
 * Created by summer on 05/09/2018.
 */

public class HostInputDialog {

    private AlertDialog mHostInputDialog;
    private Context mContext;
    private IDialogCallback mCallback;
    private String mOriginContent;

    public HostInputDialog(Context context, IDialogCallback callback){
        mContext = context;
        mCallback = callback;
    }

    public void show(){
        show(0);
    }

    public void setOriginText(String origin){
        mOriginContent = origin;
    }

    public void show(int inputType){
        if(mHostInputDialog != null){
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        LinearLayout cnt = new LinearLayout(mContext);
        cnt.setBackgroundResource(R.drawable.list_item_bg);
        cnt.setOrientation(LinearLayout.VERTICAL);
        cnt.setGravity(Gravity.CENTER_HORIZONTAL);
        int vp = ScreenUtils.dp2px(12);
        int hp = (int) ResTools.getDimen(R.dimen.hor_padding);
        cnt.setPadding(hp, vp, hp, vp);

        final EditText editText = new EditText(mContext);
        editText.setBackgroundColor(ResTools.getColor(R.color.background));
        editText.setMaxLines(1);
        editText.setSingleLine();
        if(mOriginContent != null){
            editText.setText(mOriginContent);
        }
        editText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(60)});
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);

        if(inputType > 0){
            editText.setInputType(inputType);
        }

        cnt.addView(editText);

        TextView ok = new TextView(mContext);
        ok.setText("Add");
        ok.setGravity(Gravity.CENTER);
        ok.setBackgroundResource(R.drawable.button_blue);
        ok.setTextColor(ResTools.getColor(R.color.blue));
        ok.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)ResTools.getDimen(R.dimen.textsize1));
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String host = editText.getText().toString();
                mCallback.onHostInput(host);
            }
        });

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ScreenUtils.dp2px(80), ScreenUtils.dp2px(32));
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        lp.topMargin = ScreenUtils.dp2px(16);
        cnt.addView(ok, lp);

        builder.setView(cnt);

        mHostInputDialog = builder.create();
        mHostInputDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mHostInputDialog = null;
                mCallback.onDismiss();
            }
        });

        mHostInputDialog.show();
        editText.requestFocus();
    }

    public void dismiss(){
        if(mHostInputDialog != null && mHostInputDialog.isShowing()){
            mHostInputDialog.dismiss();
        }
    }

    public interface IDialogCallback{
        void onHostInput(String value);
        void onDismiss();
    }

}
