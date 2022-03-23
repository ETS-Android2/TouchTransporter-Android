package com.shiyukine.touchtransporter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.Socket;

public class SendInfo extends AsyncTask<Object, Void, Boolean> {

    Exception e = null;
    static int counting = 0;

    private void showD()
    {
        Graph.errorShowed = true;
        new AlertDialog.Builder(Graph.g)
                .setTitle("Disconnected")
                .setMessage("We are sorry, but you're no longer connected to a server. Please log back in to continue.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        (Graph.g).onBackPressed();
                    }
                })
                .setCancelable(false)
                .create().show();
    }

    @Override
    protected Boolean doInBackground(final Object[] objects) {
        try {
            if(!Graph.isUsb)
            {
                String msg = (String)objects[0];
                Graph.buf = msg.getBytes();
                DatagramPacket packet
                        = new DatagramPacket(Graph.buf, Graph.buf.length, Graph.address, 30921);
                Graph.socketU.send(packet);
                if(!MainActivity.osuM) {
                    counting++;
                    if (counting == 5) {
                        counting = 0;
                        Graph.output.write("test");
                        return !Graph.output.checkError();
                    }
                }
                return true;
            }
            else {
                Graph.output.write((String) objects[0]);
                //Graph.output.flush();
                return !Graph.output.checkError();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Error", "E" + e.getMessage());
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if(!aBoolean && !Graph.errorShowed) showD();
        super.onPostExecute(aBoolean);
    }
}
