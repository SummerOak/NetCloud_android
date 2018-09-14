package com.summer.netcloud.utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by summer on 15/06/2018.
 */

public class Listener<T> {

    private List<WeakReference<T>> mL = new ArrayList<>();

    public void add(T l){
        synchronized (mL){

            boolean exist = false;
            List<WeakReference<T>> del = new ArrayList<>();
            for(WeakReference<T> wrf:mL){
                T ll = wrf.get();
                if(ll == null){
                    del.add(wrf);
                }else if(ll == l){
                    exist = true;
                }
            }

            mL.removeAll(del);

            if(!exist){
                mL.add(new WeakReference<>(l));
            }
        }
    }

    public void remove(T l){
        synchronized (mL){
            List<WeakReference<T>> del = new ArrayList<>();
            for(WeakReference<T> wrf:mL){
                T ll = wrf.get();
                if(ll == null || ll == l){
                    del.add(wrf);
                }
            }

            mL.removeAll(del);
        }
    }

    public List<T> alive(){
        List<T> nl = new ArrayList<>();
        synchronized (mL){
            List<WeakReference<T>> del = new ArrayList<>();

            for(WeakReference<T> wrf:mL){
                T ll = wrf.get();
                if(ll == null){
                    del.add(wrf);
                }else{
                    nl.add(ll);
                }
            }

            mL.removeAll(del);
        }

        return nl;
    }

}
