package src.java.DisplayerAccessibility;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import src.java.GUI.Data;
import src.java.Singletons.FileManager;

public class AccessibilityDisplayerSwing extends JFrame implements ActionListener{
    private final double MAP_WIDTH = 600;
    private final double MAP_HEIGHT = 600;
    private double centerLatitude = 50.851368;
    private double centerLongitude = 5.690973;
    Map<String, List<List<Double[]>>> polygonsMap = new HashMap<>();
    HashMap<String, Integer> scoresMap = new HashMap<>();
    private int zoomLevel = 13;
    private String zoomedPostcode;
    private JButton updateButton = new JButton("Update");
    private JButton zoomInButton = new JButton("+");
    private JButton zoomOutButton = new JButton("-");
    private JTextField zipCodeField1 = new JTextField();
    private ArrayList<String> zipCodes = Data.getZipCodes();
    private String[] zipCodesAll;
    private ArrayList<Double> lats = Data.getLatitudes();
    private ArrayList<Double> longs = Data.getLongitudes();
    private ArrayList<String> colours = getGradientColors();
    private final String API_KEY = "AIzaSyAZwfzWK71qIgXSleA-02n-oXfo5OjOhhU";
    private JPanel panel = new JPanel();
    private final double offset = 268435456;
    private final double radius = offset / Math.PI;
    private JFrame frame;
    private JLabel scoreField;
    private String URL;
    private int prevX, prevY;

