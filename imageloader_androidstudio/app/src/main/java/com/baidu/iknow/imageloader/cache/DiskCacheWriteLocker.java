package com.baidu.iknow.imageloader.cache;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.annotation.SuppressLint;

/**
 * 读写锁
 * @author zhaoxuyang
 * @since 2015-10-12
 */
@SuppressLint("NewApi")
final class DiskCacheWriteLocker {
    private final Map<String, WriteLock> locks = new HashMap<String, WriteLock>();
    private final WriteLockPool writeLockPool = new WriteLockPool();

    void acquire(String key) {
        WriteLock writeLock;
        synchronized (this) {
            writeLock = locks.get(key);
            if (writeLock == null) {
                writeLock = writeLockPool.obtain();
                locks.put(key, writeLock);
            }
            writeLock.interestedThreads++;
        }

        writeLock.lock.lock();
    }

    void release(String key) {
        WriteLock writeLock;
        synchronized (this) {
            writeLock = locks.get(key);
            if (writeLock == null) {
                return;
            }
            if (writeLock.interestedThreads < 1) {
                throw new IllegalStateException("Cannot release a lock that is not held" + ", key: " + key
                        + ", interestedThreads: " + writeLock.interestedThreads);
            }

            writeLock.interestedThreads--;
            if (writeLock.interestedThreads == 0) {
                WriteLock removed = locks.remove(key);
                if (!removed.equals(writeLock)) {
                    throw new IllegalStateException("Removed the wrong lock" + ", expected to remove: " + writeLock
                            + ", but actually removed: " + removed + ", key: " + key);
                }
                writeLockPool.offer(removed);
            }
        }

        writeLock.lock.unlock();
    }

    private static class WriteLock {
        final Lock lock = new ReentrantLock();
        int interestedThreads;
    }

    private static class WriteLockPool {
        private static final int MAX_POOL_SIZE = 10;
        private final Queue<WriteLock> pool = new ArrayDeque<WriteLock>();

        WriteLock obtain() {
            WriteLock result;
            synchronized (pool) {
                result = pool.poll();
            }
            if (result == null) {
                result = new WriteLock();
            }
            return result;
        }

        void offer(WriteLock writeLock) {
            synchronized (pool) {
                if (pool.size() < MAX_POOL_SIZE) {
                    pool.offer(writeLock);
                }
            }
        }
    }
}
