package com.shiyukine.touchtransporter;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.TypedValue;

import java.util.HashMap;
import java.util.Map;

import static com.shiyukine.touchtransporter.MainActivity.setSetting;

public class Profiles {
    public static HashMap<String, Integer[]> getProfiles()
    {
        HashMap<String, Integer[]> profi = new HashMap<>();
        profi.put("Default", new Integer[] {
                MainActivity.settings.getInt("margin_0", 0),
                MainActivity.settings.getInt("margin_1", 0),
                MainActivity.settings.getInt("margin_2", 0),
                MainActivity.settings.getInt("margin_3", 0)
        });
        for(Map.Entry<String, ?> set : MainActivity.settings.getAll().entrySet())
        {
            if(set.getKey().contains("_profil_margin_0"))
            {
                String p = set.getKey().split("_")[0];
                profi.put(p, new Integer[] {
                        MainActivity.settings.getInt(p + "_profil_margin_0", 0),
                        MainActivity.settings.getInt(p + "_profil_margin_1", 0),
                        MainActivity.settings.getInt(p + "_profil_margin_2", 0),
                        MainActivity.settings.getInt(p + "_profil_margin_3", 0)
                });
            }
        }
        return profi;
    }

    public static int[] getProfil(String profil)
    {
        if(profil.equals("Default"))
        {
            return new int[] {
                    MainActivity.settings.getInt("margin_0", -1),
                    MainActivity.settings.getInt("margin_1", -1),
                    MainActivity.settings.getInt("margin_2", -1),
                    MainActivity.settings.getInt("margin_3", -1)
            };
        }
        for(Map.Entry<String, ?> set : MainActivity.settings.getAll().entrySet())
        {
            if(set.getKey().contains(profil + "_profil_margin_0"))
            {
                String p = set.getKey().split("_")[0];
                return new int[] {
                        MainActivity.settings.getInt(p + "_profil_margin_0", -1),
                        MainActivity.settings.getInt(p + "_profil_margin_1", -1),
                        MainActivity.settings.getInt(p + "_profil_margin_2", -1),
                        MainActivity.settings.getInt(p + "_profil_margin_3", -1)
                };
            }
        }
        return new int[] { -1, -1, -1, -1};
    }

    public static boolean addProfile(String name, boolean force, MainActivity act)
    {
        String n = name + "_profil_";
        if(name.equals("Default")) n = "";
        if(MainActivity.settings.contains(n + "margin_0") && !force) return false;
        float dip = 12.5f;
        Resources r = act.getResources();
        float px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.getDisplayMetrics()
        );
        float dip2 = 50f;
        float px2 = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip2,
                r.getDisplayMetrics()
        );
        setSetting(n + "margin_0", (int)px);
        setSetting(n + "margin_1", (int)px);
        setSetting(n + "margin_2", (int)px);
        setSetting(n + "margin_3", (int)px2);
        return true;
    }

    public static boolean modifProfile(String name, int[] px)
    {
        if(name.equals("Default"))
        {
            int i = 0;
            for (int in : px) {
                MainActivity.setSetting("margin_" + i, in);
                Graph.margin[i] = in;
                i++;
            }
        }
        else {
            if (!MainActivity.settings.contains(name + "_profil_margin_0")) return false;
            int i = 0;
            for (int in : px) {
                MainActivity.setSetting(name + "_profil_margin_" + i, in);
                Graph.margin[i] = in;
                i++;
            }
        }
        return true;
    }

    public static boolean removeProfile(String name)
    {
        try {
            SharedPreferences.Editor sp = MainActivity.settings.edit();
            sp.remove(name + "_profil_margin_0");
            sp.remove(name + "_profil_margin_1");
            sp.remove(name + "_profil_margin_2");
            sp.remove(name + "_profil_margin_3");
            sp.apply();
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }
}
