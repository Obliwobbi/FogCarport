package app.services;

public interface SvgService
{
    void addRectangle(double x, double y, double height, double width, String style);

    void addLine(int x1, int y1, int x2, int y2, String style);

    void addArrow(int x1, int y1, int x2, int y2, String style);

    void addText(int x, int y, int rotation, String text);

    void addSvg(SvgServiceImpl innerSvg);
}