    public AccessibilityDisplayerSwing(boolean start) {
        frame = new JFrame();
        frame.setTitle("Image Display");
        frame.setSize(600, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    
        ImageIcon imageIcon = new ImageIcon("./staticmap.png");
        JLabel label = new JLabel(imageIcon);
        zipCodeField1.setPreferredSize(new Dimension(150, 30));
        TextPrompt textPrompt = new TextPrompt("Enter a zipcode to center", zipCodeField1);
        scoreField = new JLabel();
        frame.add(updateButton);
        frame.add(zoomInButton);
        frame.add(zoomOutButton);
        frame.add(zipCodeField1);
        frame.add(textPrompt);
        frame.add(scoreField);
        frame.add(panel);
        panel.add(label);

        frame.setVisible(true);
    }

    public AccessibilityDisplayerSwing() {
    }

    public void addMouseListenersPanning(JLabel label) {
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                prevX = e.getX();
                prevY = e.getY();
            }
        });

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                prevX = e.getX();
                prevY = e.getY();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    doubleClickZoom(e.getX(), e.getY());
                }
            }
        });

        label.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                double mouseSensitivity= 10.0;
                int deltaX = e.getX() - prevX;
                int deltaY = e.getY() - prevY;
                prevX = e.getX();
                prevY = e.getY();

                double lonChange = (double) deltaX / MAP_WIDTH * 360 / Math.pow(2, zoomLevel) * mouseSensitivity;
                double latChange = (double) deltaY / MAP_HEIGHT * 360 / Math.pow(2, zoomLevel) * mouseSensitivity;
                centerLongitude -= lonChange;
                centerLatitude += latChange;

                requestNewImageIcon();
                try {
                    drawScreen();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            
        });
    }

    public void doubleClickZoom(int x, int y) {
        double lonPerPixel = 360 / (256 * Math.pow(2, zoomLevel));
        double latPerPixel = 360 / (256 * Math.pow(2, zoomLevel));
        
        centerLongitude += (x - MAP_WIDTH / 2) * lonPerPixel;
        centerLatitude -= (y - MAP_HEIGHT / 2) * latPerPixel;
        
        zoomLevel++;
        requestNewImageIcon();
        try {
            drawScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Brain method
    public void drawScreen() throws IOException {
        panel.removeAll();
        URL url = new URL(URL);
        java.awt.Image image = ImageIO.read(url);
        BufferedImage bufferedImage = (BufferedImage) image;
        Color lineColor = new Color(0,0,0, 255);
        bufferedImage.flush();
        Graphics2D g = (Graphics2D) bufferedImage.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION	, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_DITHERING	, RenderingHints.VALUE_DITHER_ENABLE);
        g.setRenderingHint(RenderingHints.KEY_RENDERING	, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING	, RenderingHints.VALUE_ANTIALIAS_ON);




        Stroke originalStroke = g.getStroke();
        g.setStroke(new BasicStroke(1));
        g.setColor(new Color(255,255,255,64));
        g.setStroke(originalStroke);
        if (this.zoomedPostcode != null) this.scoreField.setText("score: " + Integer.toString(this.scoresMap.get(this.zoomedPostcode)));
        ImageIcon imageIcon = new ImageIcon(bufferedImage);
        JLabel label = new JLabel(imageIcon);
        Color polygonColor = null;
        
        double startX = 0;
        double startY = 0;
        ArrayList<Integer> xPoints = new ArrayList<>();
        ArrayList<Integer> yPoints = new ArrayList<>();

        //Change this.zipCodesAll to zipCodes to draw only the zipcodes from MassZipLatLon.xldx
        for (String postCode: this.zipCodesAll) {
            List<List<Double[]>> polygons = polygonsMap.get(postCode);
            //Set polygon color according to postal code score
            if (scoresMap.get(postCode) != null) {
                int score = scoresMap.get(postCode);
                String[] split = colours.get(getColorIndexForScore(score, scoresMap, colours)).split(",");
                // String string = (postCode + " -" + score + " -"+ Integer.parseInt(split[0]) + "," + Integer.parseInt(split[1]));
                // FileManager.getInstance().saveStringToFile(string, "text.txt");
                polygonColor = new Color(Integer.parseInt(split[0]),Integer.parseInt(split[1]),0, 255);
            }

            else {
                polygonColor = new Color(255,255,255, 64);
            }
            g.setColor(lineColor);

            if (polygons == null || polygons.get(0).isEmpty()) {
                //Postcodes with no polygons in db, so we just draw a circle, give it later a relevant color
                //Cover only the Maastricht postcodes
                if(postCode.equals("6211BM") || postCode.equals("6227CC")) {
                    ArrayList<Double> latLong = Data.getLatLong(postCode);
                    int[] xY = adjust(latLong.get(1), latLong.get(0), centerLongitude, centerLatitude, zoomLevel);
                    g.setColor(polygonColor);
                    g.fillOval((int) (xY[0] + MAP_WIDTH / 2 - 5), (int) (xY[1] + MAP_HEIGHT / 2 -5), 10, 10);
                }
                continue;
            }



            //Loop through all the polygons of one postal code
            for(List<Double[]> polygon: polygons) {
                //Draw darker outline on highlighted postal code
                if(this.zoomedPostcode != null && this.zoomedPostcode.equals(postCode)) {
                    g.setStroke(new BasicStroke(2));

                    lineColor = new Color(0,0,0);
                    g.setColor(lineColor);
                }

                else {
                    g.setStroke(new BasicStroke((float) 0.5));
                }
                xPoints.clear();
                yPoints.clear();
                

                //Loop through the coordinate points of postal codes, draw lines through them
                //And at them to xPoints and yPoints to draw the polygon
                for (int i = 0; i < polygon.size(); i++) {
                    Double[] coordinates = polygon.get(i);
                    double lat = coordinates[0];
                    double lon = coordinates[1];
                    
                    int[] xY = adjust(lon, lat, centerLongitude, centerLatitude, zoomLevel);
                    
                    xPoints.add(xY[0]);
                    yPoints.add(xY[1]);
                    
                    if (i > 0) {
                        g.drawLine(
                            (int) (startX + MAP_WIDTH / 2 ),
                            (int) (startY + MAP_HEIGHT / 2 ),
                            (int) (xY[0] + MAP_WIDTH / 2 ),
                            (int) (xY[1] + MAP_HEIGHT / 2 )
                        );
                    }
                    
                    startX = xY[0];
                    startY = xY[1];
                }
            
                xPoints.add(xPoints.get(0));
                yPoints.add(yPoints.get(0));
            
                //https://stackoverflow.com/questions/718554/how-to-convert-an-arraylist-containing-integers-to-primitive-int-array
                int[] xArray = xPoints.stream().mapToInt(i -> i).toArray();
                int[] yArray = yPoints.stream().mapToInt(i -> i).toArray();
            
                g.setColor(polygonColor);
                g.fillPolygon(
                    //https://stackoverflow.com/questions/71495980/java-8-stream-add-1-to-each-element-and-remove-if-element-is-5-in-the-list
                    Arrays.stream(xArray).map(x -> x + (int) MAP_WIDTH / 2 ).toArray(),
                    Arrays.stream(yArray).map(y -> y + (int) MAP_HEIGHT / 2 ).toArray(),
                    xArray.length
                );
            
                xPoints.clear();
                yPoints.clear();
            }
        }

        imageIcon = new ImageIcon(bufferedImage);
        label.setIcon(imageIcon);
        addMouseListenersPanning(label);
        panel.add(label);
        frame.setVisible(true);
    }

    // X,Y ... location in degrees
    // xcenter,ycenter ... center of the map in degrees (same value as in
    // the google static maps URL)
    // zoomlevel (same value as in the google static maps URL)
    // xr, yr and the returned Point ... position of X,Y in pixels relative
    // to the center of the bitmap

    // https://stackoverflow.com/questions/23898964/getting-pixel-coordinated-from-google-static-maps
    public int[] adjust(double x, double y, double xcenter, double ycenter, int zoomlevel) {
        int xr = (lToX(x) - lToX(xcenter)) >> (21 - zoomlevel);
        int yr = (lToY(y) - lToY(ycenter)) >> (21 - zoomlevel);
        return new int[] { xr, yr };
    }

    public int lToX(double x) {
        return (int) (Math.round(offset + radius * x * Math.PI / 180));
    }

    public int lToY(double y) {
        return (int) (Math.round(
                offset - radius * Math.log((1 + Math.sin(y * Math.PI / 180)) / (1 - Math.sin(y * Math.PI / 180))) / 2));
    }

    public static void main(String[] args) {
        AccessibilityDisplayerSwing displayer = new AccessibilityDisplayerSwing();
        displayer.runAccessibilityDisplayer();
    }

    public int getColorIndexForScore(int score, HashMap<String, Integer> scoreMap, ArrayList<String> colors) {
        int maxScore = Integer.MIN_VALUE;
        int minScore = Integer.MAX_VALUE;

        
        for (int value : scoreMap.values()) {
            if (value > maxScore) {
                maxScore = value;
            }
            if (value < minScore) {
                minScore = value;
            }
        }

        //https://en.wikipedia.org/wiki/Normalization_(statistics)
        int colorIndex = (int) ((double) (score - minScore) / (maxScore - minScore) * (colors.size() - 1));
        return colorIndex;
    }

    public ArrayList<String> getGradientColors() {
            ArrayList<String> colors = new ArrayList<>();
            int steps = 256;
    
            for (int i = 0; i < steps; i++) {
                int r = 255;
                int g = i;
                int b = 0;
                colors.add(r + "," + g + "," + b);
            }
    
            // Yellow to Green
            for (int i = 0; i < steps; i++) {
                int r = 255 - i;
                int g = 255;
                int b = 0;
                colors.add(r + "," + g + "," + b);
            }
    
            return colors;
        }

    public String requestNewImageIcon() {
        StringBuilder mapUrlBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/staticmap");
        mapUrlBuilder.append("?center=").append(centerLatitude).append(",").append(centerLongitude);
        mapUrlBuilder.append("&zoom=").append(zoomLevel);
        mapUrlBuilder.append("&size=600x600");
        mapUrlBuilder.append("&scale=").append(1);
        mapUrlBuilder.append("&key=").append(API_KEY);
        URL = mapUrlBuilder.toString();
        return mapUrlBuilder.toString();
    }

    public void centerToZipCode(AccessibilityDisplayerSwing displayer) {
        String zipCode = displayer.zipCodeField1.getText();
        displayer.zoomedPostcode = zipCode;
        ArrayList<Double> latLong = new ArrayList<>();
        if (zipCode.length() == 0) {
            JOptionPane.showMessageDialog(null, "No zipcode has been entered", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            latLong = Data.getLatLong(zipCode);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "No coordinates could be found for zipcode", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        displayer.zoomLevel = 18;
        if (latLong == null) {
            JOptionPane.showMessageDialog(null, "No coordinates could be found for zipcode", "Error", JOptionPane.ERROR_MESSAGE);
        }
        displayer.centerLongitude = latLong.get(1);
        displayer.centerLatitude = latLong.get(0);

    }

    public void createActionListeners(AccessibilityDisplayerSwing displayer) {
        displayer.zoomInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                displayer.zoomLevel++;
                displayer.requestNewImageIcon();
                try {
                    displayer.drawScreen();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        displayer.updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                centerToZipCode(displayer);
                displayer.requestNewImageIcon();
                try {
                    displayer.drawScreen();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        displayer.zoomOutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                displayer.zoomLevel--;
                displayer.requestNewImageIcon();
                try {
                    displayer.drawScreen();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    public void runAccessibilityDisplayer() {
        Data.getData();
        AccessibilityDisplayerSwing displayer = new AccessibilityDisplayerSwing(true);
        try {
            displayer.polygonsMap = (Map<String, List<List<Double[]>>>) FileManager.getInstance().getObject("polygonsMap.ser");
            displayer.scoresMap = (HashMap<String, Integer>) FileManager.getInstance().getObject("scores.ser");
            displayer.zipCodesAll = extractKeys(displayer.polygonsMap);
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        displayer.URL = requestNewImageIcon();
        createActionListeners(displayer);
        try {
            displayer.drawScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //https://stackoverflow.com/questions/16203880/get-array-of-maps-keys
    public static String[] extractKeys(Map<String, List<List<Double[]>>> polygonsMap) {
        Set<String> keys = polygonsMap.keySet();
        return keys.toArray(new String[keys.size()]);
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        //Is empty to be able to compile. Java doesn't see nested actionPerformed functions for some reason
    }
}