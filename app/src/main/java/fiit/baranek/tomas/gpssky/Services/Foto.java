package fiit.baranek.tomas.gpssky.Services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ExifInterface;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestBatch;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fiit.baranek.tomas.gpssky.MainActivity;

public class Foto extends Service {

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    CameraDevice mCameraDevice;
    String iconsStoragePath = "";
    private final Context mContext;
    private Boolean Received = false;

    public Foto(Context context) {
        this.mContext = context;
        openCamera();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public String takePicture(final int TimeoutOfFacebookSharing, final String SMStextForFacebookTimeout, final String message_share, String where, final String ivent, final double latitude, final double longitude, final double altitude,int BestPhotoWidth, int BestPhotoHeight, final int facebookPhotoWidth, final int facebookPhotoHeight) {
        Received = false;
        System.out.println("takePicture");
        if (null == mCameraDevice) {
            System.out.println("mCameraDevice is null, return");
            return "";
        }


        CameraManager manager = (CameraManager) mContext
                .getSystemService(CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraDevice.getId());

            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics
                        .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                        .getOutputSizes(ImageFormat.JPEG);
            }
            int width = BestPhotoWidth;
            int height = BestPhotoHeight;
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }

            final ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(1);
            outputSurfaces.add(reader.getSurface());
            //outputSurfaces.add(new Surface(mTextureView.getSurfaceTexture()));

