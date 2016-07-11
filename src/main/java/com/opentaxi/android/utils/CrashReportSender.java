package com.opentaxi.android.utils;

import android.content.Context;
import com.opentaxi.rest.RestClient;
import com.stil.generated.mysql.tables.pojos.DebuggerLog;
import org.acra.ReportField;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 12/17/14
 * Time: 5:38 PM
 * developer STANIMIR MARINOV
 */
public class CrashReportSender implements ReportSender {

    //private static final String BASE_URL = "https://rink.hockeyapp.net/api/2/apps/";
    //private static final String CRASHES_PATH = "/crashes";

    @Override
    public void send(Context context, CrashReportData report) throws ReportSenderException {
        DebuggerLog log = new DebuggerLog();
        log.setAppVersionCode(Integer.parseInt(report.get(ReportField.APP_VERSION_CODE)));
        log.setAppVersionName(report.get(ReportField.APP_VERSION_NAME));
        log.setPackageName(report.get(ReportField.PACKAGE_NAME));
        log.setFilePath(report.get(ReportField.FILE_PATH));
        log.setPhoneModel(report.get(ReportField.PHONE_MODEL));
        log.setBrand(report.get(ReportField.BRAND));
        log.setProduct(report.get(ReportField.PRODUCT));
        log.setAndroidVersion(report.get(ReportField.ANDROID_VERSION));
        log.setBuild(report.get(ReportField.BUILD));
        log.setTotalMemSize(report.get(ReportField.TOTAL_MEM_SIZE));
        log.setAvailableMemSize(report.get(ReportField.AVAILABLE_MEM_SIZE));
        log.setStackTrace(report.get(ReportField.STACK_TRACE));
        log.setInitialConfiguration(report.get(ReportField.INITIAL_CONFIGURATION));
        log.setCrashConfiguration(report.get(ReportField.CRASH_CONFIGURATION));
        log.setDisplay(report.get(ReportField.DISPLAY));
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        try {
            log.setUserAppStartDate(new Timestamp(format.parse(report.get(ReportField.USER_APP_START_DATE)).getTime()));
            log.setUserCrashDate(new Timestamp(format.parse(report.get(ReportField.USER_CRASH_DATE)).getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        log.setDumpsysMeminfo(report.get(ReportField.DUMPSYS_MEMINFO));
        log.setLogcat(report.get(ReportField.LOGCAT));
        log.setInstallationId(report.get(ReportField.INSTALLATION_ID));
        log.setDeviceFeatures(report.get(ReportField.DEVICE_FEATURES));
        log.setEnvironment(report.get(ReportField.ENVIRONMENT));
        log.setSharedPreferences(report.get(ReportField.SHARED_PREFERENCES));
        log.setSettingsSystem(report.get(ReportField.SETTINGS_SYSTEM));
        log.setSettingsSecure(report.get(ReportField.SETTINGS_SECURE));
        RestClient.getInstance().sendLog(log);
        /*String log = createCrashLog(report);
        Log.i("CrashReportSender", log);
        String url = BASE_URL + ACRA.getConfig().formKey() + CRASHES_PATH;

        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);

            List<NameValuePair> parameters = new ArrayList<NameValuePair>();
            parameters.add(new BasicNameValuePair("raw", log));
            parameters.add(new BasicNameValuePair("userID", report.get(ReportField.INSTALLATION_ID)));
            parameters.add(new BasicNameValuePair("contact", report.get(ReportField.USER_EMAIL)));
            parameters.add(new BasicNameValuePair("description", report.get(ReportField.USER_COMMENT)));
            httpPost.setEntity(new UrlEncodedFormEntity(parameters, HTTP.UTF_8));

            httpClient.execute(httpPost);
        }
        catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    /*private String createCrashLog(CrashReportData report) {
        Date now = new Date();
        StringBuilder log = new StringBuilder();
        log.append("Package: " + report.get(ReportField.PACKAGE_NAME) + "\n");
        log.append("Version: " + report.get(ReportField.APP_VERSION_CODE) + "\n");
        log.append("Android: " + report.get(ReportField.ANDROID_VERSION) + "\n");
        log.append("Manufacturer: " + android.os.Build.MANUFACTURER + "\n");
        log.append("Model: " + report.get(ReportField.PHONE_MODEL) + "\n");
        log.append("Date: " + now + "\n");
        log.append("\n");
        log.append(report.get(ReportField.STACK_TRACE));

        return log.toString();
    }*/
}
