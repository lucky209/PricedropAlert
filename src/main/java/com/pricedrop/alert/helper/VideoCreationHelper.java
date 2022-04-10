package com.pricedrop.alert.helper;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;

@Component
@Slf4j
public class VideoCreationHelper {

    @Value("${image.width}")
    private int videoWidth;

    @Value("${image.height}")
    private int videoHeight;

    @Value("${input.resource.path}")
    private String inputResourcePath;

    @Value("${output.resource.path}")
    private String outputResourcePath;

    public boolean createMP4WithAudio(int size) {
        try {
            //remove previous video file if exists
            for (int i = 1; i <= size; i++) {
                File f = new File(outputResourcePath + "/video-"+i+".mp4");
                if (f.exists()) {
                    boolean success = f.delete();
                    if (success)
                        log.info("Existing video-" + i + " file deleted successfully...");
                }
                this.createAVCodecMp4(i);
                log.info("Creating {}....", "video-"+i+".mp4");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean mergeAVCodecMp4(int count) throws Exception {
        Frame frame; opencv_core.IplImage ipl; 
        OpenCVFrameConverter.ToIplImage grabberConverter = new OpenCVFrameConverter.ToIplImage();
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(
            outputResourcePath + "/merged.mp4",720,1280);
        FrameGrabber audioFileGrabber = new FFmpegFrameGrabber(inputResourcePath + "/audio.mp3");
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264 );//AV_CODEC_ID_VORBIS
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);//AV_CODEC_ID_MP3 //AV_CODEC_ID_AAC
        recorder.setFormat("mp4");
        recorder.setAudioChannels(2);
        recorder.start();
        audioFileGrabber.start();
        for (int i = 1; i < count; i++) {
            ipl = cvLoadImage(outputResourcePath + "/image-"+ i +".png");            
            try
            {                
                int frameCount = 0;
                while ((frame = audioFileGrabber.grabFrame())!=null)
                {   recorder.record(grabberConverter.convert(ipl));
                    recorder.record(frame);
                    if (frameCount > 475)
                        break;
                    frameCount++;
                }
            }
            catch (FrameRecorder.Exception | FrameGrabber.Exception e){
                return false;
            }
        }
        recorder.stop();
        audioFileGrabber.stop();
        return true;
    }

    private void createAVCodecMp4(int imageNumber) throws FrameGrabber.Exception, FrameRecorder.Exception {
        opencv_core.IplImage ipl = cvLoadImage(outputResourcePath + "/image-"+ imageNumber +".png");
        int height = ipl.height();
        int width = ipl.width();
        if(height%2!=0) height = height+1;
        if(width%2!=0) width = width+1;

        OpenCVFrameConverter.ToIplImage grabberConverter = new OpenCVFrameConverter.ToIplImage();
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputResourcePath + "/video-"+ imageNumber +".mp4",width,height);
        FrameGrabber audioFileGrabber = new FFmpegFrameGrabber(inputResourcePath + "/audio.mp3");
        try
        {
            audioFileGrabber.start();

            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264 );//AV_CODEC_ID_VORBIS
            recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);//AV_CODEC_ID_MP3 //AV_CODEC_ID_AAC

            recorder.setFormat("mp4");
            recorder.setAudioChannels(2);
            recorder.start();

            Frame frame;
            int frameCount = 0;
            while ((frame = audioFileGrabber.grabFrame())!=null)
            {   recorder.record(grabberConverter.convert(ipl));
                recorder.record(frame);
                if (frameCount > 475)
                    break;
                frameCount++;
            }

            recorder.stop();
            audioFileGrabber.stop();
        }
        catch (FrameRecorder.Exception | FrameGrabber.Exception e){
            e.printStackTrace();
            throw e;
        }
    }
}
