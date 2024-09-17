package app.jadwal.sholat;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import static app.jadwal.sholat.AngleCalculationType.UMM_AL_QURA;
import static app.jadwal.sholat.BaseTimeAdjustmentType.TWO_MINUTES_ZUHR;
import static app.jadwal.sholat.Constants.ASR_RATIO_HANAFI;
import static app.jadwal.sholat.Constants.ASR_RATIO_MAJORITY;
import static app.jadwal.sholat.Constants.HOURS_IN_DAY;
import static app.jadwal.sholat.Constants.MID_DAY_HOUR;
import static app.jadwal.sholat.Constants.MILLIS_IN_SECOND;
import static app.jadwal.sholat.Constants.MINUTE_IN_HOUR;
import static app.jadwal.sholat.Constants.MINUTE_IN_HOUR_DOUBLE;
import static app.jadwal.sholat.Constants.SECOND_IN_MINUTE;
import static app.jadwal.sholat.JulianDayUtil.adjustJdHour;
import static app.jadwal.sholat.PrayerCalculator.asr;
import static app.jadwal.sholat.PrayerCalculator.fajr;
import static app.jadwal.sholat.PrayerCalculator.isha;
import static app.jadwal.sholat.PrayerCalculator.maghrib;
import static app.jadwal.sholat.PrayerCalculator.sunrise;
import static app.jadwal.sholat.PrayerCalculator.zuhr;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class TimeCalculator {

    /**
     * Julian Day of 1970-01-01 midday.
     */
    private static final double JAVA_DATE_EPOCH_JD = 2440588;
    private static final double UMM_AL_QURA_RAMADAN_ISHA_ADJUSTMENT = 2;
    private static final double UMM_AL_QURA_ISHA_ADJUSTMENT = 1.5;

    private AngleCalculationType angle;
    private int asrRatio;
    private TimeAdjustment adjustments;
    private double latitude;
    private double longitude;
    private double height;
    private Double timezone;
    private Double julianDay;
    private boolean umElQuraRamadanAdjustment;

    /**
     * Like calling
     * {@code timeCalculationMethod(angle, false, TimeAdjustment.TWO_MINUTES_ZUHR)}.
     *
     * @param angle
     * @return
     */
    public TimeCalculator timeCalculationMethod(AngleCalculationType angle) {
        TimeAdjustment twoMinutesZuhrAdjustment = new TimeAdjustment(TWO_MINUTES_ZUHR);
        return timeCalculationMethod(angle, false, twoMinutesZuhrAdjustment);
    }

    /**
     * Set timeCalculationMethod and adjustment of calculation.
     *
     * @param angle          Fajr and Isha angle
     * @param hanafiAsrRatio ratio of object's shadow to determine Asr time, whether
     *                       Hanafi or majority
     * @param adjustments    result adjustment
     * @return self for chaining
     */
    public TimeCalculator timeCalculationMethod(AngleCalculationType angle, boolean hanafiAsrRatio,
            TimeAdjustment adjustments) {
        this.angle = angle;
        this.asrRatio = hanafiAsrRatio ? ASR_RATIO_HANAFI : ASR_RATIO_MAJORITY;
        this.adjustments = adjustments;
        return this;
    }

    public TimeCalculator umElQuraRamadanAdjustment(boolean umElQuraRamadanAdjustment) {
        this.umElQuraRamadanAdjustment = umElQuraRamadanAdjustment;
        return this;
    }

    /**
     * Set the location.
     *
     * @param latitude  latitude in degrees
     * @param longitude longitude in degrees
     * @param height    altitude/height of the place in meters
     * @param timezone  timezone in hours, x means UTC+x
     * @return self for chaining
     */
    public TimeCalculator location(double latitude, double longitude, double height, double timezone) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.height = height;
        this.timezone = timezone;
        return this;
    }

    /**
     * Set the date.
     *
     * @param date
     * @return self for chaining
     */
    public TimeCalculator date(GregorianCalendar date) {
        this.julianDay = JulianDayUtil.gregorianToJulianDay(date.get(YEAR),
                date.get(MONTH) + 1, date.get(DAY_OF_MONTH));
        return this;
    }

    public TimeCalculator date(Date date, TimeZone zone) {
        GregorianCalendar g = new GregorianCalendar(zone);
        g.setTime(date);
        return date(g);
    }

    /**
     * Add the date by days.
     *
     * @param days
     * @return
     */
    public TimeCalculator dateRelative(int days) {
        this.julianDay += days;
        return this;
    }

    /**
     * Calculate the prayer times.
     * <p>
     * This timeCalculationMethod can be called several times. For example, you set
     * the date
     * and call this timeCalculationMethod, update the date to tomorrow and call
     * this timeCalculationMethod.
     */
    public PrayerTimes calculateTimes() {
        if (angle == null || julianDay == null || timezone == null)
            throw new IllegalStateException("Some calculation parameter is not initialized yet");
        // julian day of local midday (minus timezone, plus 12 hours)
        double julianDay = adjustJdHour(this.julianDay, MID_DAY_HOUR - this.timezone);
        double declinationDegrees = JulianDayUtil.sunDeclinationDegrees(julianDay);
        double transit = zuhr(this.longitude, this.timezone,
                JulianDayUtil.calculateTimeUponGeolocationPoint(julianDay));
        double latitude = this.latitude;
        double isha = isha(transit, latitude, declinationDegrees, angle.getIshaAngle());
        if (UMM_AL_QURA == angle) {
            double ishaAdjustValue = umElQuraRamadanAdjustment ? UMM_AL_QURA_RAMADAN_ISHA_ADJUSTMENT
                    : UMM_AL_QURA_ISHA_ADJUSTMENT;
            isha = maghrib(transit, latitude, declinationDegrees, this.height) + ishaAdjustValue;
        }
        PrayerTimes prayerTimes = new PrayerTimes(
                (long) (julianDay - JAVA_DATE_EPOCH_JD) * HOURS_IN_DAY * MINUTE_IN_HOUR *
                        SECOND_IN_MINUTE * MILLIS_IN_SECOND,
                fajr(transit, latitude, declinationDegrees, angle.getFajrAngle()) + adjustments
                        .getFajr() / MINUTE_IN_HOUR_DOUBLE,
                sunrise(transit, latitude, declinationDegrees, this.height)
                        + adjustments.getSunrise() / MINUTE_IN_HOUR_DOUBLE,
                transit + adjustments.getZuhr() / MINUTE_IN_HOUR_DOUBLE,
                asr(transit, latitude, declinationDegrees, this.asrRatio)
                        + adjustments.getAsr() / MINUTE_IN_HOUR_DOUBLE,
                maghrib(transit, latitude, declinationDegrees, this.height)
                        + adjustments.getMaghrib() / MINUTE_IN_HOUR_DOUBLE,
                isha + adjustments.getIsha() / MINUTE_IN_HOUR_DOUBLE);
        return prayerTimes;
    }
}