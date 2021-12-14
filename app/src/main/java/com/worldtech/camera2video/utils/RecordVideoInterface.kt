package com.worldtech.camera2video.utils

interface RecordVideoInterface {
    /**
     * 开始录制
     */
    fun startRecordRes()

    /**
     * 正在录制
     * @param recordTime 录制的时间
     */
    fun onRecording(recordTime: Long)

    /**
     * 录制完成
     * @param videoPath  录制保存的路径
     */
    fun onRecordFinish(videoPath: String?)

    /**
     * 录制出问题
     */
    fun onRecordError()
}