package com.huajiao.render;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.JsonReader;


import com.huajiao.help.third.FaceTrackerManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * yutianzuo，analyze res package
 */

public class EffectManager {
    private static final String TAG = "EffectManager";

    private String _effectName;
    private String _effectID;
    private int _type;
    private int _loop;
    private String _music;

    private AssetManager assetManager;
    List<TextureFeature> textureList = null;
    boolean m_b_use_assets = true;

    Context mContext;


    public EffectManager(Context context) {
        mContext = context;
        textureList = new ArrayList<>();
    }

    private int parseJson(String str_json) {
        //step 2 parset the config string
        JsonReader jsonReader = new JsonReader(new StringReader(str_json));

        try {
            jsonReader.beginObject();
            jsonReader.nextName();
            _effectName = jsonReader.nextString();
            jsonReader.nextName();
            _effectID = jsonReader.nextString();
            jsonReader.nextName();
            _type = jsonReader.nextInt();
            jsonReader.nextName();
            _loop = jsonReader.nextInt();
            jsonReader.nextName();
            _music = jsonReader.nextString();

            if (jsonReader.nextName().equals("texture")) {
                jsonReader.beginArray();
                while (jsonReader.hasNext()) {
                    TextureFeature textureFeature = new TextureFeature();
                    jsonReader.beginObject();
                    while (jsonReader.hasNext()) {
                        switch (jsonReader.nextName()) {
                            case "mframeCount":
                                textureFeature._frameCount = jsonReader.nextInt();
                                break;
                            case "radius_Type":
                                textureFeature._radiusType = jsonReader.nextInt();
                                break;
                            case "mradius":
                                textureFeature._radius = jsonReader.nextInt();
                                break;
                            case "mid_Type":
                                textureFeature._midType = jsonReader.nextInt();
                                break;
                            case "scale_Type":
                                textureFeature._scaleType = jsonReader.nextInt();
                                break;
                            case "scale_ratio":
                                textureFeature._scaleRatio = Float.parseFloat(jsonReader.nextString());
                                break;
                            case "anchor_offset_x":
                                textureFeature._x = jsonReader.nextInt();
                                break;
                            case "anchor_offset_y":
                                textureFeature._y = jsonReader.nextInt();
                                break;
                            case "asize_offset_x":
                                textureFeature._w = jsonReader.nextInt();
                                break;
                            case "asize_offset_y":
                                textureFeature._h = jsonReader.nextInt();
                                break;
                            case "mfaceCount":
                                textureFeature._faceCount = jsonReader.nextInt();
                                break;
                            case "imageName":
                                textureFeature._folderName = jsonReader.nextString();
                                break;
                            case "mid_x":
                                textureFeature._midX = Float.parseFloat(jsonReader.nextString());
                                break;
                            case "mid_y":
                                textureFeature._midY = Float.parseFloat(jsonReader.nextString());
                                break;
                            default:
                                return -2;
                        }

                    }
                    jsonReader.endObject();
                    textureList.add(textureFeature);
                }
                jsonReader.endArray();
                jsonReader.endObject();
                jsonReader.close();
            } else {
                return -3;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return -4;
        }
        return 0;
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public int parseAssetsConfig(Context context, String effectID) {
        m_b_use_assets = true;
        String folderName = "eff/" + effectID + "/config";
        assetManager = context.getAssets();
        String jsonData = "";
        //step 1: read the config string
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(assetManager.open(folderName));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                jsonData += line;
            }
            bufferedReader.close();
            inputStreamReader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }

        return parseJson(jsonData);
    }

