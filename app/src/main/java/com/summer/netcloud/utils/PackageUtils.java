package com.summer.netcloud.utils;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;

import com.summer.netcloud.Constants;
import com.summer.netcloud.ContextMgr;
import com.summer.netcloud.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by summer on 14/06/2018.
 */

public class PackageUtils {
    private static final String TAG = Constants.TAG + ".PackageUtils";

    private static SparseArray<AppInfo> sUID2AppInfo = new SparseArray<>();

    public static List<AppInfo> getAllInstallApps(){
        PackageManager pm = ContextMgr.getApplicationContext().getPackageManager();
        List<ApplicationInfo> appsInfo = pm.getInstalledApplications(0);
        List<AppInfo> appInfos = new ArrayList<>();
        if(appsInfo != null){
            for(ApplicationInfo i:appsInfo){
                AppInfo appInfo = new AppInfo();
                appInfo.uid = i.uid;
                appInfo.icon = i.loadIcon(pm);
                appInfo.pkg = i.packageName;
                appInfo.name = i.loadLabel(pm).toString();
                appInfos.add(appInfo);
            }
        }
        return appInfos;
    }

    public static String getPackageName(int uid){
        PackageManager pm = ContextMgr.getApplicationContext().getPackageManager();
        String[] pkgs = pm.getPackagesForUid(uid);

        if(pkgs != null){
            for(String pkg:pkgs){
                Log.d(TAG,"pkg: " + pkg);
            }

            return pkgs[0];
        }

        return "system";
    }

    public static AppInfo getAppInfo(int uid){
        AppInfo ret = getInfo(uid);
        if(ret != null){
            return ret;
        }

        PackageManager pm = ContextMgr.getApplicationContext().getPackageManager();
        String pkg = getPackageName(uid);
        try{
            PackageInfo info = pm.getPackageInfo(pkg,0);
            if(info != null){
                AppInfo appInfo = new AppInfo();
                appInfo.uid = uid;
                appInfo.icon = info.applicationInfo.loadIcon(pm);
                appInfo.pkg = info.packageName;
                appInfo.name = info.applicationInfo.loadLabel(pm).toString();
                addInfo(appInfo);
                return appInfo;
            }
        }catch (Throwable t){
            ExceptionHandler.handleException(t);
        }

        return null;
    }

    public static int getDefaultAppIcon(int uid){
        return R.drawable.icon144;
    }

    public static String getDefaultAppName(int uid){
        return "system" + String.valueOf(uid);
    }

    private static AppInfo getInfo(int uid){
        synchronized (sUID2AppInfo){
            return sUID2AppInfo.get(uid);
        }
    }


    private static void addInfo(AppInfo info){
        synchronized (sUID2AppInfo){
            sUID2AppInfo.put(info.uid,info);
        }
    }


    public static class AppInfo{
        public int uid;
        public String pkg;
        public String name;
        public Drawable icon;
    }

}
