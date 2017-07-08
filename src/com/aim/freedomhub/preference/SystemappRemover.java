/*=========================================================================
 *
 *  PROJECT:  SlimRoms
 *            Team Slimroms (http://www.slimroms.net)
 *
 *  COPYRIGHT Copyright (C) 2013 Slimroms http://www.slimroms.net
 *            All rights reserved
 *
 *  LICENSE   http://www.gnu.org/licenses/gpl-2.0.html GNU/GPL
 *
 *  AUTHORS:     fronti90
 *  DESCRIPTION: SlimSizer: manage your apps
 *
 *=========================================================================
 */
package com.aim.freedomhub.preference;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;
import java.util.ListIterator;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.settings.R;

import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.logging.MetricsLogger;

public class SystemappRemover extends SettingsPreferenceFragment {


    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.AMIFY;
    }

    private final int STARTUP_DIALOG = 1;
    private final int DELETE_DIALOG = 2;
    private final int DELETE_MULTIPLE_DIALOG = 3;

    protected ArrayAdapter<String> adapter;
    private ArrayList<String> mSysApp;
    private boolean startup =true;
    public final String systemPath = "/system/app/";
    public final String systemPrivPath = "/system/priv-app/";
    protected Process superUser;
    protected DataOutputStream dos;
    private ProgressDialog progress;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View view = inflater.inflate(R.layout.slim_sizer, container, false);
         return view;
     }

    @Override
      public void setUserVisibleHint(boolean isVisibleToUser) {
          super.setUserVisibleHint(isVisibleToUser);
          if (isVisibleToUser && adapter!=null && startup==true) {
              showDialog(STARTUP_DIALOG, null, adapter);
              startup=false;
          }
      }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Button delButton = (Button) getView().findViewById(R.id.btn_delete);

        // create arraylist of apps not to be removed
        final ArrayList<String> safetyList = new ArrayList<String>();
        safetyList.add("CertInstaller");
        safetyList.add("DrmProvider");
        safetyList.add("PackageInstaller");
        safetyList.add("Superuser");
        safetyList.add("TelephonyProvider");

        // create arraylist from /system/app and /system/priv-app content
        File system = new File(systemPath);
        File systemPriv = new File(systemPrivPath);
        String[] sysappArray = combine(system.list(), systemPriv.list());
        mSysApp = new ArrayList<String>(
                Arrays.asList(sysappArray));
         
         // remove .odex files from list
            filterOdex();

        // remove "apps not to be removed" from list and sort list
        mSysApp.removeAll(safetyList);
        Collections.sort(mSysApp);

        // populate listview via arrayadapter
        adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_multiple_choice, mSysApp);

        // startup dialog
        //showDialog(STARTUP_DIALOG, null, adapter);

        final ListView lv = (ListView) getView().findViewById(R.id.listsystem);
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lv.setAdapter(adapter);

        // longclick an entry
        lv.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                    final int arg2, long arg3) {
                // create deletion dialog
                String item = lv.getAdapter().getItem(arg2).toString();
                showDialog(DELETE_DIALOG, item, adapter);
                return false;
            }
        });
        // click button delete
        delButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // check which items are selected
                String item = null;
                int len = lv.getCount();
                SparseBooleanArray checked = lv.getCheckedItemPositions();
                for (int i = lv.getCount() - 1; i >= 0; i--) {
                    if (checked.get(i)) {
                        item = mSysApp.get(i);
                    }
                }
                if (item == null) {
                    toast(getResources().getString(
                            R.string.sizer_message_noselect));
                    return;
                } else {
                showDialog(DELETE_MULTIPLE_DIALOG, item, adapter);
                }
            }
        });
    }
    @Override
      public void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
      }

    private void showDialog(int id, final String item,
            final ArrayAdapter<String> adapter) {
        // startup dialog
        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        if (id == STARTUP_DIALOG) {
            // create warning dialog
            alert.setMessage(R.string.sizer_message_startup)
                    .setTitle(R.string.caution)
                    .setCancelable(true)
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int id) {
                                    // action for ok
                                    dialog.cancel();
                                }
                            });
        // delete dialog
        } else if (id == DELETE_DIALOG) {
            alert.setMessage(R.string.sizer_message_delete)
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int id) {
                                    // action for ok
                                    // call delete
                                    progress = new ProgressDialog(getView().getContext());
                                    progress.setTitle(getString(R.string.delete_progress_title));
                                    progress.setMessage(getString(R.string.delete_progress));
                                    progress.setCancelable(false);
                                    progress.show();
                                    new SystemappRemover.Deleter() {
                                        protected void onPostExecute(Boolean result) {
                                            try {
                                                dos.flush();
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            progress.dismiss();
                                            toast(getResources().getString(result ? R.string.sizer_message_success : R.string.sizer_message_error));
                                            if (result){
                                                // remove list entry
                                                adapter.remove(item);
                                                adapter.notifyDataSetChanged();
                                            }
                                        }
                                    }.execute(item);
                                }
                            })
                    .setNegativeButton(R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int id) {
                                    // action for cancel
                                    dialog.cancel();
                                }
                            });
        } else if (id == DELETE_MULTIPLE_DIALOG) {
            final ListView lv = (ListView) getView().findViewById(R.id.listsystem);
            final SparseBooleanArray checked = lv.getCheckedItemPositions();
            ArrayList<String> itemsList = new ArrayList<String>();
            for (int i = lv.getCount() - 1; i > 0; i--) {
                if (checked.get(i)) {
                    String appName = mSysApp.get(i);
                    itemsList.add(appName);
                }
            }
            alert.setMessage(R.string.sizer_message_delete)
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int id) {
                                    progress = new ProgressDialog(getView().getContext());
                                    progress.setTitle(getString(R.string.delete_progress_title));
                                    progress.setMessage(getString(R.string.delete_progress));
                                    progress.setCancelable(false);
                                    progress.show();
                                    new SystemappRemover.Deleter() {
                                        protected void onPostExecute(Boolean result) {
                                            try {
                                                dos.flush();
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            progress.dismiss();
                                            toast(getResources().getString(result ? R.string.sizer_message_success : R.string.sizer_message_error));
                                            if (result){
                                                for (int i = lv.getCount() - 1; i > 0; i--) {
                                                    if (checked.get(i)) {
                                                        String appName = mSysApp.get(i);
                                                        // remove list entry
                                                        lv.setItemChecked(i, false);
                                                        adapter.remove(appName);
                                                    }
                                                }
                                                adapter.notifyDataSetChanged();
                                            }
                                        }
                                    }.execute(itemsList.toArray(new String[itemsList.size()]));
                                }
                            })
                    .setNegativeButton(R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int id) {
                                    // action for cancel
                                    dialog.cancel();
                                }
                            });
        }
        // show warning dialog
        alert.show();
    }
  
        private String[] combine(String[] a, String[] b) {
        int length = a.length + b.length;
        String[] result = new String[length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    private void filterOdex() {
        ListIterator<String> it = mSysApp.listIterator();
        while ( it.hasNext() ) {
            String str = it.next();
            if ( str.endsWith(".odex") ) {
                it.remove();
            }
        }
    }

    public void toast(String text) {
        // easy toasts for all!
        Toast toast = Toast.makeText(getView().getContext(), text,
                Toast.LENGTH_SHORT);
        toast.show();
    }

    private short sdAvailable() {
        // check if sdcard is available
        // taken from developer.android.com
        short mExternalStorageAvailable = 0;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = 2;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = 1;
        } else {
            // Something else is wrong. It may be one of many other states, but
            // all we need
            // to know is we can neither read nor write
            mExternalStorageAvailable = 0;
        }
        return mExternalStorageAvailable;
    }

    // mount /system as ro on close
    protected void onStop(Bundle savedInstanceState) throws IOException {
        try {
           dos.writeBytes("\n" + "mount -o remount,ro /system" + "\n");
              dos.writeBytes("\n" + "exit" + "\n");
              dos.flush();
              dos.close();
          } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class Deleter extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (dos == null) {
                try {
                    superUser = new ProcessBuilder("su", "-c", "/system/bin/sh").start();
                    dos = new DataOutputStream(superUser.getOutputStream());
                    dos.writeBytes("\n" + "mount -o remount,rw /system" + "\n");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        protected Boolean doInBackground(String... params) {
            if (dos == null){
                return false;
            }
            for (String appName : params) {
                String odexAppName = appName.replaceAll(".apk$", ".odex");
                String basePath = systemPath;
                 File app = new File(systemPath + appName);

                if( ! app.exists() )
                    basePath = systemPrivPath;
                try {
                    dos.writeBytes("\n" + "rm -rf '" + basePath + "*" + appName + "'\n");
                    // needed in case user is using odexed ROM
                    File odex = new File(basePath + odexAppName);
                    if( odex.exists() )
                         dos.writeBytes("\n" + "rm -rf '" + basePath + odexAppName + "'\n");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
    }
}
