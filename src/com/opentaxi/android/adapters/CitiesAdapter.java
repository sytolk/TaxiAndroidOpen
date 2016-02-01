package com.opentaxi.android.adapters;

import com.stil.generated.mysql.tables.pojos.GeonameAdmin1;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 11/1/13
 * Time: 1:44 PM
 * developer STANIMIR MARINOV
 */
public class CitiesAdapter {

    private GeonameAdmin1 geonameAdmin1;
    private boolean isAscii;

    public CitiesAdapter(GeonameAdmin1 geonameAdmin1, boolean isAscii) {
        this.geonameAdmin1 = geonameAdmin1;
        this.isAscii = isAscii;
    }

    @Override
    public String toString() {
        if (geonameAdmin1 != null) {
            if (isAscii) return this.geonameAdmin1.getNameascii();
            else return this.geonameAdmin1.getName(); //what you want displayed for each row in the listview
        }
        return "";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || this.geonameAdmin1 == null) return false;
        if (!(obj instanceof RegionsAdapter)) return false;
        CitiesAdapter citiesAdapter = (CitiesAdapter) obj;
        if (citiesAdapter.getGeonameAdmin1() == null) return false;
        return this.geonameAdmin1.getGeonameid().equals(citiesAdapter.getGeonameAdmin1().getGeonameid());
    }

    public GeonameAdmin1 getGeonameAdmin1() {
        return geonameAdmin1;
    }
}
