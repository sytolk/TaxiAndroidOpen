package com.opentaxi.android.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Pair;
import com.opentaxi.android.utils.AppPreferences;
import com.opentaxi.models.NewRequest;
import com.opentaxi.models.RequestAddresses;
import com.opentaxi.rest.GoogleApiRestClient;
import com.opentaxi.rest.RestClient;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 3/21/13
 * Time: 3:22 PM
 * developer STANIMIR MARINOV
 */
@Deprecated
public class GoogleAPIClientTask extends AsyncTask<Context, Void, NewRequest> {

    private static final String TAG = "GoogleAPIClientTask";
    //private static String APIkey="AIzaSyDqDhi28WN6muGRbFvIXO1LKOfl4L7YT24";
    //private static String APIkey="ABQIAAAANGTAqDyDam_07aWkklK2NBSD41wX8VhCBpuiDVjGbFNuXE31lhQB8Gkwy-wmYbmaHIbJtfnlR9I_9A";
    private OnTaskCompleted listener;
    private Integer addressId;
    //private static final HttpTransport transport = new ApacheHttpTransport();

    public GoogleAPIClientTask(Integer addressId, OnTaskCompleted listener) {
        this.addressId = addressId;
        this.listener = listener;
    }

    @Override
    protected void onPostExecute(NewRequest newRequest) {
        //might want to change "executed" for the returned string passed into onPostExecute() but that is upto you
        listener.onTaskCompleted(newRequest);
    }

    @Override
    protected NewRequest doInBackground(Context... params) {

        NewRequest newRequest = AppPreferences.getInstance().getCurrentRequest();
        if (addressId != null && newRequest != null) {
            if (newRequest.getNorth() == null || newRequest.getNorth() == 0 || newRequest.getEast() == null || newRequest.getEast() == 0) {
                RequestAddresses requestAddresses = RestClient.getInstance().getAddress(addressId);
                if (requestAddresses != null) {
                    String region = "";
                    Map<Integer, String> regionsMap = AppPreferences.getInstance().getRegions();
                    if (regionsMap != null && regionsMap.containsKey(requestAddresses.getRegionsId())) {
                        region = "," + regionsMap.get(requestAddresses.getRegionsId());
                    }
                    StringBuilder address = new StringBuilder();
                    if (requestAddresses.getAddressesStreets() != null && requestAddresses.getAddressesStreets().getDescription() != null)
                        address.append(requestAddresses.getAddressesStreets().getDescription()).append(" ");
                    if (requestAddresses.getNumber() != null) address.append(requestAddresses.getNumber());

                    address.append(region);

                    Pair<Double, Double> adrCoordinates = GoogleApiRestClient.getInstance().getCoordinates(address.toString());
                    if (adrCoordinates != null) {

                        newRequest.setNorth(adrCoordinates.first);
                        newRequest.setEast(adrCoordinates.second);
                        AppPreferences.getInstance().setCurrentRequest(newRequest);
                        return newRequest;
                        // Log.i(TAG, adr);
                    }
                }
            }
        }
      /*  try {
            HttpRequestFactory httpRequestFactory = createRequestFactory(transport);
            HttpRequest request = httpRequestFactory.buildGetRequest(new GenericUrl("https://maps.googleapis.com/maps/api/place/search/json?"));  //http://maps.google.com/maps/api/place/search/json?
            request.url.put("key", APIkey);
           *//* request.url.put("location", latitude + "," + longitude);
            request.url.put("radius", 500);
            request.url.put("sensor", "false");*//*
            request.url.put("query", address);

            HttpResponse httpResponse= request.execute();
            String resp = httpResponse.parseAsString();
            if(resp.equals("")) {
                return new NewRequest();
            }
            //PlacesList places = request.execute().parseAs(PlacesList.class);//my pojo with private double longitude;   private double latitude;

           *//* System.out.println("STATUS = " + places.status);
            for (Place place : places.results) {
                System.out.println(place);
            }*//*
        } catch (HttpResponseException e) {
            try {
                System.err.println(e.response.parseAsString());
            } catch (IOException e1) {
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }*/
        return newRequest;
    }

   /* public static HttpRequestFactory createRequestFactory(final HttpTransport transport) {

        return transport.createRequestFactory(new HttpRequestInitializer() {
            public void initialize(HttpRequest request) {
                HttpHeaders headers = new HttpHeaders();
                //headers.setApplicationName("Google-Places-DemoApp");
                request.headers = headers;
                JsonHttpParser parser = new JsonHttpParser();
                parser.jsonFactory = new JacksonFactory();
                request.addParser(parser);
            }
        });
    }*/

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {

    }

    public interface OnTaskCompleted {
        void onTaskCompleted(NewRequest newRequest);
    }
}