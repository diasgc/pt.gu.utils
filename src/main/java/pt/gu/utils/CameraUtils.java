package pt.gu.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class CameraUtils {

    public interface Callback {

        float AF_AUTO = -1.0;
        float AF_INFINITY = 0.0;
        float AE_AUTO = -1.0;
        
        Activity getActivity();
        
        void onOpenCamera(CameraCharacteristics characteristics, StreamConfigurationMap map);
        
        void onCloseCamera();
        
        void configurePreviewRequest(CaptureRequest.Builder builder);
        
        void configureCaptureRequest(CaptureRequest.Builder builder);
        
        void onImageAvailable(Image image);
        
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

    public static class Camera implements ImageReader.OnImageAvailableListener {

        private final Callback mCallback;
        private final CameraManager manager;
        private final Object mCameraLock = new Object();

        protected CameraDevice cameraDevice;
        protected CameraCharacteristics characteristics;
        protected CameraCaptureSession currentSession;
        protected CaptureRequest captureRequest;
        protected CaptureRequest.Builder captureRequestBuilder;
        private ImageReader imageReader;
        
        private Handler mBackgroundHandler;
        private HandlerThread mBackgroundThread;
        
        private Size previewSize;
        private Size captureSize;
        private String cameraId;
        private int outputFormat = ImageFormat.JPEG;
        private int outputQuality = 100;
        private int imageReaderMax = 3;

        public Camera(@NonNull Callback callback){
            mCallback = callback;
            Activity a = callback.getActivity();
            if (a == null)
                throw new Exception("Camera initialized before activity is created");
            manager = (CameraManager) a.getSystemService(Context.CAMERA_SERVICE);
        }

        public void resume(TextureView textureView, String cameraId){
            startBackgroundThread();
            if (textureView.isAvailable())
                openCamera(cameraId);
        }

        public void pause(){
            closeCamera();
            stopBackgroundThread();
        }

        @Override
        public void onImageAvailable(ImageReader reader) {
            synchronized(mCameraLock){
                image = reader.acquireLatestImage();
                mCallback.onImageAvailable(image);
                dispose(image);
            }
        }

        public bytes[] getJpeg(Image image){
            try {
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);
                return bytes;
            } catch (IOException e) {
                mCallback.onError(e.toString());
            }
        }

        public void openCamera(@Nullable String cameraId){
            Activity a = mCallback.getActivity();
            mCallback.onDebug("Opening camera " + cameraId);
            try {
                ImageReader reader = ImageReader.newInstance(captureSize.getWidth(), captureSize.getHeight(), outputFormat, imageReaderMax);
                characteristics = manager.getCameraCharacteristics(cameraId);
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                assert map != null;
                // Add permission for camera and let user grant the permission
                if (ActivityCompat.checkSelfPermission(a, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(a, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(a, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                    return;
                }
                manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                    @Override
                    public void onOpened(CameraDevice camera) {
                        mCallback.onDebug("onOpened camera "+cameraId);
                        mCallback.onOpenCamera(characteristics, map);
                        cameraDevice = camera;
                    }

                    @Override
                    public void onDisconnected(CameraDevice camera) {
                        mCallback.onDebug("onDisconnected camera " + cameraId);
                        cameraDevice.close();
                        mCallback.onCloseCamera();
                    }

                    @Override
                    public void onError(CameraDevice camera, int error) {
                        dispose(cameraDevice);
                        mCallback.onError("onError " + cameraId)
                    }
                }, null);
            } catch (CameraAccessException e) {
                mCallback.onError(e.toString());
            }
        }

        protected void closeCamera(){
            dispose(cameraDevice);
            dispose(imageReader);
        }

        public void startPreview(@NonNull SurfaceTexture texture){
            try {
                texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
                Surface surface = new Surface(texture);
                captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                captureRequestBuilder.addTarget(surface);
                cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                        //The camera is already closed
                        if (null == cameraDevice) {
                            return;
                        }
                        // When the session is ready, we start displaying the preview.
                        cameraCaptureSessions = cameraCaptureSession;
                        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                        mCallback.configurePreviewRequest(captureRequestBuilder);
                        cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                        mCallback.onError("Camera preview configuration failed");
                    }
                }, null);
            } catch (CameraAccessException e) {
                mCallback.onError("startPreview exception: "+e.toString());
            }
        }

        public void takePicture(){
            try {
                texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
                Surface surface = new Surface(texture);
                captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                captureRequestBuilder.addTarget(surface);
                cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                        //The camera is already closed
                        if (null == cameraDevice) {
                            return;
                        }
                        // When the session is ready, we start displaying the preview.
                        cameraCaptureSessions = cameraCaptureSession;
                        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                        mCallback.configurePreviewRequest(captureRequestBuilder);
                        cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                        mCallback.onError("Camera preview configuration failed");
                    }
                }, null);
            } catch (CameraAccessException e) {
                mCallback.onError("startPreview exception: "+e.toString());
            }
        }

        protected void startBackgroundThread() {
            mBackgroundThread = new HandlerThread("Camera Background");
            mBackgroundThread.start();
            mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        }

        protected void stopBackgroundThread() {
            mBackgroundThread.quitSafely();
            try {
                mBackgroundThread.join();
                mBackgroundThread = null;
                mBackgroundHandler = null;
            } catch (InterruptedException e) {
                mCallback.onError(e.toString());
            }
        }

        public Object getCharacteristic(String key){
            return characteristics == null ? null : characteristics.get(key);
        }

        public CameraCharacteristics getCharacteristics(){
            return characteristics;
        }

        public List<String> getCameraIdList(){
            return Arrays.asList(manager.getCameraIdList());
        }

        public void setCaptureFormat(int format){
            outputFormat = format;
        }

        public void setPreviewSize(Size preview){
            previewSize = preview;
        }

        public void setCaptureSize(Size capture){
            captureSize = capture;
        }

        public List<Size> getOutputSizes(String cameraId, int imageFormat){
            CameraCharacteristics c = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = c.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            return Arrays.asList(map.getOutputSizes(imageFormat));
        }

        @Nullable
        public List<Size> getOutputSizes(){
            if (characteristics != null){
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                return Arrays.asList(map.getOutputSizes(outputFormat));
            }
            return null;
        }

        private void dispose(Image image){
            if (image != null) {
                image.close();
                image = null;
            }
        }

        private void dispose(CameraDevice camera){
            if (camera != null) {
                camera.close();
                camera = null;
            }
        }

        private void dispose(ImageReader image){
            if (image != null) {
                image.close();
                image = null;
            }
        }
    }

    public static class Zoom {

    }

    public static class Exposure {

        public final int AUTO_ISO = -1;
        public final long AUTO_EXPOSURE = -1;

        private CameraCharacteristics mChars;
        private Range<Long> mExposureTimeRange;
        private long mExposureTimeNanos = AUTO_EXPOSURE;
        private Range<Integer> mIsoRange;
        private int mIso = AUTO_ISO;

        public Exposure(CameraCharacteristics characteristics){
            mChars = characteristics;
            mExposureTimeRange = mChars.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
            mIsoRange = mChars.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
        }

        public long getMinExposureTimeNanos(){
            return mExposureTimeRange.getLower();
        }

        public long getMaxExposureTimeNanos(){
            return mExposureTimeRange.getUpper();
        }

        public long getExposureTimeNanos(){
            return mExposureTimeNanos;
        }

        public void setExposureTimeNanos(long nanosecs){
            if (nanosecs < mExposureTimeRange.getLower() || nanosecs > mExposureTimeRange.getUpper())
                mExposureTimeNanos = AUTO_EXPOSURE;
            else
                mExposureTimeNanos = nanosecs;
        }

        public long getMinISO(){
            return mIsoRange.getLower();
        }

        public long getMaxISO(){
            return mIsoRange.getUpper();
        }

        public void setIso(int iso){
            if (iso < mExposureTimeRange.getLower() || iso > mExposureTimeRange.getUpper())
                mIso = AUTO_ISO;
            else
                mIso = iso;
        }

        public void apply(CaptureRequest.Builder builder){
            if (mIso != AUTO_ISO){
                
            }
        }
    }



    
}