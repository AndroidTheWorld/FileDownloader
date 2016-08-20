/*
 * Copyright (c) 2015 LingoChamp Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liulishuo.filedownloader;

import com.liulishuo.filedownloader.message.MessageSnapshotThreadPool;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.liulishuo.filedownloader.util.FileDownloadUtils;

/**
 * Created by Jacksgong on 9/23/15.
 * <p/>
 * A atom download task.
 *
 * @see FileDownloader
 * @see ITaskHunter
 */
@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
public interface BaseDownloadTask {

    int DEFAULT_CALLBACK_PROGRESS_MIN_INTERVAL_MILLIS = 10;

    /**
     * @param minIntervalUpdateSpeedMs The min interval millisecond for updating the download speed
     *                                 in downloading process(Status equal to progress).
     *                                 Default 5 ms. If less than or equal to 0, will not calculate
     *                                 the download speed in process.
     */
    BaseDownloadTask setMinIntervalUpdateSpeed(int minIntervalUpdateSpeedMs);

    /**
     * @param path {@code path} = absolute directory/{@code filename}; and {@link #isPathAsDirectory()}
     *             assign to {@code false}.
     */
    BaseDownloadTask setPath(final String path);

    /**
     * @param path            Absolute path for saving the download file.
     * @param pathAsDirectory If {@code true}: {@code path} is absolute directory to store the downloading file,
     *                        and the {@code filename} will be found in contentDisposition
     *                        from the response as default, if can't find contentDisposition,
     *                        the {@code filename} will be generated by
     *                        {@link FileDownloadUtils#generateFileName(String)}  with {@code url}.
     *                        </p>
     *                        If {@code false}: {@code path} = absolute directory/{@code filename}.
     * @see #isPathAsDirectory()
     * @see #getFilename()
     */
    BaseDownloadTask setPath(final String path, final boolean pathAsDirectory);

    /**
     * @param listener For callback download status(pending,connected,progress,
     *                 blockComplete,retry,error,paused,completed,warn)
     */
    BaseDownloadTask setListener(final FileDownloadListener listener);

    /**
     * Set the maximal callback count of
     * {@link FileDownloadListener#progress(BaseDownloadTask, int, int)} during the entire process
     * of downloading.
     * <p>
     * Note: this function will not work if the URL is refer to 'chucked' resource.
     *
     * @param callbackProgressCount The maximal callback count of
     *                              {@link FileDownloadListener#progress(BaseDownloadTask, int, int)}
     *                              during the entire process of downloading.
     *                              Default value is 100, If the value less than or equal to 0, you
     *                              will not receive any callback of
     *                              {@link FileDownloadListener#progress(BaseDownloadTask, int, int)}
     *                              .
     * @see #setCallbackProgressMinInterval(int)
     */
    BaseDownloadTask setCallbackProgressTimes(int callbackProgressCount);

    /**
     * Set the minimum time interval between each callback of
     * {@link FileDownloadListener#progress(BaseDownloadTask, int, int)}.
     *
     * @param minIntervalMillis The minimum time bytes interval between each callback of
     *                          {@link FileDownloadListener#progress(BaseDownloadTask, int, int)}
     *                          Unit: millisecond.
     *                          Default value is {@link #DEFAULT_CALLBACK_PROGRESS_MIN_INTERVAL_MILLIS}.
     *                          Scope: [5, {@link Integer#MAX_VALUE}
     * @see #setCallbackProgressTimes(int)
     */
    BaseDownloadTask setCallbackProgressMinInterval(int minIntervalMillis);

    /**
     * Ignore all callbacks of {@link FileDownloadListener#progress(BaseDownloadTask, int, int)}
     * during the entire process of downloading.
     */
    BaseDownloadTask setCallbackProgressIgnored();

    /**
     * Sets the tag associated with this task, not be used by internal.
     */
    BaseDownloadTask setTag(final Object tag);

    /**
     * Set a tag associated with this task, not be used by internal.
     *
     * @param key The key of identifying the tag.
     *            If the key already exists, the old data will be replaced.
     * @param tag An Object to tag the task with
     */
    BaseDownloadTask setTag(final int key, final Object tag);


