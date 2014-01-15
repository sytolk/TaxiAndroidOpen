package com.opentaxi.android.adapters;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 11/1/13
 * Time: 1:44 PM
 * developer STANIMIR MARINOV
 */
public class CitiesAdapter {

    private Integer id;
    private String description;

    public CitiesAdapter() {

    }

    public CitiesAdapter(String city) {
        this.id = 1;
        this.description = city;
    }

    @Override
    public String toString() {
        return this.description; //what you want displayed for each row in the listview
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || this.id == null) return false;
        if (!(obj instanceof CitiesAdapter)) return false;
        CitiesAdapter regionsAdapter = (CitiesAdapter) obj;
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
