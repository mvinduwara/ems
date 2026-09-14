package com.retailhr.ems.util;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

public class IconFactory {

    private static final double DEFAULT_SIZE = 16;

    private IconFactory() {
    }

    public static Node users(double size) {
        Circle head1 = circle(size * 0.28, size * 0.32, size * 0.14, size);
        Arc body1 = arc(size * 0.28, size * 0.78, size * 0.22, size * 0.18, size);
        Circle head2 = circle(size * 0.66, size * 0.28, size * 0.12, size);
        Arc body2 = arc(size * 0.66, size * 0.72, size * 0.19, size * 0.15, size);
        return group(head1, body1, head2, body2);
    }

    public static Node clock(double size) {
        Circle face = circle(size * 0.5, size * 0.5, size * 0.4, size);
        Line hourHand = line(size * 0.5, size * 0.5, size * 0.5, size * 0.28, size);
        Line minuteHand = line(size * 0.5, size * 0.5, size * 0.68, size * 0.58, size);
        return group(face, hourHand, minuteHand);
    }

    public static Node checkSquare(double size) {
        Rectangle box = square(size * 0.1, size * 0.1, size * 0.8, size);
        Polyline check = polyline(size, size * 0.25, size * 0.5, size * 0.45, size * 0.68, size * 0.78, size * 0.3);
        return group(box, check);
    }

    public static Node check(double size) {
        return group(polyline(size, size * 0.2, size * 0.52, size * 0.42, size * 0.75, size * 0.82, size * 0.25));
    }

    public static Node x(double size) {
        Line l1 = line(size * 0.2, size * 0.2, size * 0.8, size * 0.8, size);
        Line l2 = line(size * 0.8, size * 0.2, size * 0.2, size * 0.8, size);
        return group(l1, l2);
    }

    public static Node xCircle(double size) {
        Circle ring = circle(size * 0.5, size * 0.5, size * 0.4, size);
        Line l1 = line(size * 0.35, size * 0.35, size * 0.65, size * 0.65, size);
        Line l2 = line(size * 0.65, size * 0.35, size * 0.35, size * 0.65, size);
        return group(ring, l1, l2);
    }

    public static Node dollar(double size) {
        Line vertical = line(size * 0.5, size * 0.12, size * 0.5, size * 0.88, size);
        Path s = new Path();
        s.getElements().add(new MoveTo(size * 0.72, size * 0.28));
        s.getElements().add(new CubicCurveTo(size * 0.72, size * 0.18, size * 0.28, size * 0.18, size * 0.28, size * 0.34));
        s.getElements().add(new CubicCurveTo(size * 0.28, size * 0.5, size * 0.72, size * 0.5, size * 0.72, size * 0.66));
        s.getElements().add(new CubicCurveTo(size * 0.72, size * 0.82, size * 0.28, size * 0.82, size * 0.28, size * 0.72));
        strokeOnly(s, size);
        return group(vertical, s);
    }

    public static Node document(double size) {
        Rectangle page = new Rectangle(size * 0.2, size * 0.1, size * 0.6, size * 0.8);
        strokeOnly(page, size);
        Line l1 = line(size * 0.32, size * 0.35, size * 0.68, size * 0.35, size);
        Line l2 = line(size * 0.32, size * 0.5, size * 0.68, size * 0.5, size);
        Line l3 = line(size * 0.32, size * 0.65, size * 0.55, size * 0.65, size);
        return group(page, l1, l2, l3);
    }

    public static Node logout(double size) {
        Rectangle door = new Rectangle(size * 0.15, size * 0.15, size * 0.4, size * 0.7);
        strokeOnly(door, size);
        Line arrow = line(size * 0.45, size * 0.5, size * 0.85, size * 0.5, size);
        Polyline arrowHead = polyline(size, size * 0.68, size * 0.35, size * 0.85, size * 0.5, size * 0.68, size * 0.65);
        return group(door, arrow, arrowHead);
    }

    public static Node plusCircle(double size) {
        Circle ring = circle(size * 0.5, size * 0.5, size * 0.4, size);
        Line l1 = line(size * 0.5, size * 0.3, size * 0.5, size * 0.7, size);
        Line l2 = line(size * 0.3, size * 0.5, size * 0.7, size * 0.5, size);
        return group(ring, l1, l2);
    }

    public static Node refresh(double size) {
        Arc arc1 = arcStroke(size * 0.5, size * 0.5, size * 0.32, size * 0.32, 20, 260, size);
        Polyline head1 = polyline(size, size * 0.78, size * 0.22, size * 0.85, size * 0.35, size * 0.68, size * 0.3);
        Arc arc2 = arcStroke(size * 0.5, size * 0.5, size * 0.32, size * 0.32, 200, 260, size);
        Polyline head2 = polyline(size, size * 0.22, size * 0.78, size * 0.15, size * 0.65, size * 0.32, size * 0.7);
        return group(arc1, head1, arc2, head2);
    }

