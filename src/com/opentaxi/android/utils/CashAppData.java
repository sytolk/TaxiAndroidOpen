//******************************************************************************
//* class: CashAppData
//* version 1.0
//* Copyright (c) Kavadani Ltd., 2006
//******************************************************************************

package com.opentaxi.android.utils;

import android.util.Log;
import com.opentaxi.generated.mysql.tables.pojos.DriverRequests;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

public class CashAppData {

    private static final String TAG = "CashAppData";

    private DriverRequests driverRequests;

    private int mode = -1; //unknown mode

    private int course = 0;  //<брой курсове>

    private String time;
    private String date;
    private String dateTime;

    private Double elapsedDist;
    private Double payedDist;
    private BigDecimal sumElapsedDistance;
    private BigDecimal sumAdditions;
    private BigDecimal sumTotal;

    private Date cashAppDate;
    private int cashAppConst;
    /////////////////////////////////////////////////////////////////////////////
    protected final int CATAG_MODE = 0;
    protected final int CATAG_TIME = 1;
    protected final int CATAG_DATE = 2;
    protected final int CATAG_COURSE = 3;
    protected final int CATAG_ELAPSED_DIST = 4;
    protected final int CATAG_PAYED_DIST = 5;
    protected final int CATAG_SUM_DIST = 6;
    protected final int CATAG_SUM_ADD = 7;
    protected final int CATAG_GENERAL_SUM = 8;
    protected final int CATAG_CONST = 9;

    // protected final int MAX_CATAGS = 10;

    public boolean parseError = false;
    public boolean haveMinLeigh = true;
    /////////////////////////////////////////////////////////////////////////////


/*
- при натискане на бутон „Режим” 
- преминаване на апарата от режим „Свободно” в режим „Заето”, 
- след отпечатване на бон
- преминаване режим „Каса” в режим „Свободно” 

<режим>
<time>
<date>
<брой курсове>
<изминати км>
<платени км>
<сума за изминато разстояние>
<сума добавки>
<обща сума>
<константа на апарата>
контролна сума.

Режим – 00 или 02,
като 00 е за преминаване от „Каса” в „Свободно”,
02 е при преминаване от режим „Свободно”  в режим „Заето”.

Останалите данни са в ASCI I формат. Контролната сума се образува от 
„изключващо ИЛИ” на данните намиращи се между знаците „<” и „>”. Формата 
на предаване на данните е 9600, 8,1, без контрол по четност. 

*/

