package com.opentaxi.android.adapters;

import com.opentaxi.generated.mysql.tables.pojos.Regions;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 11/1/13
 * Time: 1:44 PM
 * developer STANIMIR MARINOV
 */
public class RegionsAdapter {

    private java.lang.Integer id;
    private java.lang.String description;

    public RegionsAdapter() {

    }

    public RegionsAdapter(Regions region) {
        this.id = region.getId();
        this.description = region.getDescription();
    }

    @Override
    public String toString() {
        return this.description; //what you want displayed for each row in the listview
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || this.id == null) return false;
        if (!(obj instanceof RegionsAdapter)) return false;
        RegionsAdapter regionsAdapter = (RegionsAdapter) obj;
        return this.id.equals(regionsAdapter.getId());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }
}