    /**
     * Force re download whether already downloaded completed
     *
     * @param isForceReDownload If set to true, will not check whether the file is downloaded
     *                          by past, default false
     */
    BaseDownloadTask setForceReDownload(final boolean isForceReDownload);

    /**
     * @deprecated Replace with {@link #addFinishListener(FinishListener)}
     */
    BaseDownloadTask setFinishListener(final FinishListener finishListener);

    /**
     * This listener's method {@link FinishListener#over(BaseDownloadTask)} will be invoked in Internal-Flow-Thread
     * directly, which is controlled by {@link MessageSnapshotThreadPool}.
     *
     * @param finishListener Just consider whether the task is over.
     * @see FileDownloadStatus#isOver(int)
     */
    BaseDownloadTask addFinishListener(final FinishListener finishListener);

    boolean removeFinishListener(final FinishListener finishListener);

    /**
     * Set the number of times to automatically retry when encounter any error
     *
     * @param autoRetryTimes default 0
     */
    BaseDownloadTask setAutoRetryTimes(int autoRetryTimes);

    /**
     * We have already handled etag, and will add 'If-Match' & 'Range' value if it works.
     *
     * @see okhttp3.Headers.Builder#add(String, String)
     */
    BaseDownloadTask addHeader(final String name, final String value);

    /**
     * We have already handled etag, and will add 'If-Match' & 'Range' value if it works.
     *
     * @see okhttp3.Headers.Builder#add(String, String)
     */
    BaseDownloadTask addHeader(final String line);

    /**
     * @see okhttp3.Headers.Builder#removeAll(String)
     */
    BaseDownloadTask removeAllHeaders(final String name);

    /**
     * @param syncCallback if true will invoke callbacks of {@link FileDownloadListener} directly
     *                     on the download thread(do not post the message to the ui thread
     *                     by {@link android.os.Handler#post(Runnable)}
     */
    BaseDownloadTask setSyncCallback(final boolean syncCallback);

    // -------- Following function for ending ------

    /**
     * Ready task( For queue task )
     * <p/>
     * 用于将几个task绑定为一个队列启动的结束符
     *
     * @return downloadId
     * @see FileDownloader#start(FileDownloadListener, boolean)
     */
    int ready();

    /**
     * Reuse this task withhold request params: path、url、header、isForceReDownloader、etc.
     *
     * @return Successful reuse or not.
     */
    boolean reuse();

    /**
     * @return Whether this task object has already started and used in FileDownload Engine. If true,
     * it isn't allow to {@link #start()} again for this task object.
     * @see #isRunning()
     * @see #start()
     * @see #reuse()
     */
    boolean isUsing();

    /**
     * @return Whether this task object is running in FileDownload Engine. If true, it isn't allow
     * to {@link #start()} again for this task object, and even not allow to {@link #reuse()}.
     * @see #isUsing()
     * @see #start()
     */
    boolean isRunning();

    /**
     * @return Whether has already attached to a listener / a serial-queue. If {@code true}, this task
     * object must be running with the listener or has already assembled to a serial-queue and would
     * be started automatically when it is come to its turn.
     * @see IQueuesHandler#startQueueSerial(FileDownloadListener)
     * @see IQueuesHandler#startQueueParallel(FileDownloadListener)
     */
    boolean isAttached();

    /**
     * start the task.
     *
     * @return Download id
     */
    int start();

    // -------------- Another Operations ---------------------

    /**
     * Why pause? not stop/cancel? because invoke this method(pause) will clear all data about this task
     * in memory, and stop the total processing about this task. but when you start the paused task,
     * it would be continue downloading from the breakpoint as default.
     *
     * @return If true, successful pause this task by status of pause, otherwise this task has
     * already in over status before invoke this method(Maybe occur high concurrent situation).
     * @see FileDownloader#pause(int)
     * @see FileDownloader#pause(FileDownloadListener)
     * @see FileDownloader#pauseAll()
     */
    boolean pause();

