package app.jadwal.sholat;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static app.jadwal.sholat.Constants.BASE_HOUR_ANGLE;
import static app.jadwal.sholat.Constants.MID_DAY_HOUR;
import static app.jadwal.sholat.Constants.SUN_ALTITUDE_NIGHT_CONSTANT;
import static app.jadwal.sholat.Constants.SUN_ALTITUDE_RATIO;
import static app.jadwal.sholat.JulianDayUtil.hourAngle;
import static app.jadwal.sholat.MathUtil.acotDeg;
import static app.jadwal.sholat.MathUtil.tanDeg;

public class PrayerCalculator {

    public static double zuhr(double longitude, double timeZone, double timeAtGeolocationPoint) {
        return MID_DAY_HOUR + timeZone - longitude / BASE_HOUR_ANGLE - timeAtGeolocationPoint;
    }

    public static double asr(double zuhrBaseTime, double latitude, double declinationDegrees, double asrRatio) {
        double sunAltitude = acotDeg(asrRatio + tanDeg(abs(declinationDegrees - latitude))); // altitude of the sun
        return zuhrBaseTime + hourAngle(latitude, sunAltitude, declinationDegrees) / BASE_HOUR_ANGLE;
    }

    public static double maghrib(double zuhrBaseTime, double latitude, double declinationDegrees, double height) {
        double sunAltitude = SUN_ALTITUDE_NIGHT_CONSTANT - SUN_ALTITUDE_RATIO * sqrt(height);
        return zuhrBaseTime + hourAngle(latitude, sunAltitude, declinationDegrees) / BASE_HOUR_ANGLE;
    }

    public static double isha(double zuhrBaseTime, double latitude, double declinationDegrees, double ishaAngle) {
        double sunAltitude = -ishaAngle;
        return zuhrBaseTime + hourAngle(latitude, sunAltitude, declinationDegrees) / BASE_HOUR_ANGLE;
    }

    public static double fajr(double zuhrBaseTime, double latitude, double declinationDegrees, double fajrAngle) {
        double sunAltitude = -fajrAngle;
        return zuhrBaseTime - hourAngle(latitude, sunAltitude, declinationDegrees) / BASE_HOUR_ANGLE;
    }

    public static double sunrise(double zuhrBaseTime, double latitude, double declinationDegrees, double height) {
        double sunAltitude = SUN_ALTITUDE_NIGHT_CONSTANT - SUN_ALTITUDE_RATIO * sqrt(height); // equals to
                                                                                              // getMaghribAdjustment
        return zuhrBaseTime - hourAngle(latitude, sunAltitude, declinationDegrees) / BASE_HOUR_ANGLE;
    }
}
