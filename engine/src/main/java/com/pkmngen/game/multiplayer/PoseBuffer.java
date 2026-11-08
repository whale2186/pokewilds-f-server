package com.pkmngen.game.multiplayer;

import java.util.ArrayDeque;

/** Render 100ms behind the wire; monotone cubic tangents prevent corner overshoot. */
public final class PoseBuffer {
    private static final long DELAY = 100_000_000L;
    private static final class Sample {
        final float x, y; final long time;
        Sample(float x, float y, long time) { this.x = x; this.y = y; this.time = time; }
        float axis(boolean horizontal) { return horizontal ? x : y; }
    }
    private final ArrayDeque<Sample> samples = new ArrayDeque<>();
    public void add(float x, float y, long time, boolean reset) {
        if (reset) samples.clear();
        if (!samples.isEmpty() && time <= samples.getLast().time) return;
        samples.addLast(new Sample(x, y, time));
        while (samples.size() > 12) samples.removeFirst();
    }
    public float sample(long now, boolean horizontal) {
        if (samples.isEmpty()) return 0;
        long target = now - DELAY;
        Sample[] points = samples.toArray(new Sample[0]);
        if (target <= points[0].time) return points[0].axis(horizontal);
        for (int i = 1; i < points.length; i++) {
            Sample a = points[i - 1], b = points[i];
            if (target > b.time) continue;
            float seconds = (b.time - a.time) / 1e9f;
            float slope = (b.axis(horizontal) - a.axis(horizontal)) / seconds;
            float v0 = i > 1 ? tangent(points[i - 2], a, b, horizontal) : slope;
            float v1 = i + 1 < points.length ? tangent(a, b, points[i + 1], horizontal) : slope;
            float t = (target - a.time) / (float)(b.time - a.time), t2 = t*t, t3 = t2*t;
            return (2*t3-3*t2+1)*a.axis(horizontal) + (t3-2*t2+t)*seconds*v0
                    + (-2*t3+3*t2)*b.axis(horizontal) + (t3-t2)*seconds*v1;
        }
        // Hold on prolonged packet loss rather than predict through walls.
        return points[points.length - 1].axis(horizontal);
    }
    private static float tangent(Sample a, Sample b, Sample c, boolean horizontal) {
        float first = (b.axis(horizontal)-a.axis(horizontal))/((b.time-a.time)/1e9f);
        float second = (c.axis(horizontal)-b.axis(horizontal))/((c.time-b.time)/1e9f);
        return first*second <= 0 ? 0 : 2*first*second/(first+second);
    }
}
