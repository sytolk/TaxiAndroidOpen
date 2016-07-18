package com.opentaxi.android.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonSyntaxException;
import com.opentaxi.rest.RestClient;
import com.taxibulgaria.rest.models.Users;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 1/8/13
 * Time: 2:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class AppPreferences {
    private static final String APP_SHARED_PREFS = "com.opentaxi.android.driver_preferences"; //  Name of the file -.xml
    private SharedPreferences appSharedPrefs;
    private SharedPreferences.Editor prefsEditor;

    //ObjectMapper mapper = new ObjectMapper();

    private static final String SOCKET_TYPE = "SOCKET_TYPE";
    private static final String MAP_FILE = "MAP_FILE";
    //private static final String ACCESS_TOKEN = "ACCESS_TOKEN";

    /**
     * Singleton reference to this class.
     */
    private static AppPreferences instance;
    private static final Object mutex = new Object();
    //private Map<Integer, String> regionsMap;
    private Integer cloudMessageId;
    //private NewRequestDetails currentRequest;
    //private NewRequestDetails nextRequest;
    //private Double north;
    //private Double east;
    //private long currentLocationTime; //datetime received from GPS
    //private long gpsLastTime = 0; //local Android datetime of last received coordinates

    private String mapFile;
    //private String token;
    private Integer socketType;
    private Context context;
    private Users users;

    public AppPreferences(Context context) {
        this.appSharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
        this.context = context;
    }

    public static synchronized AppPreferences getInstance() {
        return instance;
    }

    public static synchronized AppPreferences getInstance(Context context) {

        if (instance == null) {
            synchronized (mutex) {
                if (instance == null) instance = new AppPreferences(context);
            }
        }

        return instance;
    }

    private final Object users_lock = new Object();

    public Users getUsers() {
        synchronized (users_lock) {
            if (users == null) {
                try {
                    String usersJson = appSharedPrefs.getString(Users.class.getSimpleName(), "");
                    if (!usersJson.equals(""))
                        users = RestClient.getInstance().getObjectMapper().readValue(usersJson, Users.class); //.fromJson(usersJson, Users.class);
                    if (users == null) users = new Users();
                } catch (IOException e) {
                    users = new Users();
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            return users;
        }
    }

    /**
     * @param users users
     */
    public void setUsers(Users users) {
        synchronized (users_lock) {
            //if (this.users != null) {
            this.users = users;
            //if(getCarState().equals(CarState.STATE_INEFFICIENT.getCode())) setCarState(CarState.STATE_FREE.getCode());
            try {
                this.prefsEditor = appSharedPrefs.edit();
                prefsEditor.putString(Users.class.getSimpleName(), RestClient.getInstance().getObjectMapper().writeValueAsString(users)); //.toJson(users));
                prefsEditor.apply();
                //return !(this.users.getCurrCars() == null);
            } catch (JsonProcessingException e){ //IOException e) {
                e.printStackTrace();
            }
            //}
            // return false;
        }
    }


    public String encrypt(String value, String salt) {
        if (value != null && salt != null) {
            try {
                final byte[] bytes = value.getBytes("UTF8"); //value != null ? value.getBytes("UTF8") : new byte[0];
                SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
                SecretKey key = keyFactory.generateSecret(new PBEKeySpec(salt.toCharArray()));
                Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
                pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID).getBytes("UTF8"), 20));
                String ret = new String(Base64.encode(pbeCipher.doFinal(bytes), Base64.NO_WRAP), "UTF8");
                Log.i("encrypt", "value:" + value + " to:" + ret);
                return ret;

            } catch (Exception e) {
                if (e.getMessage() != null) Log.e("encrypt:" + value + " salt:" + salt, e.getMessage());
            }
        }
        return null;
    }

    public String decrypt(String value, String salt) {
        if (value != null && salt != null) {
            try {
                final byte[] bytes = Base64.decode(value, Base64.DEFAULT); //value != null ? Base64.decode(value, Base64.DEFAULT) : new byte[0];
                SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
                SecretKey key = keyFactory.generateSecret(new PBEKeySpec(salt.toCharArray()));
                Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
                pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID).getBytes("UTF8"), 20));
                String ret = new String(pbeCipher.doFinal(bytes), "UTF8");
                Log.i("decrypt", "value:" + value + " to:" + ret);
                return ret;

            } catch (Exception e) {
                if (e.getMessage() != null) Log.e("decrypt:" + value + " salt:" + salt, e.getMessage());
            }
        }
        return null;
    }

    /*public synchronized void setRegions() {
        if (regionsMap == null || regionsMap.isEmpty()) {
            AsyncTask<Context, Void, Regions[]> regionsTask = new RegionsTask(new RegionsTask.OnTaskCompleted() {

                @Override
                public void onTaskCompleted(Map<Integer, String> regMap) {
                    regionsMap = regMap;
                }
            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                regionsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else regionsTask.execute();
        }
    }*/

    public synchronized Integer getLastCloudMessage() {
        //if (cloudMessageId == null) cloudMessageId = appSharedPrefs.getInt(CLOUD_MESSAGE_ID, 0);
        return cloudMessageId;
    }

    public synchronized void setLastCloudMessage(Integer cloudMessageId) {
        // if (cloudMessageId != null && (this.cloudMessageId == null || !this.cloudMessageId.equals(cloudMessageId))) {
        this.cloudMessageId = cloudMessageId;
       /*     this.prefsEditor = appSharedPrefs.edit();
            prefsEditor.putInt(CLOUD_MESSAGE_ID, cloudMessageId);
            prefsEditor.commit();
        }*/
    }

    /*public ObjectMapper getMapper() {
        return mapper;
    }*/

    public String getMapFile() {
        if (mapFile == null) {
            mapFile = appSharedPrefs.getString(MAP_FILE, null);
        }
        return mapFile;
    }

    public void setMapFile(String file) {
        this.mapFile = file;
        this.prefsEditor = appSharedPrefs.edit();
        prefsEditor.putString(MAP_FILE, file);
        prefsEditor.apply();
    }

    /*public String getAccessToken() {
        if (token == null) {
            token = appSharedPrefs.getString(ACCESS_TOKEN, "");
        }
        return token;
    }

    public void setAccessToken(String token) {
        this.token = token;
        this.prefsEditor = appSharedPrefs.edit();
        prefsEditor.putString(ACCESS_TOKEN, token);
        prefsEditor.commit();
    }*/

    public Integer getSocketType() {
        if (socketType == null) {
            socketType = appSharedPrefs.getInt(SOCKET_TYPE, 1); //2); todo
        }
        return socketType;
    }

    public void setSocketType(Integer type) {
        if (type != null) {
            this.socketType = type;
            this.prefsEditor = appSharedPrefs.edit();
            prefsEditor.putInt(SOCKET_TYPE, type);
            prefsEditor.apply();
        }
    }
}
