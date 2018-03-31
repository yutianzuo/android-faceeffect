package com.huajiao.help.third;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

import com.qihoo.faceapi.QhFaceApi;
import com.qihoo.faceapi.util.QhFaceInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by j-yutianzuo on 2016/5/18.
 */
final public class FaceTrackerManager {
    public static final String QH_FACE_MODEL_FOLDER_NAME = "model";

    public static int initFaceSDK(Context context) {
        int nRet = QhFaceApi.qhFaceDetectInit(getAppDir(context) + QH_FACE_MODEL_FOLDER_NAME, 1);
        return nRet;
    }

    public static void unInitFaceSDK() {
        QhFaceApi.qhFaceDetectDestroy();
    }

    public static QhFaceInfo detectedFace(byte[] data, int width, int height) {
        QhFaceInfo faces[] = QhFaceApi.FaceDetectYUV(data, width, height, -1);
        if (faces != null && faces.length > 0) {
            return faces[0];
        } else {
            return null;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public static boolean checkSDCard() {
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static void makeDir(String dir) {
        File f = new File(dir);
        if (!f.exists()) {
            f.mkdirs();
        }
    }

    public static String getAppDir(Context context) {
        String dir = "";
        if (checkSDCard()) {
            dir = Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            dir = context.getDir("faceeffdemo_private", Context.MODE_PRIVATE)
                    .getAbsolutePath();
        }
        dir = dir + File.separator + "faceeffdemo" + File.separator;
        makeDir(dir);

        String str_Hide_FilePath = dir + ".nomedia";
        File fHide = new File(str_Hide_FilePath);
        if (!fHide.isFile()) {
            try {
                fHide.createNewFile();
            } catch (Exception e) {
            }

        }
        return dir;
    }

    public static void deleteFile(String filepath) {
        File file = new File(filepath);
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    if (files.length > 0) {
                        File[] delFiles = file.listFiles();
                        if (delFiles != null && delFiles.length > 0) {
                            for (File delFile : delFiles) {
                                deleteFile(delFile.getAbsolutePath());
                            }
                        }
                    }
                }
            }
            file.delete();
        }
    }

    static public void copyAndUnzipModelFiles(final Context context) {
        String str_path = getAppDir(context) + QH_FACE_MODEL_FOLDER_NAME;
        File folder_file = new File(str_path);

        if (!folder_file.isDirectory() || folder_file.listFiles() == null || folder_file.listFiles().length <= 0) {
            deleteFile(str_path);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String file_name = QH_FACE_MODEL_FOLDER_NAME + ".zip";
                    String file_out = getAppDir(context) + file_name;
                    AssetManager assetManager = context.getAssets();
                    int byteread = 0;
                    try {
                        InputStream is = assetManager.open(file_name);
                        FileOutputStream fs = new FileOutputStream(file_out);
                        byte[] buffer = new byte[2048];

                        while ((byteread = is.read(buffer)) != -1) {
                            fs.write(buffer, 0, byteread);
                        }

                        is.close();
                        fs.close();
                    } catch (Throwable e) {
                        return;
                    }

                    unZipFolder(file_out, getAppDir(context));
                    deleteFile(file_out);
                    initFaceSDK(context);
                }
            }).start();
        } else {
            initFaceSDK(context);
            return;
        }
    }

    static public void copyAndUnzipResFiles(final Context context) {
        String str_path = getAppDir(context) + "31034_1";
        File folder_file = new File(str_path);

        if (!folder_file.isDirectory() || folder_file.listFiles() == null || folder_file.listFiles().length <= 0) {
            deleteFile(str_path);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String file_name = "31034_1" + ".zip";
                    String file_out = getAppDir(context) + file_name;
                    AssetManager assetManager = context.getAssets();
                    int byteread = 0;
                    try {
                        InputStream is = assetManager.open(file_name);
                        FileOutputStream fs = new FileOutputStream(file_out);
                        byte[] buffer = new byte[2048];

                        while ((byteread = is.read(buffer)) != -1) {
                            fs.write(buffer, 0, byteread);
                        }

                        is.close();
                        fs.close();
                    } catch (Throwable e) {
                        return;
                    }

                    unZipFolder(file_out, getAppDir(context));
                    deleteFile(file_out);
                }
            }).start();
        } else {
            return;
        }
    }

    static private boolean unZipFolder(String zipFileString, String outPathString) {
        boolean b_ret = true;
        try {
            ZipInputStream inZip = new ZipInputStream(new FileInputStream(zipFileString));
            ZipEntry zipEntry;
            String szName = "";
            while ((zipEntry = inZip.getNextEntry()) != null) {
                szName = zipEntry.getName();
                if (zipEntry.isDirectory()) {
                    // get the folder name of the widget
                    szName = szName.substring(0, szName.length() - 1);
                    File folder = new File(outPathString + File.separator + szName);
                    folder.mkdirs();
                } else {
                    int n_last_index = szName.lastIndexOf("/");
                    if (n_last_index != -1) {
                        String str_folder_name = outPathString + File.separator + szName.substring(0, n_last_index);
                        File folder = new File(str_folder_name);
                        if (!folder.isDirectory()) {
                            folder.mkdirs();
                        }
                    }
                    File file = new File(outPathString + File.separator + szName);
                    file.createNewFile();
                    // get the output stream of the file
                    FileOutputStream out = new FileOutputStream(file);
                    int len;
                    byte[] buffer = new byte[1024];
                    // read (len) bytes into buffer
                    while ((len = inZip.read(buffer)) != -1) {
                        // write (len) byte from buffer at the position 0
                        out.write(buffer, 0, len);
                        out.flush();
                    }
                    out.close();
                }
            }
            inZip.close();
        } catch (Throwable e) {
            b_ret = false;
        }
        return b_ret;
    }

}





