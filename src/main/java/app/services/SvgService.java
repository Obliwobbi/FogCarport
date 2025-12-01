package app.services;

public interface SvgService
{
    void addRectangle(double x, double y, double height, double width, String style);

    void addLine(double x1, double y1, double x2, double y2, String style);

    void addArrow(double x1, double y1, double x2, double y2, String style);

    void addText(int x, int y, int rotation, String text);

    void addSvg(SvgServiceImpl innerSvg);
}
