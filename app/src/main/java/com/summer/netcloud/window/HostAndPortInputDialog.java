package com.summer.netcloud.window;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.InputFilter;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
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

public class HostAndPortInputDialog {

    private AlertDialog mHostInputDialog;
    private Context mContext;
    private IDialogCallback mCallback;

    private String mOriginAddr;
    private String mOriginPort;

    public HostAndPortInputDialog(Context context, IDialogCallback callback){
        mContext = context;
        mCallback = callback;
    }

    public void setOriginText(String text1, String text2){
        mOriginAddr = text1;
        mOriginPort = text2;
    }

    public void show(){
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

        LinearLayout editCnt = new LinearLayout(mContext);
        editCnt.setBackgroundColor(Color.TRANSPARENT);
        editCnt.setOrientation(LinearLayout.HORIZONTAL);

        TextView host = new TextView(mContext);
        host.setTextColor(ResTools.getColor(R.color.text));
        host.setTextSize(TypedValue.COMPLEX_UNIT_PX, ResTools.getDimen(R.dimen.textsize1));
        host.setText(R.string.host);
        editCnt.addView(host);

        final EditText editText = new EditText(mContext);
        editText.setBackgroundColor(ResTools.getColor(R.color.background));
        editText.setMaxLines(1);
        editText.setSingleLine();
        if(mOriginAddr != null){
            editText.setText(mOriginAddr);
        }
        editText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(60)});
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        editText.setInputType(EditorInfo.TYPE_CLASS_PHONE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.weight = 2;
        lp.leftMargin = (int)ResTools.getDimen(R.dimen.hor_padding);
        editCnt.addView(editText,lp);

        TextView port = new TextView(mContext);
        port.setTextColor(ResTools.getColor(R.color.text));
        port.setTextSize(TypedValue.COMPLEX_UNIT_PX, ResTools.getDimen(R.dimen.textsize1));
        port.setText(R.string.port);
        lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = (int)ResTools.getDimen(R.dimen.hor_padding);
        editCnt.addView(port, lp);

        final EditText editText2 = new EditText(mContext);
        editText2.setBackgroundColor(ResTools.getColor(R.color.background));
        editText2.setMaxLines(1);
        editText2.setSingleLine();
        if(mOriginPort != null){
            editText2.setText(mOriginPort);
        }
        editText2.setFilters(new InputFilter[] {new InputFilter.LengthFilter(5)});
        editText2.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.width = ScreenUtils.dp2px(64);
        lp.leftMargin = (int)ResTools.getDimen(R.dimen.hor_padding);
        editCnt.addView(editText2, lp);

        cnt.addView(editCnt);

        TextView ok = new TextView(mContext);
        ok.setText(R.string.done);
        ok.setGravity(Gravity.CENTER);
        ok.setBackgroundResource(R.drawable.button_blue);
        ok.setTextColor(ResTools.getColor(R.color.blue));
        ok.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)ResTools.getDimen(R.dimen.textsize1));
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String host = editText.getText().toString();
                String port = editText2.getText().toString();
                mCallback.onHostInput(host, port);
            }
        });

        lp = new LinearLayout.LayoutParams(ScreenUtils.dp2px(80), ScreenUtils.dp2px(32));
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
        void onHostInput(String value,String port);
        void onDismiss();
    }

}
