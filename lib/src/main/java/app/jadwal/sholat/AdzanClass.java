package app.jadwal.sholat;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import app.jadwal.sholat.AngleCalculationType;
import app.jadwal.sholat.BaseTimeAdjustmentType;
import app.jadwal.sholat.PrayerTimes;
import app.jadwal.sholat.PrayersType;
import app.jadwal.sholat.TimeAdjustment;
import app.jadwal.sholat.TimeCalculator;
import app.muslim.pedia.model.PrayerTimeModel;

public class AdzanClass {
    private Context mContext;
    private GPSTracker gpsTracker;
    private double latitude;
    private double longitude;
    private double altititude;
    private TimeAdjustment adjustments;
    private TimeCalculator timecalc;
    private PrayerTimes prayerTimes;
    private GregorianCalendar gregorianCalendar;
    private String WaktuImsak;
    private String WaktuSubuh;
    private String WaktuTerbit;
    private String WaktuDzuhur;
    private String WaktuAshar;
    private String WaktuMaghrib;
    private String WaktuIsya;
    private Calendar uCal;
    SimpleDateFormat sdfjam;
    SimpleDateFormat formatterid;
    public String formattglarab;
    public String formattgllokal;
    public Calendar waktu_now;
    public String var_daerah, var_kota, var_negara;
    public Calendar waktu_alarm_last;
    public String alarmadzanskg;
    public Calendar waktu_alarm;
    public int waktushalat;
    public String waktuadzanskg;
    public String waktuadzannextshort;
    private int tDeltainminutes;
    private int tDeltainminutes_min15;
    public String waktuatasjudul;
    public String waktuatastimemin;
    public String waktuatastime_min;
    public String wnownextjudul;
    public String waktuatastime_h;
    public String waktuatastimehour;

    public AdzanClass(Context context) {
        this.mContext = context;
        this.waktu_now = Calendar.getInstance();
        this.uCal = new UmmalquraCalendar();
        this.gpsTracker = new GPSTracker(context);
        this.latitude = gpsTracker.getLatitude();
        this.longitude = gpsTracker.getLongitude();
        this.altititude = gpsTracker.getAltitude();
        this.gregorianCalendar = new GregorianCalendar();
        this.sdfjam = new SimpleDateFormat("HH:mm");
        this.formatterid = new SimpleDateFormat("dd MMMM yyyy");
        this.adjustments = new TimeAdjustment(BaseTimeAdjustmentType.TWO_MINUTES_ADJUSTMENT);
        this.timecalc = new TimeCalculator();
        timecalc.timeCalculationMethod(AngleCalculationType.MUHAMMADIYAH, false, adjustments);
        calculate(gregorianCalendar);
        getTimePray();
        getjadwalshalat();
    }

    private void calculate(GregorianCalendar gregorianCalendars) {
        prayerTimes = timecalc
                .date(gregorianCalendars)
                .dateRelative(0)
                .location(latitude, longitude, altititude, 0)
                .calculateTimes();
        prayerTimes.setUseSecond(false);
    }

    private void getTimePray() {
        if (prayerTimes != null) {
            Date fajrToday = prayerTimes.getPrayTime(PrayersType.FAJR);
            Calendar waktu_subuh_hari_ini = Calendar.getInstance();
            waktu_subuh_hari_ini.setTime(fajrToday);
            Calendar waktu_imsak = (Calendar) waktu_subuh_hari_ini.clone();
            waktu_imsak.add(Calendar.MINUTE, -10);
            WaktuImsak = sdfjam.format(waktu_imsak.getTime());
            WaktuSubuh = sdfjam.format(prayerTimes.getPrayTime(PrayersType.FAJR));
            WaktuTerbit = sdfjam.format(prayerTimes.getPrayTime(PrayersType.SUNRISE));
            WaktuDzuhur = sdfjam.format(prayerTimes.getPrayTime(PrayersType.ZUHR));
            WaktuAshar = sdfjam.format(prayerTimes.getPrayTime(PrayersType.ASR));
            WaktuMaghrib = sdfjam.format(prayerTimes.getPrayTime(PrayersType.MAGHRIB));
            WaktuIsya = sdfjam.format(prayerTimes.getPrayTime(PrayersType.ISHA));
        }
    }

