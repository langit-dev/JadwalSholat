package app.jadwal.sholat;

import java.util.Calendar;
import java.util.TimeZone;

public class PrayerTimesCalculator {
    private double latitude;
    private double longitude;
    private double timezone;

    public PrayerTimesCalculator(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timezone = getDefaultTimezone();
    }

    private double getDefaultTimezone() {
        TimeZone tz = TimeZone.getDefault();
        return tz.getRawOffset() / (1000 * 60 * 60);
    }

    public void calculatePrayerTimes() {
        double imsak = calculateImsak();
        double fajr = calculateFajr();
        double sunrise = calculateSunrise();
        double dhuhr = calculateDhuhr();
        double asr = calculateAsr();
        double maghrib = calculateMaghrib();
        double isha = calculateIsha();

        System.out.println("Imsak: " + imsak);
        System.out.println("Fajr: " + fajr);
        System.out.println("Sunrise: " + sunrise);
        System.out.println("Dhuhr: " + dhuhr);
        System.out.println("Asr: " + asr);
        System.out.println("Maghrib: " + maghrib);
        System.out.println("Isha: " + isha);
    }

    public double calculateImsak() {
        return calculateFajr() - (10 / 60.0);
    }

    public double calculateFajr() {
        return calculateTime(18);
    }

    public double calculateSunrise() {
        return calculateTime(0);
    }

    public double calculateDhuhr() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT" + timezone));
        double noon = calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE) / 60.0;
        return noon + (4 / 60.0);
    }

    public double calculateAsr() {
        return calculateTime(4);
    }

    public double calculateMaghrib() {
        return calculateTime(0);
    }

    public double calculateIsha() {
        return calculateTime(18);
    }

    private double calculateTime(double angle) {
        double D = (longitude / 15.0) + timezone;
        double T = 12 + D - (angle / 15.0);
        return T;
    }
}