package pt.gu.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class CameraUtils {

    public interface Callback {

        Activity getActivity();
        void onOpenCamera(CameraDevice camera);
        void onCloseCamera(CameraDevice camera);
        void onError(String err);
        void onDebug(String debug);

    }

    private static final String TAG = CameraUtils.class.getSimpleName();

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private final Callback mCallback;

    public CameraUtils(@NonNull Callback callback){
        mCallback = callback;    
    }

    public void openCamera(@Nullable String cameraId){
        Activity a = mCallback.getActivity();
        CameraManager manager = (CameraManager) a.getSystemService(Context.CAMERA_SERVICE);
        mCallback.onDebug("Opening camera " + cameraId);
        try {
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(a, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(a, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(a, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(CameraDevice camera) {
                    mCallback.onDebug("onOpened camera "+cameraId);
                    mCallback.onOpenCamera(camera);
                    cameraDevice = camera;
                }

                @Override
                public void onDisconnected(CameraDevice camera) {
                    mCallback.onDebug("onDisconnected camera "+cameraId);
                    mCallback.onCloseCamera(camera);
                }

                @Override
                public void onError(CameraDevice camera, int error) {
                    cameraDevice.close();
                    cameraDevice = null;
                }
            }, null);
        } catch (CameraAccessException e) {
            mCallback.onError(e.toString());
        }
        Log.e(TAG, "openCamera X");
    }


}