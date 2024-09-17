package app.jadwal.sholat;

public enum AngleCalculationType {

    MWL(18, 17),
    ISNA(15, 15),
    EGYPT(19.5, 17.5),
    KARACHI(18, 18),
    MUHAMMADIYAH(20, 18),
    UOIF(12, 12),
    KUWAIT(18, 17.5),
    UMM_AL_QURA(18.5, 19);

    private double fajr, isha;

    AngleCalculationType(double fajr, double isha) {
        this.fajr = fajr;
        this.isha = isha;
    }

    public double getFajrAngle() {
        return fajr;
    }

    public double getIshaAngle() {
        return isha;
    }

}