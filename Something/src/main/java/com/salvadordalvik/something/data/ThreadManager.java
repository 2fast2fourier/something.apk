package com.salvadordalvik.something.data;

import android.util.SparseArray;

import com.salvadordalvik.something.list.ThreadItem;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by matthewshepard on 2/11/14.
 */
public class ThreadManager {
    private static final SparseArray<WeakReference<ThreadItem>> threads = new SparseArray<WeakReference<ThreadItem>>();

    public static void clearThreadCache(){
        threads.clear();
    }

    public static ThreadItem getThread(int threadId){
        WeakReference<ThreadItem> item = threads.get(threadId);
        return item != null ? item.get() : null;
    }

    public static void putThreadList(List<ThreadItem> threadList){
        for(ThreadItem thread : threadList){
            threads.put(thread.getId(), new WeakReference<ThreadItem>(thread));
        }
    }

    public static void putThreads(ThreadItem... threadList){
        for(ThreadItem thread : threadList){
            threads.put(thread.getId(), new WeakReference<ThreadItem>(thread));
        }
    }
}
