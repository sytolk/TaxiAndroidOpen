package com.opentaxi.android.adapters;

import com.stil.generated.mysql.tables.pojos.TaxiClientPrices;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 11/1/13
 * Time: 1:44 PM
 * developer STANIMIR MARINOV
 */
public class TaxiClientPricesAdapter {

    private TaxiClientPrices taxiClientPrices;

    public TaxiClientPricesAdapter(TaxiClientPrices taxiClientPrices) {
        this.taxiClientPrices = taxiClientPrices;
    }

    @Override
    public String toString() {
        return taxiClientPrices.getShortname();
    }

    public TaxiClientPrices getTaxiClientPrices() {
        return taxiClientPrices;
    }
}
