package dev.mcloudtw.rf.utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtils {
    public static Date getDateWithSpecifiedTime(String time) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        Date timeDate;
        try {
            timeDate = timeFormat.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timeDate);

        Calendar current = Calendar.getInstance();
        calendar.set(current.get(Calendar.YEAR),
                current.get(Calendar.MONTH),
                current.get(Calendar.DAY_OF_MONTH));

        return calendar.getTime();
    }
}
