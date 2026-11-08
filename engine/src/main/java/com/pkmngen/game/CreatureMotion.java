package com.pkmngen.game;

/** Visual-only glide: collision, interactions and serialization retain authoritative positions. */
final class CreatureMotion {
    private float fromX, fromY, toX, toY;
    private long started;
    private static final long DURATION = 250_000_000L;
    CreatureMotion(float x, float y) { fromX = toX = x; fromY = toY = y; }
    void target(float x, float y, long now) {
        fromX = x(now); fromY = y(now); toX = x; toY = y; started = now;
        if ((toX-fromX)*(toX-fromX)+(toY-fromY)*(toY-fromY) > 48*48) { fromX = x; fromY = y; }
    }
    private float fraction(long now) { return Math.max(0, Math.min(1, (now-started)/(float)DURATION)); }
    float x(long now) { return fromX + (toX-fromX)*fraction(now); }
    float y(long now) { return fromY + (toY-fromY)*fraction(now); }
}
