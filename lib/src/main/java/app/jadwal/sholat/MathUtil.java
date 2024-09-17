package app.jadwal.sholat;

import static java.lang.Math.acos;
import static java.lang.Math.atan;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.tan;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

public class MathUtil {
    public static double sinDeg(double deg) {
        return sin(toRadians(deg));
    }

    public static double cosDeg(double deg) {
        return cos(toRadians(deg));
    }

    public static double acosDeg(double x_r) {
        return toDegrees(acos(x_r));
    }

    public static double tanDeg(double deg) {
        return tan(toRadians(deg));
    }

    public static double acotDeg(double x_y) {
        return toDegrees(atan(1.0 / x_y));
    }

    public static double atan2Deg(double y, double x) {
        return toDegrees(atan2(y, x));
    }
}
