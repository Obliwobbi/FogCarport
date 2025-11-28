package app.services;

public class CarportTopViewSvg
{
    private int width;
    private int height;
    private SvgService svgService;

    public CarportTopViewSvg(int width, int height)
    {
        this.width = width;
        this.height = height;
        this.svgService = new SvgServiceImpl(0, 0, "0 0 855 690", "100%", "auto");
    }

    private void addTopPlate()
    {

    }

    private void addCeilingJoist()
    {

    }

    private void addPosts()
    {

    }


}
