/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package seeds.dbconfig.Util;

import java.security.MessageDigest;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author Administrator
 */
public class Utils {

// Create Current System Date   
    public long Time9() throws ParseException {
        long dt = getDateFormatTM(getDateFormater(new Date()) + " 09:00:00").getTime();
        return dt;

    }

    public String DateIn() {
        String date11 = "";
        Format formatter;
        Date date1 = new Date();
        formatter = new SimpleDateFormat("yyyy-MM-dd");
        date11 = formatter.format(date1);
        return date11;
    }
    
    public String DateIn1() {
        String date11 = "";
        Format formatter;
        Date date1 = new Date();
        formatter = new SimpleDateFormat("dd-MM-yyyy");
        date11 = formatter.format(date1);
        return date11;
    }
    
    public String DateIn2() {
        String date11 = "";
        Format formatter;
        Date date1 = new Date();
        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        date11 = formatter.format(date1);
        return date11;
    }

    public Date getDateFormat(String date) throws ParseException {
        DateFormat formatter;
        Date newDate;
        formatter = new SimpleDateFormat("yyyy-MM-dd");
        newDate = (Date) formatter.parse(date);
        return newDate;
    }
    
    public String getDateFormat1(String date) throws ParseException {
        DateFormat formatter;
        Date newDate;
        formatter = new SimpleDateFormat("yyyy-MM-dd");
        newDate = (Date) formatter.parse(date);
        SimpleDateFormat newFormat = new SimpleDateFormat("dd-MM-yyyy");
        String finalString = newFormat.format(newDate);
        return finalString;
    }
    
    public String getDateFormat2(String date) throws ParseException {
        DateFormat formatter;
        Date newDate;
        formatter = new SimpleDateFormat("dd-MM-yyyy");
        newDate = (Date) formatter.parse(date);
        SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd");
        String finalString = newFormat.format(newDate);
        return finalString;
    }
    
    public String getDateFormat3(String date) throws ParseException {
        DateFormat formatter;
        Date newDate;
        formatter = new SimpleDateFormat("yyyy-MM-dd");
        newDate = (Date) formatter.parse(date);
        SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd");
        String finalString = newFormat.format(newDate);
        return finalString;
    }

    public String getYear(Date date) throws ParseException {
        String date11 = "";
        Format formatter;
        Date date1 = date;
        formatter = new SimpleDateFormat("yyyy");
        date11 = formatter.format(date1);
        return date11;
    }

    public Date getDateFormatTM(String date) throws ParseException {
        DateFormat formatter;
        Date newDate;
        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        newDate = (Date) formatter.parse(date);
        return newDate;
    }

    public int monthCount(String dat1, String dat2) {

        String[] slipt1 = dat1.split("-");

        int yy1 = Integer.parseInt(slipt1[0]);
        int mm1 = Integer.parseInt(slipt1[1]);
        int dd1 = Integer.parseInt(slipt1[2]);

        String[] slipt2 = dat2.split("-");
        int yy2 = Integer.parseInt(slipt2[0]);
        int mm2 = Integer.parseInt(slipt2[1]);
        int dd2 = Integer.parseInt(slipt2[2]);
        Calendar firstDate = Calendar.getInstance();
        firstDate.set(yy1, mm1, dd1);
        Calendar secondDate = Calendar.getInstance();
        secondDate.set(yy2, mm2, dd2);
        int months = (firstDate.get(Calendar.YEAR) - secondDate.get(Calendar.YEAR)) * 12
                + (firstDate.get(Calendar.MONTH) - secondDate.get(Calendar.MONTH))
                + (firstDate.get(Calendar.DAY_OF_MONTH) >= secondDate.get(Calendar.DAY_OF_MONTH) ? 0 : -1);
        return months;
    }

    public String YearIn() {
        String date11 = "";
        Format formatter;
        Date date1 = new Date();
        formatter = new SimpleDateFormat("yyyy");
        date11 = formatter.format(date1);
        return date11;
    }

    public String YearIn1() {
        String date11 = "";
        Format formatter;
        Date date1 = new Date();
        formatter = new SimpleDateFormat("yy");
        date11 = formatter.format(date1);
        return date11;
    }

    public List getDatesInRange(Date startDate, Date endDate, boolean includeStartDate) throws Exception {

        if (!endDate.after(startDate)) {
            throw new Exception("INVALID DATE RANGE: end date should be after start date");
        }

        final long ONE_DAY = 24 * 60 * 60 * 1000;
        long startTime = startDate.getTime();
        long endTime = endDate.getTime();

        List dateRange = new ArrayList();

        if (includeStartDate) {
            dateRange.add(startDate);
        }

        for (long currentTime = startTime;
                currentTime < endTime;
                currentTime++) {
            currentTime += ONE_DAY;

            Date date = new Date(currentTime);
            dateRange.add(date);

        }

        return dateRange;
    }
// Create Current System Time   

