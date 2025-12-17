package app.services;

import java.util.Locale;

public class SvgServiceImpl implements SvgService
{
    private static final String SVG_TEMPLATE =
            "<svg xmlns=\"http://www.w3.org/2000/svg\"\n" +
                    "         x=\"%d\" y=\"%d\"\n" +
                    "         viewBox=\"%s\" width=\"%s\" height=\"%s\"\n" +
                    "         preserveAspectRatio=\"xMinYMin\">";

    private static final String SVG_RECT_TEMPLATE = "<rect x=\"%f\" y=\"%f\" height=\"%f\" width=\"%f\" style=\"%s\"/>";

    private static final String SVG_LINE_TEMPLATE = "<line x1=\"%f\" y1=\"%f\" x2=\"%f\" y2=\"%f\" style=\"%s\"/>";

    private static final String SVG_ARROW_TEMPLATE = "<line x1=\"%f\" y1=\"%f\" x2=\"%f\" y2=\"%f\" style=\"%s\"/>";

    private static final String SVG_TEXT_TEMPLATE = "<text style=\"text-anchor: middle\" transform=\"translate(%d,%d) rotate(%d)\">%s</text>";

    private static final String SVG_ARROW_DEFS = "   <defs>\n" +
            "        <marker\n" +
            "                id=\"beginArrow\"\n" +
            "                markerWidth=\"12\"\n" +
            "                markerHeight=\"12\"\n" +
            "                refX=\"0\"\n" +
            "                refY=\"6\"\n" +
            "                orient=\"auto\">\n" +
            "            <path d=\"M0,6 L12,0 L12,12 L0,6\" style=\"fill: #000000;\"/>\n" +
            "        </marker>\n" +
            "        <marker\n" +
            "                id=\"endArrow\"\n" +
            "                markerWidth=\"12\"\n" +
            "                markerHeight=\"12\"\n" +
            "                refX=\"12\"\n" +
            "                refY=\"6\"\n" +
            "                orient=\"auto\">\n" +
            "            <path d=\"M0,0 L12,6 L0,12 L0,0 \" style=\"fill: #000000;\"/>\n" +
            "        </marker>\n" +
            "    </defs>";

    private final StringBuilder svg = new StringBuilder();

    public SvgServiceImpl(int x, int y, String viewBox, String width, String height)
    {
        svg.append(String.format(Locale.US, SVG_TEMPLATE, x, y, viewBox, width, height));
        svg.append(SVG_ARROW_DEFS);
    }

    @Override
    public void addRectangle(double x, double y, double height, double width, String style)
    {
        svg.append(String.format(Locale.US, SVG_RECT_TEMPLATE, x, y, height, width, style));
    }

    @Override
    public void addLine(double x1, double y1, double x2, double y2, String style)
    {
        svg.append(String.format(Locale.US, SVG_LINE_TEMPLATE, x1, y1, x2, y2, style));
    }

    @Override
    public void addArrow(double x1, double y1, double x2, double y2, String style)
    {
        svg.append(String.format(Locale.US, SVG_ARROW_TEMPLATE, x1, y1, x2, y2, style));
    }

    @Override
    public void addText(int x, int y, int rotation, String text)
    {
        svg.append(String.format(SVG_TEXT_TEMPLATE, x, y, rotation, text));
    }

    @Override
    public void addSvg(SvgServiceImpl innerSvg)
    {
        svg.append(innerSvg.toString());
    }

    @Override
    public String toString()
    {
        return svg.append("</svg>").toString();
    }
}