            final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            // Orientation
            WindowManager windowManager =(WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            int rotation = windowManager.getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            String timeStamp = new SimpleDateFormat("hh_mm_ss_dd_MM_yyyy").format(new Date());
            String imageFileName = "SKUSKA_" + timeStamp;

            iconsStoragePath = where;
            File sdIconStorageDir = new File(iconsStoragePath);
            sdIconStorageDir.mkdir();

            final File file = new File(sdIconStorageDir, imageFileName + ".jpg");
            iconsStoragePath = file.getAbsolutePath();
            System.out.println("Cesta: " + iconsStoragePath);
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {


                @Override
                public void onImageAvailable(ImageReader reader) {
                    //Toast.makeText(MainActivity.this, "Fotka bola urobena : " , Toast.LENGTH_SHORT).show();
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                    }
                }

                Bitmap ShrinkBitmap(String file, int width, int height) {

                    BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
                    bmpFactoryOptions.inJustDecodeBounds = true;
                    Bitmap bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions);

                    int heightRatio = (int) Math.ceil(bmpFactoryOptions.outHeight / (float) height);
                    int widthRatio = (int) Math.ceil(bmpFactoryOptions.outWidth / (float) width);

                    if (heightRatio > 1 || widthRatio > 1) {
                        if (heightRatio > widthRatio) {
                            bmpFactoryOptions.inSampleSize = heightRatio;
                        } else {
                            bmpFactoryOptions.inSampleSize = widthRatio;
                        }
                    }

                    bmpFactoryOptions.inJustDecodeBounds = false;
                    bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions);
                    return bitmap;
                }

                private void save(byte[] bytes) throws IOException {
                    OutputStream output = null;
                    try {
                        output = new FileOutputStream(file);
                        output.write(bytes);
                        ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
                        //800,420
                        ShrinkBitmap(file.getAbsolutePath(), facebookPhotoWidth, facebookPhotoHeight).compress(Bitmap.CompressFormat.PNG, 100, stream2);
                        byte[] byteArray2 = stream2.toByteArray();
                        AccessToken token = AccessToken.getCurrentAccessToken();
                        String path = "/" + "1584697781841739" + "/photos";
                        Bundle parametre = new Bundle();
                        final String send = message_share;
                        parametre.putString("message", send);
                        parametre.putString("description", "topic share");
                        parametre.putByteArray("picture", byteArray2);


                                GraphRequest request2 = new GraphRequest(token, path, parametre, HttpMethod.POST, new GraphRequest.Callback() {

                            @Override
                            public void onCompleted(GraphResponse response) {
                                JSONObject obj = response.getJSONObject();
                                if (obj != null) {
                                    Received = true;
                                    SMS sendSMS = new SMS();
                                    sendSMS.sendSMS("+421919277176", "Nenstala chyb" +  obj.optString("id"), mContext.getApplicationContext());
                                    System.out.println("id : " + obj.optString("id"));
                                } else {
                                    Received = true;
                                    System.out.println("Zle je : " +response.getError().getErrorMessage());
                                    SMS sendSMS = new SMS();
                                    sendSMS.sendSMS("+421919277176", SMStextForFacebookTimeout + String.valueOf(longitude) + ", " + String.valueOf(latitude) + "," + String.valueOf(altitude), mContext.getApplicationContext());

                                }
                            }
                        });
                        GraphRequestBatch requestBatch = new GraphRequestBatch(request2);
                        System.out.println("Facebook timeout: " + requestBatch.getTimeout());
                        requestBatch.setTimeout(TimeoutOfFacebookSharing);
                        requestBatch.executeAsync();
                        //request2.executeAsync();
                        //request2.
                    } finally {
                        if (null != output) {
                            output.close();
                        }
                    }


                    ExifInterface exif = new ExifInterface(file.getAbsolutePath());
                    exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, String.valueOf(latitude));
                    exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, String.valueOf(longitude));
                    exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, String.valueOf(altitude));
                    exif.saveAttributes();
                }

            };

            HandlerThread thread = new HandlerThread("CameraPicture");
            thread.start();
            final Handler backgroudHandler = new Handler(thread.getLooper());
            reader.setOnImageAvailableListener(readerListener, backgroudHandler);

            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(CameraCaptureSession session,
                                               CaptureRequest request, TotalCaptureResult result) {

                    super.onCaptureCompleted(session, request, result);

                    //Toast.makeText(MainActivity.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
                    reader.toString();
                    //Toast.makeText(MainActivity.this, "Koniec ! ! ! ! " + reader.toString() , Toast.LENGTH_SHORT).show();
                    //startPreview();
                }

            };

            mCameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(CameraCaptureSession session) {

                    try {
                        session.capture(captureBuilder.build(), captureListener, backgroudHandler);
                    } catch (CameraAccessException e) {

                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {

                }
            }, backgroudHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return iconsStoragePath;
    }


    public String takePictureWithoutFacebook(String where, final double latitude, final double longitude, final double altitude,int BestPhotoWidth, int BestPhotoHeight) {
        System.out.println("takePicture");
        if (null == mCameraDevice) {
            System.out.println("mCameraDevice is null, return");
            return "";
        }


        CameraManager manager = (CameraManager) mContext
                .getSystemService(CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraDevice.getId());

            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics
                        .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                        .getOutputSizes(ImageFormat.JPEG);
            }
            int width = BestPhotoWidth;
            int height = BestPhotoHeight;
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }

            final ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(1);
            outputSurfaces.add(reader.getSurface());
            //outputSurfaces.add(new Surface(mTextureView.getSurfaceTexture()));

            final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            // Orientation
            WindowManager windowManager =(WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            int rotation = windowManager.getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            String timeStamp = new SimpleDateFormat("hh_mm_ss_dd_MM_yyyy").format(new Date());
            String imageFileName = timeStamp;

            iconsStoragePath = where;
            File sdIconStorageDir = new File(iconsStoragePath);
            sdIconStorageDir.mkdir();

            final File file = new File(sdIconStorageDir, imageFileName + ".jpg");
            iconsStoragePath = file.getAbsolutePath();
            System.out.println("Cesta: " + iconsStoragePath);
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {


                @Override
                public void onImageAvailable(ImageReader reader) {
                    //Toast.makeText(MainActivity.this, "Fotka bola urobena : " , Toast.LENGTH_SHORT).show();
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                    }
                }

                Bitmap ShrinkBitmap(String file, int width, int height) {

                    BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
                    bmpFactoryOptions.inJustDecodeBounds = true;
                    Bitmap bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions);

                    int heightRatio = (int) Math.ceil(bmpFactoryOptions.outHeight / (float) height);
                    int widthRatio = (int) Math.ceil(bmpFactoryOptions.outWidth / (float) width);

                    if (heightRatio > 1 || widthRatio > 1) {
                        if (heightRatio > widthRatio) {
                            bmpFactoryOptions.inSampleSize = heightRatio;
                        } else {
                            bmpFactoryOptions.inSampleSize = widthRatio;
                        }
                    }

                    bmpFactoryOptions.inJustDecodeBounds = false;
                    bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions);
                    return bitmap;
                }

                private void save(byte[] bytes) throws IOException {
                    OutputStream output = null;
                    try {
                        output = new FileOutputStream(file);
                        output.write(bytes);
                    } finally {
                        if (null != output) {
                            output.close();
                        }
                    }


                    ExifInterface exif = new ExifInterface(file.getAbsolutePath());
                    exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, String.valueOf(latitude));
                    exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, String.valueOf(longitude));
                    exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, String.valueOf(altitude));
                    exif.saveAttributes();
                }

            };

            HandlerThread thread = new HandlerThread("CameraPicture");
            thread.start();
            final Handler backgroudHandler = new Handler(thread.getLooper());
            reader.setOnImageAvailableListener(readerListener, backgroudHandler);

            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(CameraCaptureSession session,
                                               CaptureRequest request, TotalCaptureResult result) {

                    super.onCaptureCompleted(session, request, result);

                    //Toast.makeText(MainActivity.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
                    reader.toString();
                    //Toast.makeText(MainActivity.this, "Koniec ! ! ! ! " + reader.toString() , Toast.LENGTH_SHORT).show();
                    //startPreview();
                }

            };

            mCameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(CameraCaptureSession session) {

                    try {
                        session.capture(captureBuilder.build(), captureListener, backgroudHandler);
                    } catch (CameraAccessException e) {

                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {

                }
            }, backgroudHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return iconsStoragePath;
    }

    private void openCamera() {

        CameraManager manager = (CameraManager) mContext
                .getSystemService(CAMERA_SERVICE);
        System.out.println("openCamera E");
        try {
            String cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }*/
            manager.openCamera(cameraId, mStateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        System.out.println("openCamera X");
    }

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice camera) {

            System.out.println("onOpened");
            mCameraDevice = camera;
            //startPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {

            System.out.println("onDisconnected");
        }

        @Override
        public void onError(CameraDevice camera, int error) {

            System.out.println("onError");
        }

    };

    public Boolean LastFotoStatus(){
        return Received;
    }
}
