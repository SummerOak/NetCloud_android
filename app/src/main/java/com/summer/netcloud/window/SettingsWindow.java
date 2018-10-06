package com.summer.netcloud.window;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.summer.netcloud.utils.SystemUtils;
import com.summer.netcore.Config;
import com.summer.netcore.IPUtils;
import com.summer.netcore.VpnConfig;
import com.summer.netcloud.ContextMgr;
import com.summer.netcloud.R;
import com.summer.netcloud.message.Messege;
import com.summer.netcloud.message.MsgDispatcher;
import com.summer.netcloud.utils.ResTools;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by summer on 04/09/2018.
 */

public class SettingsWindow extends AbsListContentWindow<Integer,SettingsWindow.ItemView> implements View.OnClickListener{

    private static final int SETTING_APPS_UNDER_CTRL = 1;
    private static final int SETTING_DNS_SERVER = 2;
    private static final int SETTING_PROXY_ADDR = 3;
    private static final int SETTING_CRASH_RECORDS = 4;
    private static final int SETTING_CAPTURE_DIR = 5;
    private static final int SETTING_ABOUT = 6;

    private static HostInputDialog mHostInputDialog;
    private static HostAndPortInputDialog mHostAndPortDialog;

    private TitleBar mTitleBar;
    private List<Integer> mSettings = new ArrayList<>();


    public SettingsWindow(Context context) {
        super(context);

        mSettings.add(SETTING_APPS_UNDER_CTRL);
        mSettings.add(SETTING_DNS_SERVER);
        mSettings.add(SETTING_PROXY_ADDR);
        mSettings.add(SETTING_CAPTURE_DIR);
        mSettings.add(SETTING_CRASH_RECORDS);
        mSettings.add(SETTING_ABOUT);

        updateData(mSettings);
    }

    @Override
    protected View getTitleBar() {
        if(mTitleBar == null){
            mTitleBar = new TitleBar(getContext());
            mTitleBar.setTitle("Settings");
        }
        return mTitleBar;
    }

    @Override
    protected int getItemId(Integer item) {
        return item;
    }

    @Override
    protected ItemView createItemView(int position) {
        ItemView view = new ItemView(getContext());
        view.setOnClickListener(this);
        return view;
    }

    @Override
    protected void bindItem(Integer item, ItemView view) {
        switch (item){
            case SETTING_APPS_UNDER_CTRL:{
                view.setTitle("Control Strategy", null);
                break;
            }
            case SETTING_DNS_SERVER:{
                String dnsAddr = VpnConfig.getConfig(Config.DNS_SERVER, "");
                if(VpnConfig.isValidateHost(dnsAddr)){
                    view.setTitle("DNS", dnsAddr);
                    break;
                }

                view.setTitle("DNS", "not set");

                break;
            }
            case SETTING_PROXY_ADDR:{
                String proxyAddr = VpnConfig.getConfig(Config.PROXY_ADDR, "");
                String proxyPort = VpnConfig.getConfig(Config.PROXY_PORT, "");
                if(VpnConfig.isValidateHost(proxyAddr)){
                    try{
                        int port = Integer.valueOf(proxyPort);
                        if(0<=port && port<=65536){
                            view.setTitle("Proxy server", proxyAddr+"/"+proxyPort);
                            break;
                        }
                    }catch (Throwable t){
                        t.printStackTrace();
                    }
                }
                view.setTitle("Proxy server", "not set");

                break;
            }
            case SETTING_CAPTURE_DIR:{
                String capDir = VpnConfig.getConfig(Config.CAP_OUTPUT_DIR, "not set");
                view.setTitle("Capture output directory", capDir);

                break;
            }
            case SETTING_CRASH_RECORDS:{
                view.setTitle("Crash records", null);
                break;
            }
            case SETTING_ABOUT:{
                view.setTitle("About ", null);
                break;
            }
        }

        view.setTag(item);
    }

