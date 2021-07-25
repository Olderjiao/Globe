package com.guanwei.globe.data;

public class PoiEntity {
    private double x;
    private double y;

    public PoiEntity(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "PoiEntity{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