    public List<PrayerTimeModel> prayerTimeModelList() {
        List<PrayerTimeModel> list = new ArrayList<>();
        String[] prayerName = { "Imsak", "Subuh", "Terbit", "Dzuhur", "Ashar", "Maghrib", "Isya" };
        String[] waktuSholat = { WaktuImsak, WaktuSubuh, WaktuTerbit, WaktuDzuhur, WaktuAshar, WaktuMaghrib,
                WaktuIsya };
        boolean active;
        for (int i = 0; i < prayerName.length; i++) {
            if (prayerName[i].equals(waktuadzannextshort)) {
                active = true;
            } else {
                active = false;
            }
            PrayerTimeModel prayerTimeModel = new PrayerTimeModel();
            prayerTimeModel.setName(prayerName[i]);
            prayerTimeModel.setTime(waktuSholat[i]);
            prayerTimeModel.setActive(active);
            list.add(prayerTimeModel);
        }
        return list;
    }

    public String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + c;
    }

    public String getAddress() {
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                var_daerah = addresses.get(0).getLocality();
                var_negara = addresses.get(0).getAdminArea();
                var_kota = addresses.get(0).getSubAdminArea();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String daerah = var_daerah.replace("Kecamatan ", "");
        String kota = var_kota.replace("Kabupaten ", "");
        return daerah + ", " + kota + ", " + var_negara;
    }

    public void getjadwalshalat() {
        // ambil jadwal isya hari kemarin
        waktu_alarm_last = Calendar.getInstance();
        waktu_alarm_last.add(Calendar.DAY_OF_MONTH, -1);
        calculate(new GregorianCalendar(
                waktu_alarm_last.get(Calendar.YEAR),
                waktu_alarm_last.get(Calendar.MONTH),
                waktu_alarm_last.get(Calendar.DAY_OF_MONTH),
                waktu_alarm_last.get(Calendar.HOUR_OF_DAY),
                waktu_alarm_last.get(Calendar.MINUTE),
                waktu_alarm_last.get(Calendar.SECOND)));
        waktu_alarm_last.setTime(prayerTimes.getPrayTime(PrayersType.ISHA));
        waktu_alarm_last.set(Calendar.SECOND, 0);
        waktu_alarm_last.set(Calendar.MILLISECOND, 0);
        alarmadzanskg = sdfjam.format(prayerTimes.getPrayTime(PrayersType.ISHA));

        // ambil jadwal subuh hari ini
        calculate(new GregorianCalendar());
        waktu_alarm = Calendar.getInstance();
        // ambil imsak
        Date fajrToday = prayerTimes.getPrayTime(PrayersType.FAJR);
        Calendar waktu_subuh_hari_ini = Calendar.getInstance();
        waktu_subuh_hari_ini.setTime(fajrToday);
        Calendar waktu_imsak = (Calendar) waktu_subuh_hari_ini.clone();
        waktu_imsak.add(Calendar.MINUTE, -10);

        waktu_alarm.setTime(waktu_imsak.getTime());
        waktu_alarm.set(Calendar.SECOND, 0);
        waktu_alarm.set(Calendar.MILLISECOND, 0);

        waktu_now = Calendar.getInstance();
        waktu_now.set(Calendar.SECOND, 20);
        waktu_now.set(Calendar.MILLISECOND, 0);

        waktushalat = 6;
        waktuadzanskg = "Imsak";
        waktuadzannextshort = "Subuh";

        if (waktu_alarm.before(waktu_now)) {
            waktushalat = 1;
            waktuadzanskg = "Imsak";
            waktuadzannextshort = "Subuh";
            alarmadzanskg = sdfjam.format(waktu_imsak.getTime());

            waktu_alarm.setTime(prayerTimes.getPrayTime(PrayersType.FAJR));
            waktu_alarm_last.setTime(waktu_imsak.getTime());
            waktu_alarm.set(Calendar.SECOND, 0);
            waktu_alarm.set(Calendar.MILLISECOND, 0);
            waktu_alarm_last.set(Calendar.SECOND, 0);
            waktu_alarm_last.set(Calendar.MILLISECOND, 0);
            if (waktu_alarm.before(waktu_now)) {
                waktushalat = 2;
                waktuadzanskg = "Subuh";
                waktuadzannextshort = "Terbit";
                alarmadzanskg = sdfjam.format(prayerTimes.getPrayTime(PrayersType.FAJR));

                waktu_alarm.setTime(prayerTimes.getPrayTime(PrayersType.SUNRISE));
                waktu_alarm_last.setTime(prayerTimes.getPrayTime(PrayersType.FAJR));
                waktu_alarm.set(Calendar.SECOND, 0);
                waktu_alarm.set(Calendar.MILLISECOND, 0);
                waktu_alarm_last.set(Calendar.SECOND, 0);
                waktu_alarm_last.set(Calendar.MILLISECOND, 0);
                if (waktu_alarm.before(waktu_now)) {
                    waktushalat = 3;
                    waktuadzanskg = "Terbit";
                    waktuadzannextshort = "Dzuhur";
                    alarmadzanskg = sdfjam.format(prayerTimes.getPrayTime(PrayersType.SUNRISE));

                    waktu_alarm.setTime(prayerTimes.getPrayTime(PrayersType.ZUHR));
                    waktu_alarm_last.setTime(prayerTimes.getPrayTime(PrayersType.SUNRISE));
                    waktu_alarm.set(Calendar.SECOND, 0);
                    waktu_alarm.set(Calendar.MILLISECOND, 0);
                    waktu_alarm_last.set(Calendar.SECOND, 0);
                    waktu_alarm_last.set(Calendar.MILLISECOND, 0);

                    if (waktu_alarm.before(waktu_now)) {
                        waktushalat = 4;
                        waktuadzanskg = "Dzuhur";
                        waktuadzannextshort = "Ashar";
                        alarmadzanskg = sdfjam.format(prayerTimes.getPrayTime(PrayersType.ZUHR));

                        waktu_alarm.setTime(prayerTimes.getPrayTime(PrayersType.ASR));
                        waktu_alarm_last.setTime(prayerTimes.getPrayTime(PrayersType.ZUHR));
                        waktu_alarm.set(Calendar.SECOND, 0);
                        waktu_alarm.set(Calendar.MILLISECOND, 0);
                        waktu_alarm_last.set(Calendar.SECOND, 0);
                        waktu_alarm_last.set(Calendar.MILLISECOND, 0);
                        if (waktu_alarm.before(waktu_now)) {
                            waktushalat = 5;
                            waktuadzanskg = "Ashar";
                            waktuadzannextshort = "Maghrib";
                            alarmadzanskg = sdfjam.format(prayerTimes.getPrayTime(PrayersType.ASR));

                            waktu_alarm.setTime(prayerTimes.getPrayTime(PrayersType.MAGHRIB));
                            waktu_alarm_last.setTime(prayerTimes.getPrayTime(PrayersType.ASR));
                            waktu_alarm.set(Calendar.SECOND, 0);
                            waktu_alarm.set(Calendar.MILLISECOND, 0);
                            waktu_alarm_last.set(Calendar.SECOND, 0);
                            waktu_alarm_last.set(Calendar.MILLISECOND, 0);
                            if (waktu_alarm.before(waktu_now)) {
                                waktushalat = 6;
                                waktuadzanskg = "Maghrib";
                                waktuadzannextshort = "Isya";
                                alarmadzanskg = sdfjam.format(prayerTimes.getPrayTime(PrayersType.MAGHRIB));
                                waktu_alarm.setTime(prayerTimes.getPrayTime(PrayersType.ISHA));
                                waktu_alarm_last.setTime(prayerTimes.getPrayTime(PrayersType.MAGHRIB));

                                waktu_alarm.set(Calendar.SECOND, 0);
                                waktu_alarm.set(Calendar.MILLISECOND, 0);
                                waktu_alarm_last.set(Calendar.SECOND, 0);
                                waktu_alarm_last.set(Calendar.MILLISECOND, 0);
                                if (waktu_alarm.before(waktu_now)) {
                                    waktushalat = 7;
                                    waktuadzanskg = "Isya";
                                    waktuadzannextshort = "Imsak";
                                    alarmadzanskg = sdfjam.format(prayerTimes.getPrayTime(PrayersType.ISHA));
                                    waktu_alarm_last.setTime(prayerTimes.getPrayTime(PrayersType.ISHA));

                                    waktu_alarm = Calendar.getInstance();
                                    waktu_alarm.add(Calendar.DAY_OF_MONTH, 1);
                                    calculate(new GregorianCalendar(
                                            waktu_alarm.get(Calendar.YEAR),
                                            waktu_alarm.get(Calendar.MONTH),
                                            waktu_alarm.get(Calendar.DAY_OF_MONTH),
                                            waktu_alarm.get(Calendar.HOUR_OF_DAY),
                                            waktu_alarm.get(Calendar.MINUTE),
                                            waktu_alarm.get(Calendar.SECOND)));
                                    Date fajrTodayEsok = prayerTimes.getPrayTime(PrayersType.FAJR);
                                    Calendar waktu_subuh_esok = Calendar.getInstance();
                                    waktu_subuh_esok.setTime(fajrTodayEsok);
                                    Calendar waktu_imsak_esok = (Calendar) waktu_subuh_esok.clone();
                                    waktu_imsak_esok.add(Calendar.MINUTE, -10);
                                    waktu_alarm.setTime(waktu_imsak_esok.getTime());

                                    waktu_alarm.set(Calendar.SECOND, 0);
                                    waktu_alarm.set(Calendar.MILLISECOND, 0);
                                    waktu_alarm_last.set(Calendar.SECOND, 0);
                                    waktu_alarm_last.set(Calendar.MILLISECOND, 0);
                                }

                            }
                        }
                    }
                }
            }
        }
        tDeltainminutes = (int) (((waktu_now.getTimeInMillis() - waktu_alarm_last.getTimeInMillis()) / 1000) / 60);
        if (((tDeltainminutes <= 30) && (waktushalat == 1 || waktushalat == 4))
                || ((tDeltainminutes <= 59) && (waktushalat != 1 && waktushalat != 4))) {
            waktuatasjudul = "Lewat waktu " + waktuadzanskg;
            waktuatastime_h = "";
            waktuatastimehour = "";
            waktuatastimemin = "+ " + pad(tDeltainminutes);
            waktuatastime_min = "menit";
            wnownextjudul = "Sekarang";
        } else {
            waktuatasjudul = "Menuju waktu " + waktuadzannextshort;
            wnownextjudul = "Berikutnya";
            tDeltainminutes = (int) (((waktu_alarm.getTimeInMillis() - waktu_now.getTimeInMillis()) / 1000) / 60);
            Calendar waktu_syuruq = Calendar.getInstance();
            waktu_syuruq.setTime(prayerTimes.getPrayTime(PrayersType.SUNRISE));
            waktu_syuruq.add(Calendar.MINUTE, 15);

            tDeltainminutes_min15 = (int) (((waktu_syuruq.getTimeInMillis() - waktu_now.getTimeInMillis()) / 1000)
                    / 60);
            if ((waktuatasjudul.equals("Dzuhur") && (tDeltainminutes_min15 <= 15 && tDeltainminutes_min15 >= 0))) {
                waktuatastimemin = "+ " + pad(15 - tDeltainminutes_min15);
                waktuatastime_min = "menit";
                waktuatasjudul = "Terbit";
                wnownextjudul = "Dilarang Sholat";
                waktuatastime_h = "";
                waktuatastimehour = "";
            } else {
                int Hours = tDeltainminutes / 60;
                int Minutes = tDeltainminutes % 60;
                if (tDeltainminutes < 0) {
                    Hours = 0;
                    Minutes = 0;
                }
                if (Hours == 0) {
                    if (Minutes == 0) {
                        waktuatastime_h = "";
                        waktuatastimehour = "";
                        waktuatastimemin = "+ 00";
                        waktuatastime_min = "menit";
                    } else {
                        waktuatastime_h = "";
                        waktuatastimehour = "";
                        waktuatastimemin = "- " + pad(Minutes);
                        waktuatastime_min = "menit";
                    }
                } else {
                    waktuatastime_h = "jam ";
                    waktuatastime_min = "menit";
                    waktuatastimehour = "- " + pad(Hours);
                    waktuatastimemin = pad(Minutes);
                }
            }
        }
    }
}