    /**
     * The {@link #pause()} also clear all data relate with this task in the memory, so please use
     * {@link #pause()} instead.
     *
     * @return {@code true} if cancel this task successfully.
     * @deprecated replace with {@link #pause()}
     */
    boolean cancel();
    // ------------------- get -----------------------

    /**
     * The identify download id is generated by Url & Path
     * {@link FileDownloadUtils#generateId(String, String)}.
     *
     * @return The identify id for this task.
     * @see FileDownloader#pause(int)
     * @see FileDownloader#getStatus(String, String)
     * @see FileDownloader#getTotal(int)
     * @see FileDownloader#getSoFar(int)
     */
    int getId();

    /**
     * @return The identify id for this task.
     * @deprecated Used {@link #getId()} instead.
     */
    int getDownloadId();

    /**
     * Get download url
     *
     * @return download url
     */
    String getUrl();

    /**
     * @return The maximal callback count of
     * {@link FileDownloadListener#progress(BaseDownloadTask, int, int)} during the entire process
     * of downloading.
     */
    int getCallbackProgressTimes();

    /**
     * @return The minimum time interval between each callback of
     * {@link FileDownloadListener#progress(BaseDownloadTask, int, int)} .
     */
    int getCallbackProgressMinInterval();

    /**
     * @return If {@link #isPathAsDirectory()} is {@code true}: {@code path} is a absolute directory
     * to store the downloading file, and the {@code filename} will be found in contentDisposition
     * from the response as default, if can't find contentDisposition, the {@code filename} will be
     * generated by {@link FileDownloadUtils#generateFileName(String)}  with {@code url}.
     * </p>
     * If {@link #isPathAsDirectory()} is {@code false}: {@code path} = absolute directory/{@code filename}.
     */
    String getPath();

    /**
     * @return Is {@link #getPath()} as a absolute directory.
     * @see #getPath()
     */
    boolean isPathAsDirectory();

    /**
     * @return If {@link #isPathAsDirectory()} is {@code true}, the {@code filename} will be found in
     * contentDisposition from the response as default, if can't find contentDisposition, the
     * {@code filename} will be generated by {@link FileDownloadUtils#generateFileName(String)} with
     * {@code url}. It will be found before the callback of
     * {@link FileDownloadListener#connected(BaseDownloadTask, String, boolean, int, int)}.
     * </p>
     * If {@link #isPathAsDirectory()} is {@code false}, the {@code filename} will be found immediately
     * when invoke {@link #setPath(String, boolean)} .
     */
    String getFilename();

    /**
     * @return The target file path to store the file.
     */
    String getTargetFilePath();

    /**
     * @return Current FileDownloadListener
     */
    FileDownloadListener getListener();

    /**
     * @return Number of bytes download so far
     * @deprecated replace with {@link #getSmallFileSoFarBytes()}.
     */
    int getSoFarBytes();

    /**
     * @return The downloaded so far bytes which size is less than or equal to 1.99G
     */
    int getSmallFileSoFarBytes();

    long getLargeFileSoFarBytes();

    /**
     * @return Total bytes, available
     * after {@link FileDownloadListener#connected(BaseDownloadTask, String, boolean, int, int)}/ already have in db
     * @deprecated replace with {@link #getSmallFileTotalBytes()}}
     */
    int getTotalBytes();

    /**
     * @return The total bytes which size is less than or equal to 1.99G
     */
    int getSmallFileTotalBytes();

    long getLargeFileTotalBytes();

    /**
     * If in downloading process(status equal {@link FileDownloadStatus#progress}) : Calculating
     * when the interval from the last calculation more than {@link #setMinIntervalUpdateSpeed(int)} before
     * each {@link FileDownloadListener#progress(BaseDownloadTask, int, int)} call-back method.
     * <p/>
     * If finished({@link FileDownloadStatus#isOver(int)}): Would be average speed. The scope is
     * (connected, over).
     *
     * @return KB/s
     * @see #setMinIntervalUpdateSpeed(int)
     */
    int getSpeed();

    /**
     * @return Current status
     * @see FileDownloadStatus
     */
    byte getStatus();

    /**
     * @return Force re-download,do not care about whether already downloaded or not
     */
    boolean isForceReDownload();

