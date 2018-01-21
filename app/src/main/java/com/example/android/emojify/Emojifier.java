/*
* Copyright (C) 2017 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*  	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.example.android.emojify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import timber.log.Timber;

class Emojifier {

    private static final double SMILING_PROB_THRESHOLD = .12;
   private static final double EYE_OPEN_PROB_THRESHOLD = .6;

    private static final float EMOJI_SCALE_FACTOR = .9f;

static Bitmap detectFaces(Context context,Bitmap picture) {

    FaceDetector detector = new FaceDetector.Builder(context)
            .setTrackingEnabled(false)
            .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
            .build();

    Frame frame = new Frame.Builder().setBitmap(picture).build();

    SparseArray<Face> faces = detector.detect(frame);

    if (faces.size() == 0) {
        Toast.makeText(context, "THERE ARE NO FACES IN THE PICTURE", Toast.LENGTH_LONG).show();
    } else {
        Toast.makeText(context, "NO.OF FACES: " + faces.size(), Toast.LENGTH_LONG).show();
    }
    Bitmap resultBitmap=picture;
    for(int i=0;i<faces.size();i++){
        Face face=faces.valueAt(i);

        Bitmap emojiBitmap;
        switch (whichEmoji(face)){

            case SMILE:
                emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.smile);
                break;
            case FROWN:
                emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.frown);
                break;
            case LEFT_WINK_SMILE:
                emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.leftwink);
                break;
            case RIGHT_WINK_SMILE:
                emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.rightwink);
                break;
            case LEFT_WINK_FROWN:
                emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.leftwinkfrown);
                break;
            case RIGHT_WINK_FROWN:
                emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.rightwinkfrown);
                break;
            case EYES_CLOSED_SMILE:
                emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.closed_smile);
                break;
            case EYES_CLOSED_FROWN:
                emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.closed_frown);
                break;
            default:
                emojiBitmap = null;

        }
             resultBitmap=addBitmapToFace(resultBitmap,emojiBitmap,face);
    }

    detector.release();
    return resultBitmap;
}

static Emoji whichEmoji(Face face){

    float lefteyeopen=face.getIsLeftEyeOpenProbability();
    float righteyeopen=face.getIsRightEyeOpenProbability();
    float smileProb=face.getIsSmilingProbability();

    Log.d("TAG","LEFT EYE OPEN:"+lefteyeopen);
    Log.d("TAG","RIGHT EYE OPEN:"+righteyeopen);
    Log.d("TAG","SMILING:"+smileProb);

    Emoji emoji;

    boolean smile=smileProb>SMILING_PROB_THRESHOLD;
    boolean leftEyeClosed=lefteyeopen<EYE_OPEN_PROB_THRESHOLD;
    boolean rightEyeClosed=righteyeopen<EYE_OPEN_PROB_THRESHOLD;

    if(smile){
        if(leftEyeClosed&&!rightEyeClosed){
            emoji=Emoji.LEFT_WINK_SMILE;
        }
        else if (!leftEyeClosed&&rightEyeClosed){
            emoji=Emoji.RIGHT_WINK_SMILE;
        }
        else if(leftEyeClosed&&rightEyeClosed) {
            emoji=Emoji.EYES_CLOSED_SMILE;
        }
        else {
            emoji=Emoji.SMILE;
        }
    }else{
        if(leftEyeClosed&&!rightEyeClosed){
            emoji=Emoji.LEFT_WINK_FROWN;
        }
        else if (!leftEyeClosed&&rightEyeClosed){
            emoji=Emoji.RIGHT_WINK_FROWN;
        }
        else if(leftEyeClosed&&rightEyeClosed) {
            emoji=Emoji.EYES_CLOSED_FROWN;
        }
        else {
            emoji=Emoji.FROWN;
        }

    }

return emoji;

}

    private static Bitmap addBitmapToFace(Bitmap backgroundBitmap, Bitmap emojiBitmap, Face face) {


        Bitmap resultBitmap = Bitmap.createBitmap(backgroundBitmap.getWidth(),
                backgroundBitmap.getHeight(), backgroundBitmap.getConfig());


        float scaleFactor = EMOJI_SCALE_FACTOR;


        int newEmojiWidth = (int) (face.getWidth() * scaleFactor);
        int newEmojiHeight = (int) (emojiBitmap.getHeight() *
                newEmojiWidth / emojiBitmap.getWidth() * scaleFactor);



        emojiBitmap = Bitmap.createScaledBitmap(emojiBitmap, newEmojiWidth, newEmojiHeight, false);

        float emojiPositionX = (face.getPosition().x + face.getWidth() / 2) - emojiBitmap.getWidth() / 2;
        float emojiPositionY = (face.getPosition().y + face.getHeight() / 2) - emojiBitmap.getHeight() / 3;


        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(backgroundBitmap, 0, 0, null);
        canvas.drawBitmap(emojiBitmap, emojiPositionX, emojiPositionY, null);

        return resultBitmap;
    }

private enum Emoji{
    SMILE,
    FROWN,
    LEFT_WINK_SMILE,
    RIGHT_WINK_SMILE,
    LEFT_WINK_FROWN,
    RIGHT_WINK_FROWN,
    EYES_CLOSED_SMILE,
    EYES_CLOSED_FROWN

}

}