    public static Node edit(double size) {
        Line body = line(size * 0.2, size * 0.8, size * 0.65, size * 0.35, size);
        Polygon tip = new Polygon(size * 0.65, size * 0.35, size * 0.78, size * 0.22, size * 0.85, size * 0.29, size * 0.72, size * 0.42);
        tip.setFill(Color.web("#666666"));
        Line underline = line(size * 0.15, size * 0.85, size * 0.3, size * 0.85, size);
        return group(body, tip, underline);
    }

    public static Node download(double size) {
        Line vertical = line(size * 0.5, size * 0.15, size * 0.5, size * 0.6, size);
        Polyline arrowHead = polyline(size, size * 0.3, size * 0.4, size * 0.5, size * 0.62, size * 0.7, size * 0.4);
        Line base = line(size * 0.2, size * 0.82, size * 0.8, size * 0.82, size);
        return group(vertical, arrowHead, base);
    }

    public static Node camera(double size) {
        Rectangle body = new Rectangle(size * 0.12, size * 0.3, size * 0.76, size * 0.55);
        body.setArcWidth(size * 0.1);
        body.setArcHeight(size * 0.1);
        strokeOnly(body, size);
        Circle lens = circle(size * 0.5, size * 0.58, size * 0.16, size);
        Rectangle bump = new Rectangle(size * 0.36, size * 0.18, size * 0.28, size * 0.14);
        strokeOnly(bump, size);
        return group(body, lens, bump);
    }

    public static Node cameraOff(double size) {
        Node cam = camera(size);
        Line slash = line(size * 0.1, size * 0.1, size * 0.9, size * 0.9, size);
        return group(cam, slash);
    }

    public static Node calendar(double size) {
        Rectangle body = new Rectangle(size * 0.12, size * 0.2, size * 0.76, size * 0.68);
        strokeOnly(body, size);
        Line divider = line(size * 0.12, size * 0.38, size * 0.88, size * 0.38, size);
        Line hook1 = line(size * 0.3, size * 0.1, size * 0.3, size * 0.28, size);
        Line hook2 = line(size * 0.7, size * 0.1, size * 0.7, size * 0.28, size);
        return group(body, divider, hook1, hook2);
    }

    public static Node grid(double size) {
        Rectangle a = square(size * 0.12, size * 0.12, size * 0.32, size);
        Rectangle b = square(size * 0.56, size * 0.12, size * 0.32, size);
        Rectangle c = square(size * 0.12, size * 0.56, size * 0.32, size);
        Rectangle d = square(size * 0.56, size * 0.56, size * 0.32, size);
        return group(a, b, c, d);
    }

    private static Group group(Node... nodes) {
        Group g = new Group(nodes);
        g.setStyle("-fx-scale-x: 1; -fx-scale-y: 1;");
        return g;
    }

    private static Circle circle(double cx, double cy, double r, double size) {
        Circle c = new Circle(cx, cy, r);
        strokeOnly(c, size);
        return c;
    }

    private static Rectangle square(double x, double y, double side, double size) {
        Rectangle r = new Rectangle(x, y, side, side);
        strokeOnly(r, size);
        return r;
    }

    private static Line line(double x1, double y1, double x2, double y2, double size) {
        Line l = new Line(x1, y1, x2, y2);
        l.setStrokeWidth(Math.max(1.2, size * 0.08));
        l.getStyleClass().add("icon-shape");
        l.setStrokeLineCap(StrokeLineCap.ROUND);
        return l;
    }

    private static Arc arc(double cx, double cy, double rx, double ry, double size) {
        Arc a = new Arc(cx, cy, rx, ry, 0, 180);
        a.setType(ArcType.OPEN);
        strokeOnly(a, size);
        return a;
    }

    private static Arc arcStroke(double cx, double cy, double rx, double ry, double start, double extent, double size) {
        Arc a = new Arc(cx, cy, rx, ry, start, extent);
        a.setType(ArcType.OPEN);
        strokeOnly(a, size);
        return a;
    }

    private static Polyline polyline(double size, double... points) {
        Polyline p = new Polyline(points);
        p.setStrokeWidth(Math.max(1.2, size * 0.08));
        p.getStyleClass().add("icon-shape");
        p.setStrokeLineCap(StrokeLineCap.ROUND);
        p.setStrokeLineJoin(StrokeLineJoin.ROUND);
        return p;
    }

    private static void strokeOnly(Shape shape, double size) {
        shape.setFill(Color.TRANSPARENT);
        shape.setStrokeWidth(Math.max(1.2, size * 0.07));
        shape.getStyleClass().add("icon-shape");
        shape.setStrokeLineCap(StrokeLineCap.ROUND);
    }
}