    /**
     * @deprecated replaced with {@link #getErrorCause()}
     */
    Throwable getEx();

    /**
     * @return the error cause.
     */
    Throwable getErrorCause();


    /**
     * @return Whether reused the downloaded file by past.
     * @see #isReusedOldFile
     */
    boolean isReusedOldFile();

    /**
     * @return The task's tag
     */
    Object getTag();

    /**
     * Returns the tag associated with this task and the specified key.
     *
     * @param key The key identifying the tag
     * @return the object stored in this take as a tag, or {@code null} if not
     * set
     * @see #setTag(int, Object)
     * @see #getTag()
     */
    Object getTag(int key);


    /**
     * @deprecated Use {@link #isResuming()} instead.
     */
    boolean isContinue();

    /**
     * @return Is resume by breakpoint, available
     * after {@link FileDownloadListener#connected(BaseDownloadTask, String, boolean, int, int)}
     */
    boolean isResuming();

    /**
     * @return ETag, available
     * after {@link FileDownloadListener#connected(BaseDownloadTask, String, boolean, int, int)}
     */
    String getEtag();

    /**
     * @return The number of times to automatically retry
     */
    int getAutoRetryTimes();

    /**
     * @return The current number of trey. available
     * after {@link FileDownloadListener#retry(BaseDownloadTask, Throwable, int, int)}
     */
    int getRetryingTimes();

    /**
     * @return whether sync callback directly on the download thread, do not post to the ui thread.
     */
    boolean isSyncCallback();

    /**
     * @return Whether the length of downloading file is more than or equal to 2G.
     * @see #getLargeFileSoFarBytes()
     * @see #getLargeFileTotalBytes()
     */
    boolean isLargeFile();

    @SuppressWarnings("UnusedParameters")
    interface FinishListener {
        /**
         * Will be invoked when the {@code task} is over({@link FileDownloadStatus#isOver(int)}).
         * This method will be invoked in Non-UI-Thread and this thread is controlled by
         * {@link MessageSnapshotThreadPool}.
         *
         * @param task is over, the status would be one of below:
         *             {@link FileDownloadStatus#completed}、{@link FileDownloadStatus#warn}、
         *             {@link FileDownloadStatus#error}、{@link FileDownloadStatus#paused}.
         * @see FileDownloadStatus#isOver(int)
         */
        void over(final BaseDownloadTask task);
    }

    /**
     * The running task.
     * <p>
     * Used in internal.
     */
    interface IRunningTask {
        /**
         * @return The origin one.
         */
        BaseDownloadTask getOrigin();

        /**
         * @return The message handler of this task.
         */
        ITaskHunter.IMessageHandler getMessageHandler();

        /**
         * @return {@code true} the id of the task is equal to the {@code id}.
         */
        boolean is(int id);

        /**
         * @return {@code true} the listener of the task is equal to the {@code listener}.
         */
        boolean is(FileDownloadListener listener);

        /**
         * @return {@code true} if the task has already finished.
         */
        boolean isOver();

        /**
         * @return The attached key, if this task in a queue, the attached key is the hash code of
         * the listener.
         */
        int getAttachKey();

        /**
         * @param key The attached key for this task.
         *            When the task is running, it must attach a key.
         *            if this task is running in a queue downloading tasks serial, the attach key
         *            is equal to the hash code of the callback of queue's handler, otherwise the
         *            attach key is equal to the hash code of the listener.
         */
        void setAttachKey(int key);

        /**
         * @return {@code true} the task has already added to the downloading list.
         */
        boolean isMarkedAdded2List();

        /**
         * Mark the task has already added to the downloading list.
         */
        void markAdded2List();

        /**
         * Free the task.
         */
        void free();
    }

    /**
     * The callback for the life cycle of the task.
     */
    interface LifeCycleCallback {
        /**
         * The task begin working.
         */
        void onBegin();

        /**
         * The task is running, and during the downloading processing, when the status of the task
         * is changed will trigger to callback this method.
         */
        void onIng();

        /**
         * The task is end.
         */
        void onOver();
    }
}