    /**
     * <режим >
     * <time><date>
     * <брой курсове>
     * <изминати км>
     * <платени км>
     * <сума за изминато разстояние>
     * <сума добавки>
     * <обща сума>
     * <константа на апарата>
     * контролна сума
     *
     * @param strData
     */
    public DriverRequests parseCashAppData(String strData) {

        String str = "";
        haveMinLeigh = (strData.contains("<") && strData.contains(">"));

        strData = tidyData(strData);

        int pos = strData.indexOf("<" + (char) 0 + ">");
        if (pos == -1) {
            pos = strData.indexOf("<" + (char) 2 + ">");

            if (pos == -1) {
                //unknown mode
                if (strData.length() < 3) haveMinLeigh = false;
                //fix for <>...
                pos = strData.indexOf("<>");
                if (pos == -1) {
                    parseError = true;
                    Log.e(TAG, "CashAppData parser error: unknown mode");
                    //return null;
                } else mode = 0;//mode 0
                str = strData.substring(pos + 2); //2 - len of mode tag
                Log.i(TAG, "MODE=" + mode + " (shutdown)");
            } else {
                mode = 2;//mode 2
                str = strData.substring(pos + 3); //3 - len of mode tag
                Log.i(TAG, "MODE=" + mode + " (on)");
            }
        } else { //shutdown chashApp
            mode = 0;//mode 0
            str = strData.substring(pos + 3); //3 - len of mode tag
            Log.i(TAG, "MODE=" + mode + " (shutdown)");
        }

        try {
            if (!str.equals("")) {
                int endIndex;
                String innerTag;
                int i = CATAG_TIME;
                for (int x = 1; x < 10; x++) {
                    pos = str.indexOf('<');
                    endIndex = str.indexOf('>');
                    if (pos < 0 || endIndex < 0) {
                        parseError = true;
                        Log.e(TAG, "Defective data, parser brake!");
                        break;
                    }
                    innerTag = str.substring(pos + 1, endIndex);
                    innerTag = innerTag.trim();

                    //System.out.println("pos=" + pos + " endIndex=" + endIndex);
                    StringTokenizer tokens = new StringTokenizer(innerTag, " ");
                    if (tokens.countTokens() == 0) i++;
                    while (tokens.hasMoreTokens()) {
                        innerTag = tokens.nextToken();
                        switch (i) {
                            case CATAG_MODE:
                                //System.out.println("DATA="+innerTag);
                                break;
                            case CATAG_TIME:
                                time = innerTag;
                                break;
                            case CATAG_DATE:
                                date = innerTag;
                                break;
                            case CATAG_COURSE:
                                try {
                                    course = Integer.parseInt(innerTag);
                                } catch (NumberFormatException ex) {
                                    Log.e(TAG, "CashAppData course: NumberFormatException: ", ex);
                                }
                                break;
                            case CATAG_ELAPSED_DIST:
                                try {
                                    elapsedDist = Double.parseDouble(innerTag);
                                } catch (NumberFormatException ex) {
                                    Log.e(TAG, "CashAppData elapsedDist: NumberFormatException: ", ex);
                                }
                                break;
                            case CATAG_PAYED_DIST:
                                try {
                                    payedDist = Double.parseDouble(innerTag);
                                } catch (NumberFormatException ex) {
                                    Log.e(TAG, "CashAppData payedDist: NumberFormatException: ", ex);
                                }
                                break;
                            case CATAG_SUM_DIST:
                                try {
                                    sumElapsedDistance = new BigDecimal(innerTag).setScale(2, RoundingMode.HALF_UP);
                                } catch (NumberFormatException ex) {
                                    Log.e(TAG, "CashAppData sumElapsedDistance: NumberFormatException: ", ex);
                                }
                                break;
                            case CATAG_SUM_ADD:
                                try {
                                    sumAdditions = new BigDecimal(innerTag).setScale(2, RoundingMode.HALF_UP);
                                } catch (NumberFormatException ex) {
                                    Log.e(TAG, "CashAppData sumAdditions: NumberFormatException: ", ex);
                                }
                                break;
                            case CATAG_GENERAL_SUM:
                                try {
                                    sumTotal = new BigDecimal(innerTag).setScale(2, RoundingMode.HALF_UP);
                                } catch (NumberFormatException ex) {
                                    Log.e(TAG, "CashAppData sumTotal: NumberFormatException: ", ex);
                                }
                                break;
                            case CATAG_CONST:
                                try {
                                    cashAppConst = Integer.parseInt(innerTag);
                                } catch (NumberFormatException ex) {
                                    Log.e(TAG, "CashAppData cashAppConst: NumberFormatException: ", ex);
                                }
                                break;
                        }
                        i++;
                    }

                    str = str.substring(endIndex + 1);
                }
            } else parseError = true;
        } catch (Exception ex) {
            parseError = true;
            Log.e(TAG, "CashAppData: data parse error: ", ex);
        }

        try {
            //<><18:11:48><15-03-2013><    3><    0.000><    0.000><      0.00><      0.00><      3.03>< 4140>
            //parse dateTime
            //yyyy-MM-DD HH:MM:SS
            //19:46:45
            //03-11-2006
            if (date != null) {
                StringTokenizer token = new StringTokenizer(date, "-");

                String d = token.nextToken();
                String m = token.hasMoreTokens() ? token.nextToken() : "00";
                String y = token.hasMoreTokens() ? token.nextToken() : "0000";

                dateTime = y + "-" + m + "-" + d + " " + time;

               /* StringTokenizer timeToken = new StringTokenizer(time, ":");
                String hrs = timeToken.nextToken();
                String min = timeToken.hasMoreTokens() ? timeToken.nextToken() : "00";
                String sec = timeToken.hasMoreTokens() ? timeToken.nextToken() : "00";*/

                DateFormat df = new SimpleDateFormat("yyyy-MM-DD HH:MM:SS");
                cashAppDate = df.parse(dateTime);
                // cashAppDate = new Timestamp(Integer.parseInt(y), Integer.parseInt(m), Integer.parseInt(d), Integer.parseInt(hrs), Integer.parseInt(min), Integer.parseInt(sec), 0);
            } else parseError = true;

            if (elapsedDist == null || payedDist == null || sumElapsedDistance == null || sumAdditions == null || sumTotal == null)
                parseError = true;
        } catch (Exception ex) {
            parseError = true;
            Log.e(TAG, "CashAppData: date tokenize error: " + ex.getMessage());
        }

        driverRequests = new DriverRequests();
        driverRequests.setMode(mode);
        if (cashAppDate != null) driverRequests.setCashappDate(new Timestamp(cashAppDate.getTime()));
        driverRequests.setCourse(course);
        driverRequests.setElapsedDist(elapsedDist);
        driverRequests.setPayedDist(payedDist);
        if (sumElapsedDistance != null) driverRequests.setSumDist(sumElapsedDistance.doubleValue());
        if (sumAdditions != null) driverRequests.setSumAdd(sumAdditions.doubleValue());
        if (sumTotal != null) driverRequests.setSumTotal(sumTotal.doubleValue());
        driverRequests.setCashappConst(cashAppConst);

        return driverRequests;
    }