    public String TimeIn() {
        String time;
        Format formatter;
        Date date = new Date();
        formatter = new SimpleDateFormat("HH:mm:ss");
        time = formatter.format(date);
        return time;

    }

    public String TimeFormat(Date date) {
        String time;
        Format formatter;

        formatter = new SimpleDateFormat("hh:mm:ss a");
        time = formatter.format(date);
        return time;

    }

    public boolean TimeCheck(String date) throws ParseException {
        boolean time = false;
        long dt = getDateFormatTM(getDateFormater(getDateFormat(date)) + " 09:30:00.000").getTime();
        long dt1 = getDateFormatTM(date).getTime();
        //long dt1=formatter.parse(cur).getTime();  
        if (dt < dt1) {
            return true;
        }
        return time;

    }

    public String CurntTime() {
        String date11 = "";
        Format formatter;
        Date date1 = new Date();
        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.000");
        date11 = formatter.format(date1);
        return date11;
    }

    public String RepGrTime() {
        String date11 = "";
        Format formatter;
        Date date1 = new Date();
        formatter = new SimpleDateFormat("yyyy-MM-dd 09:30:00.000");
        date11 = formatter.format(date1);
        return date11;
    }

    public String getMonth(Date date1) {
        String date11 = "0";
        Format formatter;
        formatter = new SimpleDateFormat("MM");
        date11 = formatter.format(date1);
        return date11;
    }

    public String getJoinDay(Date date1) {
        String date11 = "0";
        Format formatter;
        formatter = new SimpleDateFormat("dd");
        date11 = formatter.format(date1);
        return date11;
    }

    public String getDuration(String in, String out) throws ParseException {
        String dt1 = out;
        String dt2 = in;
        String duration = "";
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Long dur = null;
            if (df.parse(dt1).getTime() > df.parse(dt2).getTime()) {
                dur = df.parse(dt1).getTime() - df.parse(dt2).getTime();
            } else {
                dur = df.parse(dt2).getTime() - df.parse(dt1).getTime();
            }
            Long h = dur / (60 * 60 * 1000);
            Long m = (dur / (60 * 1000)) % 60;;
            Long s = (dur / 1000) % 60;
            String hr = h.toString();
            String mnt = m.toString();
            String ss = s.toString();
            if (h < 10) {
                hr = "0" + hr;
            }
            if (m < 10) {
                mnt = "0" + m;

            }

            duration = hr + ":" + mnt;
        } catch (Exception e) {
        }
        return duration;

    }

    public String getDateFormatTM1(Date date) throws ParseException {
        DateFormat formatter;
        String newDate;
        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        newDate = formatter.format(date);
        return newDate;
    }

    public String getDateFormater(Date date1) throws ParseException {
        DateFormat formatter;
        Date newDate;
        formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(date1);
    }

    public String getEncript(String EncriptMe) {
        String passw = "";
        String password = EncriptMe;
        byte[] unencodedPassword = password.getBytes();
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
        }
        md.reset();
        md.update(unencodedPassword);
        byte[] encodedPassword = md.digest();
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < encodedPassword.length; i++) {
            if (((int) encodedPassword[i] & 0xff) < 0x10) {
                buf.append("0");
            }
            buf.append(Long.toString((int) encodedPassword[i] & 0xff, 16));
        }
        passw = buf.toString();
        return passw;
    }

    public String combine(List<String> s, String glue) {
        int k = s.size();
        if (k == 0) {
            return null;
        }
        StringBuilder out = new StringBuilder();
        Iterator it = s.iterator();
        int j = 0;
        while (it.hasNext()) {

            if (j == 0) {
                out.append(it.next());
            } else {
                out.append(glue).append(it.next());
            }
            j++;
        }
        return out.toString();
    }

    public static void main(String[] args) throws ParseException {
        Utils u = new Utils();
        java.util.Date date = new java.util.Date();
        System.out.println(u.getEncript("ramesh"));
    }

    public long getTimeMilli(String time) throws ParseException {
        DateFormat formater;
        String date = "2013-09-03";
        formater = new SimpleDateFormat("2013-09-03 " + time + ":00.000");
        long strDate = getDateFormatTM(formater.format(new Date())).getTime();
        return strDate;
    }

    public boolean getTimeMilli1(String in, String sftBgn) throws ParseException {
        boolean a = false;
        long inTime = getTimeMilli(in);
        long sftTime = getTimeMilli(sftBgn);
        if (sftTime < inTime) {
            a = true;
        }
        return a;

    }

    public Timestamp tstamp() {
        java.util.Date date = new java.util.Date();
        return new Timestamp(date.getTime());
    }
}
