package de.shellfire.vpn.gui.helper;

import javafx.scene.input.MouseEvent;

public final class VelocityTracker {

    private static final boolean DEBUG = false;
    private static final boolean localLOGV = DEBUG;

    private static final int NUM_PAST = 10;
    private static final int MAX_AGE_MILLISECONDS = 200;

    private static final int POINTER_POOL_CAPACITY = 20;

    private static Pointer sRecycledPointerListHead;
    private static int sRecycledPointerCount;

    private static final class Pointer {

        public Pointer next;

        public int id;
        public float xVelocity;
        public float yVelocity;

        public final float[] pastX = new float[NUM_PAST];
        public final float[] pastY = new float[NUM_PAST];
        public final long[] pastTime = new long[NUM_PAST]; // uses Long.MIN_VALUE as a sentinel

    }

    private Pointer mPointerListHead; // sorted by id in increasing order
    private int mLastTouchIndex;

    public VelocityTracker() {
        clear();
    }

    public void clear() {
        releasePointerList(mPointerListHead);

        mPointerListHead = null;
        mLastTouchIndex = 0;
    }

    public void addMovement(MouseEvent ev) {
        final int historySize = 0;
        final int lastTouchIndex = mLastTouchIndex;
        final int nextTouchIndex = (lastTouchIndex + 1) % NUM_PAST;
        final int finalTouchIndex = (nextTouchIndex + historySize) % NUM_PAST;

        mLastTouchIndex = finalTouchIndex;

        if (mPointerListHead == null) {
            mPointerListHead = obtainPointer();
        }

        final float[] pastX = mPointerListHead.pastX;
        final float[] pastY = mPointerListHead.pastY;
        final long[] pastTime = mPointerListHead.pastTime;

        pastX[finalTouchIndex] = (float) ev.getX();
        pastY[finalTouchIndex] = (float) ev.getY();
        pastTime[finalTouchIndex] = System.currentTimeMillis();

    }

    public void computeCurrentVelocity(int units) {
        computeCurrentVelocity(units, Float.MAX_VALUE);
    }

    public void computeCurrentVelocity(int units, float maxVelocity) {
        final int lastTouchIndex = mLastTouchIndex;

        for (Pointer pointer = mPointerListHead; pointer != null; pointer = pointer.next) {
            final long[] pastTime = pointer.pastTime;
            int oldestTouchIndex = lastTouchIndex;
            int numTouches = 1;
            final long minTime = pastTime[lastTouchIndex] - MAX_AGE_MILLISECONDS;
            while (numTouches < NUM_PAST) {
                final int nextOldestTouchIndex = (oldestTouchIndex + NUM_PAST - 1) % NUM_PAST;
                final long nextOldestTime = pastTime[nextOldestTouchIndex];
                if (nextOldestTime < minTime) { // also handles end of trace sentinel
                    break;
                }
                oldestTouchIndex = nextOldestTouchIndex;
                numTouches += 1;
            }
            if (numTouches > 3) {
                numTouches -= 1;
            }
            final float[] pastX = pointer.pastX;
            final float[] pastY = pointer.pastY;

            final float oldestX = pastX[oldestTouchIndex];
            final float oldestY = pastY[oldestTouchIndex];
            final long oldestTime = pastTime[oldestTouchIndex];

            float accumX = 0;
            float accumY = 0;

            for (int i = 1; i < numTouches; i++) {
                final int touchIndex = (oldestTouchIndex + i) % NUM_PAST;
                final int duration = (int) (pastTime[touchIndex] - oldestTime);

                if (duration == 0) {
                    continue;
                }

                float delta = pastX[touchIndex] - oldestX;
                float velocity = (delta / duration) * units; // pixels/frame.
                accumX = (accumX == 0) ? velocity : (accumX + velocity) * .5f;

                delta = pastY[touchIndex] - oldestY;
                velocity = (delta / duration) * units; // pixels/frame.
                accumY = (accumY == 0) ? velocity : (accumY + velocity) * .5f;
            }

            if (accumX < -maxVelocity) {
                accumX = -maxVelocity;
            } else if (accumX > maxVelocity) {
                accumX = maxVelocity;
            }

            if (accumY < -maxVelocity) {
                accumY = -maxVelocity;
            } else if (accumY > maxVelocity) {
                accumY = maxVelocity;
            }

            pointer.xVelocity = accumX;
            pointer.yVelocity = accumY;
        }
    }

    public float getXVelocity() {
        Pointer pointer = getPointer(0);
        return pointer != null ? pointer.xVelocity : 0;
    }

    public float getYVelocity() {
        Pointer pointer = getPointer(0);
        return pointer != null ? pointer.yVelocity : 0;
    }

    public float getXVelocity(int id) {
        Pointer pointer = getPointer(id);
        return pointer != null ? pointer.xVelocity : 0;
    }

    public float getYVelocity(int id) {
        Pointer pointer = getPointer(id);
        return pointer != null ? pointer.yVelocity : 0;
    }

    private Pointer getPointer(int id) {
        for (Pointer pointer = mPointerListHead; pointer != null; pointer = pointer.next) {
            if (pointer.id == id) {
                return pointer;
            }
        }
        return null;
    }

    private static Pointer obtainPointer() {
        if (sRecycledPointerCount != 0) {
            Pointer element = sRecycledPointerListHead;
            sRecycledPointerCount -= 1;
            sRecycledPointerListHead = element.next;
            element.next = null;
            return element;
        }
        return new Pointer();
    }

    private static void releasePointer(Pointer pointer) {
        if (sRecycledPointerCount < POINTER_POOL_CAPACITY) {
            pointer.next = sRecycledPointerListHead;
            sRecycledPointerCount += 1;
            sRecycledPointerListHead = pointer;
        }
    }

    private static void releasePointerList(Pointer pointer) {
        if (pointer != null) {
            int count = sRecycledPointerCount;
            if (count >= POINTER_POOL_CAPACITY) {
                return;
            }

            Pointer tail = pointer;
            for (;;) {
                count += 1;
                if (count >= POINTER_POOL_CAPACITY) {
                    break;
                }

                Pointer next = tail.next;
                if (next == null) {
                    break;
                }
                tail = next;
            }

            tail.next = sRecycledPointerListHead;
            sRecycledPointerCount = count;
            sRecycledPointerListHead = pointer;
        }
    }
}