    private String tidyData(String strData) {
        StringBuilder out = new StringBuilder();
        boolean isTagAlreadyOpen = false;
        boolean nextTagMustBeOpen = false;
        for (int i = 0; i < strData.length(); i++) {
            String letter = strData.substring(i, i + 1);

            if (i == 0 && !letter.equals("<")) {
                out.append("<");
                isTagAlreadyOpen = true;
            } else if (isTagAlreadyOpen && letter.equals("<")) {
                out.append(">");
                isTagAlreadyOpen = false;
                nextTagMustBeOpen = true;
            }

            if (nextTagMustBeOpen) {
                if (!letter.equals("<")) out.append("<");
                nextTagMustBeOpen = false;
            }

            if (letter.equals("<")) {
                isTagAlreadyOpen = true;
            } else if (letter.equals(">")) {
                isTagAlreadyOpen = false;
                nextTagMustBeOpen = true;
            }

            out.append(letter);
        }
        //return StringUtils.trimTrailingCharacter(out.toString(), '<');
        Log.i(TAG, "Tidy: " + out.toString());
        return out.toString();
    }


    //for trace
   /* public String toString() {
        return "mode=" + mode + "\n" +
                "course=" + course + "\n" +
                "date=" + date + "\n" +
                "time=" + time + "\n" +
                "elapsedDist=" + elapsedDist + "\n" +
                "payedDist=" + payedDist + "\n" +
                "sumElapsedDistance=" + sumElapsedDistance + "\n" +
                "sumAdditions=" + sumAdditions + "\n" +
                "sumTotal=" + sumTotal + "\n" +
                "cashAppConst=" + cashAppConst;
    }*/


    //convert to STMessage
  /*  public String toMessage() {

        if (dateTime == null) {
            //init date - format yyyy-MM-DD HH:MM:SS
            ///////////////////////////////////////////////////////////////////////////
            Calendar calendar = Calendar.getInstance();
            dateTime = "" + calendar.get(Calendar.YEAR);
            int val = calendar.get(Calendar.MONTH) + 1;
            dateTime += val < 10 ? "-0" + val : "-" + val;
            val = calendar.get(Calendar.DATE);
            dateTime += val < 10 ? "-0" + val : "-" + val;

            val = calendar.get(Calendar.HOUR);
            dateTime += val < 10 ? " 0" + val : " " + val;
            val = calendar.get(Calendar.MINUTE);
            dateTime += val < 10 ? ":0" + val : ":" + val;
            val = calendar.get(Calendar.SECOND);
            dateTime += val < 10 ? ":0" + val : ":" + val;
            ///////////////////////////////////////////////////////////////////////////
        }

        return dateTime + STDef.COMMAND_DELIMITER +
                course + STDef.COMMAND_DELIMITER +
                (elapsedDist == null ? "0.0" : elapsedDist) + STDef.COMMAND_DELIMITER +
                (payedDist == null ? "0.0" : payedDist) + STDef.COMMAND_DELIMITER +
                (sumElapsedDistance == null ? "0.0" : sumElapsedDistance) + STDef.COMMAND_DELIMITER +
                (sumAdditions == null ? "0.0" : sumAdditions) + STDef.COMMAND_DELIMITER +
                (sumTotal == null ? "0.0" : sumTotal) + STDef.COMMAND_DELIMITER +
                cashAppConst;
    }*/
}