    public int parseLocalConfig(Context context, String effectID) {
        m_b_use_assets = false;
        String str_folder = FaceTrackerManager.getAppDir(context) + File.separator;
        String folderName = str_folder + effectID + "/config";
        String jsonData = "";
        //step 1: read the config string
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(folderName));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                jsonData += line;
            }
            bufferedReader.close();
            inputStreamReader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }

        return parseJson(jsonData);
    }

    public int GetPngTotalNum() {
        int n_total = 0;
        for (int i = 0; i < getTextureNum(); i++) {
            n_total += getTextureFrameCount(i);
        }
        return n_total;
    }

    public int getTextureNum() {
        return textureList.size();
    }

    public int getTextureX(int index) {
        assert (textureList.size() != 0);
        if (index >= textureList.size()) {
            index = index % textureList.size();
        }
        return textureList.get(index)._x;
    }

    public int getTextureY(int index) {
        assert (textureList.size() != 0);
        if (index >= textureList.size()) {
            index = index % textureList.size();
        }
        return textureList.get(index)._y;
    }

    public int getTextureW(int index) {
        assert (textureList.size() != 0);
        if (index >= textureList.size()) {
            index = index % textureList.size();
        }
        return textureList.get(index)._w;
    }

    public int getTextureH(int index) {
        assert (textureList.size() != 0);
        if (index >= textureList.size()) {
            index = index % textureList.size();
        }
        return textureList.get(index)._h;
    }

    public int getTextureRadiusType(int index) {
        assert (textureList.size() != 0);
        if (index >= textureList.size()) {
            index = index % textureList.size();
        }
        return textureList.get(index)._radiusType;
    }

    public int getTextureRadius(int index) {
        assert (textureList.size() != 0);
        if (index >= textureList.size()) {
            index = index % textureList.size();
        }
        return textureList.get(index)._radius;
    }

    public int getTextureMidType(int index) {
        assert (textureList.size() != 0);
        if (index >= textureList.size()) {
            index = index % textureList.size();
        }
        return textureList.get(index)._midType;
    }

    public int getTextureScaleType(int index) {
        assert (textureList.size() != 0);
        if (index >= textureList.size()) {
            index = index % textureList.size();
        }
        return textureList.get(index)._scaleType;
    }

    public float getTextureScaleRation(int index) {
        assert (textureList.size() != 0);
        if (index >= textureList.size()) {
            index = index % textureList.size();
        }
        return textureList.get(index)._scaleRatio;
    }

    public int getTextureFaceCount(int index) {
        assert (textureList.size() != 0);
        if (index >= textureList.size()) {
            index = index % textureList.size();
        }
        return textureList.get(index)._faceCount;
    }

    public int getTextureFrameCount(int index) {
        assert (textureList.size() != 0);
        if (index >= textureList.size()) {
            index = index % textureList.size();
        }
        return textureList.get(index)._frameCount;
    }

    public String getTextureFolderName(int index) {
        assert (textureList.size() != 0);
        if (index >= textureList.size()) {
            index = index % textureList.size();
        }
        return textureList.get(index)._folderName;
    }

    public float getTextureMidX(int index) {
        assert (textureList.size() != 0);
        if (index >= textureList.size()) {
            index = index % textureList.size();
        }
        return textureList.get(index)._midX;
    }

    public float getTextureMidY(int index) {
        assert (textureList.size() != 0);
        if (index >= textureList.size()) {
            index = index % textureList.size();
        }
        return textureList.get(index)._midY;
    }

    public String GetPngName(int indexOfFolder, int indexOfImage) {
        TextureFeature textureFeature = textureList.get(indexOfFolder);
        return textureFeature._folderName + indexOfImage + ".png";
    }

    public Bitmap GetBitmap(int indexOfFolder, int indexOfImage) {
        if (m_b_use_assets) {
            return getBitmapFromAssets(indexOfFolder, indexOfImage);
        } else {
            return getBitmapFromLocal(indexOfFolder, indexOfImage);
            //return getBitmapFromLocalUseFresco(indexOfFolder, indexOfImage);
        }
    }

    private String GetPngAssetName(int indexOfFolder, int indexOfImage) {
        TextureFeature textureFeature = textureList.get(indexOfFolder);
        String currentImageName = "eff/" + _effectID + "/" + textureFeature._folderName + "/" + textureFeature._folderName + indexOfImage + ".png";
        return currentImageName;
    }

    private String GetPngLocalName(int indexOfFolder, int indexOfImage) {
        TextureFeature textureFeature = textureList.get(indexOfFolder);
        String currentImageName = FaceTrackerManager.getAppDir(mContext) + File.separator + _effectID + "/" + textureFeature._folderName + "/" + textureFeature._folderName + indexOfImage + ".png";
        return currentImageName;
    }

    private Bitmap getBitmapFromAssets(int indexOfFolder, int indexOfImage) {
        String currentImageName = GetPngAssetName(indexOfFolder, indexOfImage);
        InputStream in = null;
        try {
            in = assetManager.open(currentImageName);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(in);
        } catch (Throwable e) {

        }
        return bitmap;
    }

    private Bitmap getBitmapFromLocal(int indexOfFolder, int indexOfImage) {
        String currentImageName = GetPngLocalName(indexOfFolder, indexOfImage);
        InputStream in = null;
        try {
            in = new FileInputStream(currentImageName);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(in);
        } catch (Throwable e) {

        }
        return bitmap;
    }

    public void clear() {
        if (textureList != null) {
            textureList.clear();
        }
    }

    private class TextureFeature {
        public float _midY;
        public float _midX;
        private int _x;
        private int _y;
        private int _w;
        private int _h;
        private int _radiusType;
        private int _radius;
        private int _midType;
        private int _scaleType;
        private float _scaleRatio;
        private int _faceCount;
        private int _frameCount;
        private String _folderName;
    }
}
