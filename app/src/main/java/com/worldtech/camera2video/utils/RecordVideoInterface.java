package com.worldtech.camera2video.utils;

public interface RecordVideoInterface {

    /**
     * 开始录制
     */
    void  startRecordRes();

    /**
     * 正在录制
     * @param recordTime 录制的时间
     */
    void  onRecording(long recordTime);

    /**
     * 录制完成
     * @param videoPath  录制保存的路径
     */
    void  onRecordFinish(String videoPath);

    /**
     * 录制出问题
     */
    void  onRecordError();

}
