package com.shiyukine.touchtransporter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.google.android.material.snackbar.Snackbar;

import java.net.DatagramSocket;
import java.net.InetAddress;


public class MainActivity extends AppCompatActivity {
    public static boolean osuM = false;
    public static String version = "";
    public static int verCode = -1;
    public static int revision = 1;
    public static boolean automc = true;
    public static boolean revert = false;
    public static boolean histEv = true;

    CircularProgressDrawable cpd;

    private String urlUpdate = "";

    ArrayAdapter<String> adapter;

    static SharedPreferences settings;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        settings = getSharedPreferences("UserInfo", 0);
        try {
            PackageInfo pInfo = MainActivity.this.getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
            verCode = pInfo.versionCode;
            ((TextView)findViewById(R.id.infos)).setText("By Shiyukine - Aketsuky\n" + version + "." + revision);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String lastPr = settings.getString("CurProfile", "Default");
        final Spinner langOpt = findViewById(R.id.profiles);
        setPro();
        //
        langOpt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setSetting("CurProfile", adapter.getItem(position));
                String pro = settings.getString("CurProfile", "Default");
                int i = 0;
                for(int in : Profiles.getProfil(pro))
                {
                    Graph.margin[i] = in;
                    i++;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        langOpt.setSelection(adapter.getPosition(lastPr));
        if(settings.contains("osuM"))
        {
            osuM = settings.getBoolean("osuM", false);
            ((CheckBox)findViewById(R.id.osu)).setChecked(osuM);
        }
        if(settings.contains("automc"))
        {
            automc = settings.getBoolean("automc", false);
            ((CheckBox)findViewById(R.id.automc)).setChecked(automc);
        }
        if(settings.contains("revert"))
        {
            revert = settings.getBoolean("revert", false);
            ((CheckBox)findViewById(R.id.revert)).setChecked(revert);
        }
        if(settings.contains("historicEv"))
        {
            histEv = settings.getBoolean("historicEv", false);
            ((CheckBox)findViewById(R.id.hist)).setChecked(histEv);
        }
        if(settings.contains("press")) Graph.pressI = settings.getInt("press", 0);
        ((EditText)findViewById(R.id.serv_url)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_GO)
                {
                    chServ(null);
                }
                return false;
            }
        });
        ((EditText)findViewById(R.id.profiles_n)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_GO)
                {
                    if(v.getText().toString().contains("_")) Snackbar.make(findViewById(android.R.id.content), "You can't put \"_\" in your profile name !", Snackbar.LENGTH_LONG)
                            .show();
                    else Profiles.addProfile(v.getText().toString(), false, MainActivity.this);
                    setPro();
                    langOpt.setSelection(adapter.getCount() - 1);
                    //
                    v.requestFocus();
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    v.clearFocus();
                    findViewById(R.id.newPro).setVisibility(View.GONE);
                }
                return false;
            }
        });
        ((EditText)findViewById(R.id.conn_all_t)).setText(settings.getString("LastIP", "127.0.0.1"));
        ((EditText)findViewById(R.id.conn_all_t)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_GO)
                {
                    setSetting("LastIP", v.getText().toString());
                    //
                    v.requestFocus();
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    v.clearFocus();
                }
                return false;
            }
        });
        ((EditText)findViewById(R.id.serv_url)).setText(settings.getString("Url_Server", "aketsuky.com"));
        searchUpd();
        RadioGroup rg = (RadioGroup)findViewById(R.id.conn_options);
        int connopt = settings.getInt("Conn_Options", 0);
        rg.check(rg.getChildAt(connopt).getId());
        if(connopt == 1) findViewById(R.id.conn_all_p).setVisibility(View.VISIBLE);
        cpd = new CircularProgressDrawable(this);
        final FrameLayout loader = findViewById(R.id.conn_load);
        cpd.setStrokeWidth(5f);
        cpd.setCenterRadius(20f);
        cpd.setColorSchemeColors(Color.WHITE);
        loader.setBackground(cpd);
    }

    private void setPro()
    {
        final Spinner langOpt = findViewById(R.id.profiles);
        Object[] profiles = Profiles.getProfiles().keySet().toArray();
        String[] prof = new String[profiles.length];
        int i2 = profiles.length - 1;
        for(int i = 0 ; i < profiles.length ; i ++){
            try {
                prof[i] = profiles[i2].toString();
                i2--;
            } catch (NullPointerException ex) {
                // do some default initialization
            }
        }
        adapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item,
                prof);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        langOpt.setAdapter(adapter);
    }

    public void osu_click(View view) {
        osuM = ((CheckBox)view).isChecked();
        setSetting("osuM", osuM);
    }

    public void setT(View view) {
        Graph.isSet = true;
        startActivity(new Intent(MainActivity.this, Graph.class));
    }

    @SuppressLint("StaticFieldLeak")
    public void Log(View view) {
        try {
            if (findViewById(R.id.conn).getVisibility() == View.GONE) {
                findViewById(R.id.conn).setVisibility(View.VISIBLE);
            } else {
                cpd.start();
                findViewById(R.id.log).setVisibility(View.GONE);
                findViewById(R.id.conn_back).setVisibility(View.GONE);
                Graph.isUsb = ((RadioButton) findViewById(R.id.conn_usb)).isChecked();
                if (!Graph.isUsb) {
                    Graph.socketU = new DatagramSocket();
                    Graph.address = InetAddress.getByName(((EditText) findViewById(R.id.conn_all_t)).getText().toString());
                }
                new ConnectTcp() {
                    @Override
                    protected void onPostExecute(Boolean aBoolean) {
                        if (aBoolean && Graph.socket != null && Graph.socket.isConnected() && !Graph.socket.isClosed()) {
                            Graph.isSet = false;
                            findViewById(R.id.conn).setVisibility(View.GONE);
                            startActivity(new Intent(MainActivity.this, Graph.class));
                        }
                        else
                        {
                            findViewById(R.id.conn).setVisibility(View.GONE);
                            Snackbar.make(findViewById(android.R.id.content), "We can't connect to your PC. Check that the server is enabled.", Snackbar.LENGTH_LONG)
                                    .show();
                        }
                        cpd.stop();
                        findViewById(R.id.log).setVisibility(View.VISIBLE);
                        findViewById(R.id.conn_back).setVisibility(View.VISIBLE);
                        super.onPostExecute(aBoolean);
                    }
                }.execute(((EditText) findViewById(R.id.conn_all_t)).getText().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Error", "E: " + e.getMessage());
        }
    }

    public static SharedPreferences getSettings() {
        return settings;
    }

    public static void setSetting(String key, Object value) {
        SharedPreferences.Editor ed = settings.edit();
        if (value.getClass().getName().equals(String.class.getName()))  ed.putString(key, (String) value);
        if (value.getClass().getName().equals(Integer.class.getName())) ed.putInt(key, (int) value);
        if (value.getClass().getName().equals(Boolean.class.getName())) ed.putBoolean(key, (boolean) value);
        ed.apply();
    }

    public void mouse_click(View view) {
        automc = ((CheckBox)view).isChecked();
        setSetting("automc", automc);
    }

    public void reset_cl(View view) {
        Profiles.addProfile(settings.getString("CurProfile", "Default"), true, MainActivity.this);
        Graph.margin = Profiles.getProfil(settings.getString("CurProfile", "Default"));
    }

    private WebView webView;

    public void upd_click(View view) {
        searchUpd();
    }

    private void searchUpd()
    {
        webView = new WebView(MainActivity.this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                webView.evaluateJavascript("document.body.innerText", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        try {
                            StringBuilder val = new StringBuilder(value);
                            val.deleteCharAt(0);
                            val.deleteCharAt(val.length() - 1);
                            if(Integer.parseInt(val.toString().split("\\\\n")[0]) > MainActivity.verCode) {
                                Log.d("Update", "Available update " + MainActivity.verCode + " -> " + val.toString());
                                Toast.makeText(MainActivity.this, "An update is available.", Toast.LENGTH_SHORT).show();
                                urlUpdate = val.toString().split("\\\\n")[1];
                                MainActivity.this.updapp();
                            }
                            else Toast.makeText(MainActivity.this, "No new updates.", Toast.LENGTH_SHORT).show();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            Snackbar.make(findViewById(android.R.id.content), "Can't search updates.", Snackbar.LENGTH_LONG)
                                    .show();
                        }
                    }
                });
                super.onPageFinished(view, url);
            }
        });
        webView.loadUrl("http://" + settings.getString("Url_Server", "aketsuky.com") + "/dl/TouchTransporter/update_c.php");
    }

    public void updapp()
    {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + urlUpdate + "/dl/TouchTransporter/touchtransporter.apk"));
        startActivity(browserIntent);
    }

    public void revert_click(View view) {
        revert = ((CheckBox)view).isChecked();
        setSetting("revert", revert);
    }

    public void useHist(View view) {
        histEv = ((CheckBox)view).isChecked();
        setSetting("historicEv", histEv);
    }

    public void chServ(View view) {
        setSetting("Url_Server", ((EditText)findViewById(R.id.serv_url)).getText().toString());
        Snackbar.make(findViewById(android.R.id.content), "Modified !", Snackbar.LENGTH_LONG)
                .show();
    }

    public void addProfile(View view) {
        Snackbar.make(findViewById(android.R.id.content), "Please enter a name.", Snackbar.LENGTH_LONG)
                .show();
        findViewById(R.id.newPro).setVisibility(View.VISIBLE);
    }

    public void removeProfile(View view) {
        String cur = settings.getString("CurProfile", "Default");
        if(cur.equals("Default")) Snackbar.make(findViewById(android.R.id.content), "You can't remove the default profile", Snackbar.LENGTH_LONG)
                .show();
        else {
            Profiles.removeProfile(cur);
            setPro();
        }
    }

    public void backProfile(View view) {
        findViewById(R.id.newPro).setVisibility(View.GONE);
    }

    public void showOrHideOther(View view) {
        if(findViewById(R.id.set_other).getVisibility() == View.GONE) findViewById(R.id.set_other).setVisibility(View.VISIBLE);
        else findViewById(R.id.set_other).setVisibility(View.GONE);
    }

    public void conn_usb_click(View view) {
        findViewById(R.id.conn_all_p).setVisibility(View.GONE);
        setSetting("Conn_Options", 0);
    }

    public void conn_all_click(View view) {
        findViewById(R.id.conn_all_p).setVisibility(View.VISIBLE);
        setSetting("Conn_Options", 1);
    }

    public void back(View view) {
        findViewById(R.id.conn).setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        if(findViewById(R.id.conn).getVisibility() == View.VISIBLE) findViewById(R.id.conn).setVisibility(View.GONE);
        else super.onBackPressed();
    }
}
