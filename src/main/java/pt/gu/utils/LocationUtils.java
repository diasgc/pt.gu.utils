package pt.gu.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class LocationUtils {

    public static void getCurrentLocation(Context context, Consumer<Location> c){
        if (c != null && context != null && context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager m = context.getSystemService(LocationManager.class);
            m.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    c.accept(location);
                }
            },context.getMainLooper());
        }
    }

    public enum Format {
        GPS_dec4, ADDR
    }

    public static String format(Context context, @NonNull Location location, @NonNull Format f){
        if (f == Format.ADDR && context != null){
            Geocoder g = new Geocoder(context, Locale.getDefault());
            try {
                final StringBuilder sb = new StringBuilder();
                final Address address;
                List<Address> addressList = g.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                if (addressList != null && addressList.size() > 0 && (address = addressList.get(0)) != null) {
                    sb.append(address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) + ", " : "")
                            // append locality, always
                            .append(address.getLocality())
                            // append country, if different from default locale
                            .append(Locale.getDefault().equals(address.getLocale()) ? "" : ", "+address.getCountryName());
                    return sb.toString();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            f = Format.GPS_dec4;
        }
        if (f == Format.GPS_dec4)
            return String.format(Locale.US, "%+10.4f%+10.4f", location.getLatitude(), location.getLongitude());
        return "unknown location";
    }
}
