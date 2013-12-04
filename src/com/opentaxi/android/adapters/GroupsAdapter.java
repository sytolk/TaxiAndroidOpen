package com.opentaxi.android.adapters;

import com.opentaxi.generated.mysql.tables.pojos.Groups;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 11/1/13
 * Time: 1:44 PM
 * developer STANIMIR MARINOV
 */
public class GroupsAdapter {

    private Groups groups;

    public GroupsAdapter(Groups groups) {
        this.groups = groups;
    }

    @Override
    public String toString() {
        return groups.getDescription(); //what you want displayed for each row in the listview
    }

    public Groups getGroups() {
        return groups;
    }
}
