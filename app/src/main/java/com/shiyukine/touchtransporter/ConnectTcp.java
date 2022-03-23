package com.shiyukine.touchtransporter;

import android.os.AsyncTask;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import java.io.PrintWriter;
import java.net.Socket;

public class ConnectTcp extends AsyncTask<Object, Void, Boolean> {
    @Override
    protected Boolean doInBackground(Object... objs) {
        try {
            Graph.socket = new Socket((Graph.isUsb ? "127.0.0.1" : (String)objs[0]), 30921);
            Graph.output = new PrintWriter(Graph.socket.getOutputStream());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
