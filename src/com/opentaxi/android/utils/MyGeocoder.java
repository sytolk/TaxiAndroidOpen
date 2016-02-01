package com.opentaxi.android.utils;

import android.location.Address;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 10/29/14
 * Time: 10:23 AM
 * developer STANIMIR MARINOV
 */
public class MyGeocoder {

    public static List<Address> getFromLocation(double lat, double lng, int maxResult) {
        List<Address> retList = null;
        String url = String.format(Locale.ENGLISH, "http://maps.googleapis.com/maps/api/geocode/json?latlng=%1$f,%2$f&sensor=false&language=" + Locale.getDefault().getCountry(), lat, lng);

        try {
            URL placeUrl = new URL(url);

            HttpURLConnection connection = (HttpURLConnection) placeUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {

                InputStream inputStream = connection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf8"));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }

                String json = buffer.toString();
                JSONObject jsonObject = new JSONObject(json); //stringBuilder.toString());

                retList = new ArrayList<>();

                if ("OK".equalsIgnoreCase(jsonObject.getString("status"))) {
                    JSONArray results = jsonObject.getJSONArray("results");
                    if (results.length() > 0) {
                        for (int i = 0; i < results.length() && i < maxResult; i++) {
                            JSONObject result = results.getJSONObject(i);
                            //Log.e(MyGeocoder.class.getName(), result.toString());
                            Address addr = new Address(Locale.getDefault());
                            // addr.setAddressLine(0, result.getString("formatted_address"));

                            JSONArray components = result.getJSONArray("address_components");
                            String streetNumber = "";
                            String route = "";
                            for (int a = 0; a < components.length(); a++) {
                                JSONObject component = components.getJSONObject(a);
                                JSONArray types = component.getJSONArray("types");
                                for (int j = 0; j < types.length(); j++) {
                                    String type = types.getString(j);
                                    if (type.equals("locality")) {
                                        addr.setLocality(component.getString("long_name"));
                                    } else if (type.equals("street_number")) {
                                        streetNumber = component.getString("long_name");
                                    } else if (type.equals("route")) {
                                        route = component.getString("long_name");
                                    }
                                }
                            }
                            addr.setAddressLine(0, route + " " + streetNumber);

                            addr.setLatitude(result.getJSONObject("geometry").getJSONObject("location").getDouble("lat"));
                            addr.setLongitude(result.getJSONObject("geometry").getJSONObject("location").getDouble("lng"));
                            retList.add(addr);
                        }
                    }
                }

                //Log.d(TAG, buffer.toString());
            } else {
                Log.i("getFromLocation", "Unsuccessful HTTP Response Code: " + responseCode);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return retList;
    }

    /*public static List<Address> getFromLocation(double lat, double lng, int maxResult) {

        String address = String.format(Locale.ENGLISH, "http://maps.googleapis.com/maps/api/geocode/json?latlng=%1$f,%2$f&sensor=false&language=" + Locale.getDefault().getCountry(), lat, lng);
        HttpGet httpGet = new HttpGet(address);
        HttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(AllClientPNames.USER_AGENT, "Mozilla/5.0 (Java) Gecko/20081007 java-geocoder");
        client.getParams().setIntParameter(AllClientPNames.CONNECTION_TIMEOUT, 5 * 1000);
        client.getParams().setIntParameter(AllClientPNames.SO_TIMEOUT, 25 * 1000);
        HttpResponse response;
        //StringBuilder stringBuilder = new StringBuilder();

        List<Address> retList = null;

        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            String json = EntityUtils.toString(entity, "UTF-8");
            *//*InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }*//*

            JSONObject jsonObject = new JSONObject(json); //stringBuilder.toString());

            retList = new ArrayList<Address>();

            if ("OK".equalsIgnoreCase(jsonObject.getString("status"))) {
                JSONArray results = jsonObject.getJSONArray("results");
                if (results.length() > 0) {
                    for (int i = 0; i < results.length() && i < maxResult; i++) {
                        JSONObject result = results.getJSONObject(i);
                        //Log.e(MyGeocoder.class.getName(), result.toString());
                        Address addr = new Address(Locale.getDefault());
                        // addr.setAddressLine(0, result.getString("formatted_address"));

                        JSONArray components = result.getJSONArray("address_components");
                        String streetNumber = "";
                        String route = "";
                        for (int a = 0; a < components.length(); a++) {
                            JSONObject component = components.getJSONObject(a);
                            JSONArray types = component.getJSONArray("types");
                            for (int j = 0; j < types.length(); j++) {
                                String type = types.getString(j);
                                if (type.equals("locality")) {
                                    addr.setLocality(component.getString("long_name"));
                                } else if (type.equals("street_number")) {
                                    streetNumber = component.getString("long_name");
                                } else if (type.equals("route")) {
                                    route = component.getString("long_name");
                                }
                            }
                        }
                        addr.setAddressLine(0, route + " " + streetNumber);

                        addr.setLatitude(result.getJSONObject("geometry").getJSONObject("location").getDouble("lat"));
                        addr.setLongitude(result.getJSONObject("geometry").getJSONObject("location").getDouble("lng"));
                        retList.add(addr);
                    }
                }
            }


        } catch (ClientProtocolException e) {
            Log.e(MyGeocoder.class.getName(), "Error calling Google geocode webservice.", e);
        } catch (IOException e) {
            Log.e(MyGeocoder.class.getName(), "Error calling Google geocode webservice.", e);
        } catch (JSONException e) {
            Log.e(MyGeocoder.class.getName(), "Error parsing Google geocode webservice response.", e);
        }

        return retList;
    }*/

    /*private static String getCityAddress(JSONObject place) {
        try {
            JSONArray components = place.getJSONArray("address_components");
            for (int i = 0; i < components.length(); i++) {
                JSONObject component = components.getJSONObject(i);
                JSONArray types = component.getJSONArray("types");
                for (int j = 0; j < types.length(); j++) {
                    if (types.getString(j).equals("locality")) {
                        return component.getString("long_name");
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }*/
}