    private void showDnsConfigDialog(){
        if(mHostInputDialog != null){
            return;
        }

        mHostInputDialog = new HostInputDialog(getContext(), new HostInputDialog.IDialogCallback() {
            @Override
            public void onHostInput(String value) {
                if(value == null || value.equals("") || IPUtils.isIpv4Address(value) || IPUtils.isIpv6Address(value)){
                    VpnConfig.setConfig(Config.DNS_SERVER, value==null?"":value);
                    update();
                    mHostInputDialog.dismiss();
                    return;
                }
                Toast.makeText(getContext(),"invalidate input.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDismiss() {
                mHostInputDialog = null;
            }
        });

        mHostInputDialog.setOriginText(VpnConfig.getConfig(Config.DNS_SERVER,null));
        mHostInputDialog.show(EditorInfo.TYPE_CLASS_PHONE);
    }

    private void showProxyConfigDialog(){
        if(mHostAndPortDialog != null){
            return;
        }

        mHostAndPortDialog = new HostAndPortInputDialog(getContext(), new HostAndPortInputDialog.IDialogCallback() {
            @Override
            public void onHostInput(String value, String port) {
                int ipver = 0;
                if(IPUtils.isIpv4Address(value)) {
                    ipver = 4;
                }else if(IPUtils.isIpv6Address(value)){
                    ipver = 6;
                }

                if(ipver == 4 || ipver == 6){
                    try{
                        int nPort = Integer.valueOf(port);
                        if(0<nPort&&nPort<65536){
                            VpnConfig.setConfig(Config.PROXY_IPVER, String.valueOf(ipver));
                            VpnConfig.setConfig(Config.PROXY_ADDR, value);
                            VpnConfig.setConfig(Config.PROXY_PORT, port);

                            update();
                            mHostAndPortDialog.dismiss();
                            return;
                        }
                    }catch (Throwable t){
                        t.printStackTrace();
                    }

                }

                Toast.makeText(getContext(),"invalidate input.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDismiss() {
                mHostAndPortDialog = null;
            }
        });

        mHostAndPortDialog.setOriginText(
                VpnConfig.getConfig(Config.PROXY_ADDR, null),
                VpnConfig.getConfig(Config.PROXY_PORT, null));
        mHostAndPortDialog.show();
    }

    @Override
    public void onClick(View v) {
        if(v.getTag() instanceof Integer){
            int settingID = (int)v.getTag();
            switch (settingID){
                case SETTING_APPS_UNDER_CTRL:{
                    MsgDispatcher.get().dispatchSync(Messege.PUSH_WINDOW,
                            TrafficCtrlWindow.createWindow(getContext(), VpnConfig.CTRL_BITS.BASE));
                    break;
                }
                case SETTING_DNS_SERVER:{
                    showDnsConfigDialog();
                    break;
                }
                case SETTING_PROXY_ADDR:{
                    showProxyConfigDialog();
                    break;
                }
                case SETTING_CRASH_RECORDS:{
                    MsgDispatcher.get().dispatchSync(Messege.PUSH_WINDOW, new CrashRecordWindow(ContextMgr.getContext()));
                    break;
                }case SETTING_ABOUT:{
                    Toast.makeText(getContext(), "NetCloud " + SystemUtils.getLocalVersionName(getContext()), Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        }
    }

    public static class ItemView extends LinearLayout{

        private TextView mTitle;
        private TextView mSubtitle;

        public ItemView(Context context) {
            super(context);

            setOrientation(LinearLayout.HORIZONTAL);
            setGravity(Gravity.CENTER_VERTICAL);

            LinearLayout cnt = new LinearLayout(context);
            cnt.setOrientation(LinearLayout.VERTICAL);
            cnt.setBackgroundColor(Color.TRANSPARENT);

            mTitle = new TextView(context);
            mTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) ResTools.getDimen(R.dimen.textsize2));
            cnt.addView(mTitle);

            mSubtitle = new TextView(context);
            mSubtitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)ResTools.getDimen(R.dimen.textsize3));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.topMargin = 10;
            mSubtitle.setLayoutParams(lp);
            cnt.addView(mSubtitle);

            this.addView(cnt, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            this.setBackgroundResource(R.drawable.list_item_bg);
            this.setMinimumHeight((int)ResTools.getDimen(R.dimen.item_height));
            int hp = (int)ResTools.getDimen(R.dimen.hor_padding);
            int vp = (int)ResTools.getDimen(R.dimen.vtl_padding);
            this.setPadding(hp,vp,hp,vp);

        }

        public void setTitle(String title,String subtitle){
            mTitle.setText(title);
            if(subtitle == null || subtitle.equals("")){
                mSubtitle.setVisibility(GONE);
            }else {
                mSubtitle.setText(subtitle);
            }
        }


    }

}
