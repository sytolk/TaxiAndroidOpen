package com.opentaxi.android.adapters;

import com.stil.generated.mysql.tables.pojos.Regions;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 11/1/13
 * Time: 1:44 PM
 * developer STANIMIR MARINOV
 */
public class RegionsAdapter {

    private Regions regions;

    public RegionsAdapter(Regions regions) {
        this.regions = regions;
    }

    @Override
    public String toString() {
        if (regions != null)
            return this.regions.getDescription(); //what you want displayed for each row in the listview
        return "";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || this.regions == null) return false;
        if (!(obj instanceof RegionsAdapter)) return false;
        RegionsAdapter regionsAdapter = (RegionsAdapter) obj;
        if (regionsAdapter.getRegions() == null) return false;
        return this.regions.getId().equals(regionsAdapter.getRegions().getId());
    }

    public Regions getRegions() {
        return regions;
    }
}
