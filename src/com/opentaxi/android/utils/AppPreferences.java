package com.opentaxi.android.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import com.opentaxi.android.asynctask.RegionsTask;
import com.opentaxi.generated.mysql.tables.pojos.Courses;
import com.opentaxi.models.NewRequest;
import com.opentaxi.models.Users;
import org.codehaus.jackson.map.ObjectMapper;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

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

    ObjectMapper mapper = new ObjectMapper();

    private static final String SOCKET = "SOCKET";
    private static final String SOCKET_TYPE = "SOCKET_TYPE";
    private static final String LAST_PLAYED_ITEM = "LAST_PLAYED_ITEM";
    private static final String APP_LAST_MODIFIED = "APP_LAST_MODIFIED";
    private static final String APP_VERSION = "APP_VERSION";
    private static final String PROPERTY_REG_ID = "registration_id";
    //private static String JSON_USERS = "jsonUsers";
    //private static String CLOUD_MESSAGE_ID = "cloudMessageId";

    /**
     * Singleton reference to this class.
     */
    private static AppPreferences instance;
    private static final Object mutex = new Object();
    private Users users;
    private Map<Integer, String> regionsMap;
    private Integer cloudMessageId;
    private NewRequest currentRequest;
    private NewRequest nextRequest;
    private Courses course;
    private boolean track = false;
    private Double north;
    private Double east;
    private long currentLocationTime; //datetime received from GPS
    private long gpsLastTime = 0; //local Android datetime of last received coordinates

    private int gpsRun = 0; //in Meters
    private int gpsSpeedSum = 0;
    private int gpsSpeedCount = 0;
    private String currentSocket;
    private int lastPlayedItem = 0;
    private long appModified = 0;
    private String appVersion;
    private String GCMRegId;
    private Integer socketType;
    private Context context;
    //private boolean enableProcessMsg = false;

    public AppPreferences(Context context) {
        this.appSharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
        this.context = context;
        //this.prefsEditor = appSharedPrefs.edit();
        //if (appSharedPrefs.getInt("car_number", 0) == 0) loadPreferences();
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
                //throw new RuntimeException(e);
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
                //throw new RuntimeException(e);
                if (e.getMessage() != null) Log.e("decrypt:" + value + " salt:" + salt, e.getMessage());
            }
        }
        return null;
    }

    public synchronized void setRegions() {
        if (regionsMap == null || regionsMap.isEmpty()) {
            new RegionsTask(new RegionsTask.OnTaskCompleted() {

                @Override
                public void onTaskCompleted(Map<Integer, String> regMap) {
                    regionsMap = regMap;
                }
            }).execute();
        }
    }

    public Map<Integer, String> getRegions() {
        if (regionsMap == null || regionsMap.isEmpty()) {
            setRegions();

            regionsMap = new LinkedHashMap<Integer, String>();
            regionsMap.put(8, "Славейков");
            regionsMap.put(9, "Изгрев");
            regionsMap.put(10, "Зорница");
            regionsMap.put(7, "Лазур");
            regionsMap.put(5, "Бр. Миладинови");
            regionsMap.put(6, "Центъра");
            regionsMap.put(4, "Възраждане");
            regionsMap.put(3, "Акациите");
            regionsMap.put(2, "Победа");
            regionsMap.put(1, "Меден Рудник");
            regionsMap.put(11, "Сарафово");
            regionsMap.put(15, "Крайморие");
            regionsMap.put(13, "Долно Езерово");
            regionsMap.put(14, "Горно Езерово");
            regionsMap.put(12, "Лозово");
            regionsMap.put(17, "Ветрен");
            regionsMap.put(18, "Банево");
        }
        return regionsMap;
    }

    /*public void registerGCM(Context context) {
        final String regId = GCMRegistrar.getRegistrationId(context);

        if (regId.equals("")) {
            // Automatically registers application on startup.
            GCMRegistrar.register(context, "767228808037"); //todo move project id to configuration
        } else { //if (!GCMRegistrar.isRegisteredOnServer(context)) {
            new GCMregisterTask(regId).execute(context);
        }
    }*/


    /*public boolean saveCarsList(List<Cars> array) {
        if (array != null && array.size() > 0) {
            prefsEditor = appSharedPrefs.edit();
            prefsEditor.putInt(CAR_LIST + "_size", array.size());
            for (int i = 0; i < array.size(); i++)
                prefsEditor.putString(CAR_LIST + "_" + i, array.get(i).getNumber());
            return prefsEditor.commit();
        }
        return false;
    }

    public String[] loadServerCarsList() {
        setServerConfiguration();
        return loadCarsList();
    }

    public String[] loadCarsList() {
        int size = appSharedPrefs.getInt(CAR_LIST + "_size", 0);
        String array[] = new String[size];
        for (int i = 0; i < size; i++)
            array[i] = appSharedPrefs.getString(CAR_LIST + "_" + i, "");
        return array;
    }*/

    public NewRequest getCurrentRequest() {
        return this.currentRequest;
    }

    public void setCurrentRequest(NewRequest currentRequest) {
        this.currentRequest = currentRequest;
        if (currentRequest != null) Log.i("setCurrentRequest", "setCurrentRequest:" + currentRequest.getRequestsId());
        else Log.i("setCurrentRequest", "setCurrentRequest:null");
    }

    public NewRequest getNextRequest() {
        return nextRequest;
    }

    public void setNextRequest(NewRequest nextRequest) {
        this.nextRequest = nextRequest;
        if (nextRequest != null) Log.i("nextRequest", "nextRequest:" + nextRequest.getRequestsId());
        else Log.i("nextRequest", "nextRequest:null");
    }

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

    public ObjectMapper getMapper() {
        return mapper;
    }

    public Courses getCourse() {
        if (course == null) {
            try {
                String courseJson = appSharedPrefs.getString(Courses.class.getName(), "");
                if (!courseJson.equals("")) course = mapper.readValue(courseJson, Courses.class);
            } catch (IOException e) {
                Log.e("getCourse", e.getMessage());
            }
        }
        return course;
    }

    public void setCourse(Courses course) {

        this.course = course;
        try {
            String json = "";
            if (this.course != null) json = mapper.writeValueAsString(course);

            this.prefsEditor = appSharedPrefs.edit();
            prefsEditor.putString(Courses.class.getName(), json);
            prefsEditor.commit();
        } catch (IOException e) {
            Log.e("setCourse", e.getMessage());
        }
    }

    public boolean getTrack() {
        return track;
    }

    public void setTrack(boolean track) {
        this.track = track;
    }

    public Double getNorth() {
        return north;
    }

    public void setNorth(Double north) {
        this.north = north;
    }

    public Double getEast() {
        return east;
    }

    public void setEast(Double east) {
        this.east = east;
    }

    public int getGpsSpeedCount() {
        return gpsSpeedCount;
    }

    public void setGpsSpeedCount(int gpsSpeedCount) {
        this.gpsSpeedCount = gpsSpeedCount;
    }

    public int getGpsSpeedSum() {
        return gpsSpeedSum;
    }

    public void setGpsSpeedSum(int gpsSpeedSum) {
        this.gpsSpeedSum = gpsSpeedSum;
    }

    public long getGpsLastTime() {
        return gpsLastTime;
    }

    public void setGpsLastTime(long gpsLastTime) {
        this.gpsLastTime = gpsLastTime;
    }

    public synchronized int getGpsRun() {
        return gpsRun;
    }

    public void setGpsRun(int gpsRun) {
        this.gpsRun = gpsRun;
    }

    public void startTrack() {
        track = true;
        gpsRun = 0;
        gpsSpeedSum = 0;
        gpsSpeedCount = 0;
    }

    public void endTrack() {
        track = false;
        gpsRun = 0;
        gpsSpeedSum = 0;
        gpsSpeedCount = 0;
    }

    public void setCurrentLocationTime(long currentLocationTime) {
        this.currentLocationTime = currentLocationTime;
    }

    public long getCurrentLocationTime() {
        return currentLocationTime;
    }

    public String getCurrentSocket() {
        if (currentSocket == null || currentSocket.equals("")) {
            currentSocket = appSharedPrefs.getString(SOCKET, "");
        }
        return currentSocket;
    }

    public void setCurrentSocket(String currentSocket) {
        if (currentSocket != null && !currentSocket.equals("")) {
            this.currentSocket = currentSocket;
            this.prefsEditor = appSharedPrefs.edit();
            prefsEditor.putString(SOCKET, currentSocket);
            prefsEditor.commit();
        }
    }

    public int getLastPlayedItem() {
        if (lastPlayedItem == 0) {
            lastPlayedItem = appSharedPrefs.getInt(LAST_PLAYED_ITEM, 0);
        }
        return lastPlayedItem;
    }

    public void setLastPlayedItem(int lastPlayedItem) {
        this.lastPlayedItem = lastPlayedItem;
        this.prefsEditor = appSharedPrefs.edit();
        prefsEditor.putInt(LAST_PLAYED_ITEM, lastPlayedItem);
        prefsEditor.commit();
    }

    public long getAppModified() {
        if (appModified == 0) {
            appModified = appSharedPrefs.getLong(APP_LAST_MODIFIED, 0);
        }
        return appModified;
    }

    public void setAppModified(long appModified) {
        this.appModified = appModified;
        this.prefsEditor = appSharedPrefs.edit();
        prefsEditor.putLong(APP_LAST_MODIFIED, appModified);
        prefsEditor.commit();
    }

    public String getAppVersion() {
        if (appVersion == null) {
            appVersion = appSharedPrefs.getString(APP_VERSION, "");
        }
        return appVersion;
    }

    public void setAppVersion(String version) {
        this.appVersion = version;
        this.prefsEditor = appSharedPrefs.edit();
        prefsEditor.putString(APP_VERSION, version);
        prefsEditor.commit();
    }

    /*public String getGCMRegId() {
        if (GCMRegId == null) {
            GCMRegId = appSharedPrefs.getString(PROPERTY_REG_ID, "");
        }
        return GCMRegId;
    }

    public void setGCMRegId(String reg_id) {
        this.GCMRegId = reg_id;
        this.prefsEditor = appSharedPrefs.edit();
        prefsEditor.putString(PROPERTY_REG_ID, reg_id);
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
            prefsEditor.commit();
        }
    }
}
