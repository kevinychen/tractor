package view;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import model.Card;

public class CardImages
{
    private BufferedImage[][] CARD_IMAGES;
    private BufferedImage BIG_JOKER_IMAGE, SMALL_JOKER_IMAGE;
    private BufferedImage CARD_BACK_IMAGE;

    private Map<BufferedImage, Map<Long, BufferedImage>> rotatedImages;

    public void loadImages() throws IOException
    {
        CARD_IMAGES = new BufferedImage[13][4];
        for (int i = 0; i < CARD_IMAGES.length; i++)
            for (int j = 0; j < CARD_IMAGES[i].length; j++)
            {
                String resource = String.format("/images/%c%s.gif",
                        "shdc".charAt(j),
                        "2 3 4 5 6 7 8 9 10 j q k 1".split(" ")[i]);
                CARD_IMAGES[i][j] = getImage(resource);
            }
        BIG_JOKER_IMAGE = getImage("/images/jr.gif");
        SMALL_JOKER_IMAGE = getImage("/images/jb.gif");
        CARD_BACK_IMAGE = getImage("/images/b1fv.gif");

        rotatedImages = new HashMap<BufferedImage, Map<Long, BufferedImage>>();
    }

    private BufferedImage getImage(String path) throws IOException
    {
        return squareImage(ImageIO.read(getClass().getResource(path)));
    }

    /*
     * Rotating images works best when the image is square. This method centers
     * a rectangular image in a larger square. The additional pixels are
     * transparent.
     */
    private BufferedImage squareImage(BufferedImage image)
    {
        int size = (int)Math.ceil(Math.hypot(image.getWidth(), image.getHeight()));
        BufferedImage squareImage = new BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR);
        int dx = (size - image.getWidth()) / 2;
        int dy = (size - image.getHeight()) / 2;
        for (int x = 0; x < size; x++)
            for (int y = 0; y < size; y++)
                squareImage.setRGB(x, y, 0xffffff);
        for (int x = 0; x < image.getWidth(); x++)
            for (int y = 0; y < image.getHeight(); y++)
                squareImage.setRGB(x + dx, y + dy, image.getRGB(x, y));
        return squareImage;
    }

    public BufferedImage getCardImage(Card.VALUE value, Card.SUIT suit)
    {
        return CARD_IMAGES[value.ordinal()][suit.ordinal()];
    }

    public BufferedImage getBigJokerImage()
    {
        return BIG_JOKER_IMAGE;
    }

    public BufferedImage getSmallJokerImage()
    {
        return SMALL_JOKER_IMAGE;
    }

    public BufferedImage getCardBackImage()
    {
        return CARD_BACK_IMAGE;
    }

    public BufferedImage getRotatedImage(BufferedImage image, double dir)
    {
        if (!rotatedImages.containsKey(image))
            rotatedImages.put(image, new HashMap<Long, BufferedImage>());
        Map<Long, BufferedImage> map = rotatedImages.get(image);

        // Approximate the direction by finding closest multiple of 1/1024.
        long hash = Math.round(dir * 1024);
        if (!map.containsKey(hash))
        {
            AffineTransform at = AffineTransform.getRotateInstance(dir,
                    image.getWidth() / 2, image.getHeight() / 2);
            AffineTransformOp op = new AffineTransformOp(at,
                    AffineTransformOp.TYPE_BILINEAR);
            map.put(hash, op.filter(image, null));
        }
        return map.get(hash);
    }
}
