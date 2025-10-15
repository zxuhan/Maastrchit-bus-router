package src.java.DisplayerAccessibility;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import src.java.GUI.Data;
import src.java.Singletons.ExceptionManager;
import src.java.Singletons.FileManager;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class AccessibilityDisplayerJavaFx extends Application {
    private final double MAP_WIDTH = 600;
    private final double MAP_HEIGHT = 600;
    private double centerLatitude = 50.851368;
    private double centerLongitude = 5.690973;
    Map<String, List<List<Double[]>>> polygonsMap = new HashMap<>();
    HashMap<String, Integer> scoresMap = new HashMap<>();
    private int zoomLevel = 13;
    private String zoomedPostcode;
    private Button updateButton = new Button("Update");
    private Button zoomInButton = new Button("+");
    private Button zoomOutButton = new Button("-");
    private CheckBox checkBox = new CheckBox("Disable map");
    private TextField zipCodeField1 = new TextField();
    private TextField transparencyField = new TextField();
    private ArrayList<String> zipCodes = Data.getZipCodes();
    private String[] zipCodesAll;
    private ArrayList<Double> lats = Data.getLatitudes();
    private ArrayList<Double> longs = Data.getLongitudes();
    private ArrayList<String> colours = getGradientColors();
    private final String API_KEY = "AIzaSyAZwfzWK71qIgXSleA-02n-oXfo5OjOhhU";
    private Canvas canvas = new Canvas(MAP_WIDTH, MAP_HEIGHT);
    private ImageView mapView = new ImageView();
    private Label scoreField = new Label();
    private String URL;
    private double prevX, prevY;
    private final double offset = 268435456;
    private final double radius = offset / Math.PI;
    private int selectedOption = 1; 
    private RadioMenuItem option1 = new RadioMenuItem("Standard Official Xlsx");
    private RadioMenuItem option2 = new RadioMenuItem("Custom 100m");
    private RadioMenuItem option3 = new RadioMenuItem("Custom 500m");
    private RadioMenuItem option4 = new RadioMenuItem("Official Xlsx 1000m");
    private RadioMenuItem option5 = new RadioMenuItem("Custom 1000m");
    private RadioMenuItem option6 = new RadioMenuItem("");


    public static void main(String[] args) {
        launch(args);
    }

    public int getSelectedOption() {
        if (option1.isSelected()) return 1;
        if (option2.isSelected()) return 2;
        if (option3.isSelected()) return 3;
        if (option4.isSelected()) return 4;
        if (option5.isSelected()) return 5;

        return 1;
    }

    public String getScorePath(int option) {
        if (option == 1) return "scores.ser";
        if (option == 2) return "scores100m.ser";
        if (option == 3) return "scores500m.ser";
        if (option == 4) return "scores1000official.ser";
        if (option == 5) return "scores1000custom.ser";
        return "scores.ser";
    }


    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Image Display");
        HBox root = new HBox(10);
        root.setPadding(new Insets(10));
        zipCodeField1.setPromptText("Enter a zipcode to center");

        root.getChildren().addAll(updateButton, zoomInButton, zoomOutButton, zipCodeField1, scoreField, mapView);

        createActionListeners();
        primaryStage.setScene(new Scene(root, 700, 700));
        primaryStage.show();

        runAccessibilityDisplayer();
    }

    private void createActionListeners() {
        zoomInButton.setOnAction(e -> {
            zoomLevel++;
            requestNewImageIcon();
            try {
                drawScreen();
            } catch (IOException m) {
                m.printStackTrace();
            };
        });

        zoomOutButton.setOnAction(e -> {
            zoomLevel--;
            requestNewImageIcon();
            try {
                drawScreen();
            } catch (IOException m) {
                m.printStackTrace();
            }
        });

        updateButton.setOnAction(e -> {
            centerToZipCode();
            requestNewImageIcon();
            try {
                drawScreen();
            } catch (IOException m) {
                m.printStackTrace();
            }
        });

        canvas.setOnMousePressed(e -> {
            prevX = e.getX();
            prevY = e.getY();
        });

        canvas.setOnMouseDragged(e -> {
            double mouseSensitivity = 30.0;
            double deltaX = e.getX() - prevX;
            double deltaY = e.getY() - prevY;
            prevX = e.getX();
            prevY = e.getY();

            double lonChange = deltaX / MAP_WIDTH * 360 / Math.pow(2, zoomLevel) * mouseSensitivity;
            double latChange = deltaY / MAP_HEIGHT * 360 / Math.pow(2, zoomLevel) * mouseSensitivity;
            centerLongitude -= lonChange;
            centerLatitude += latChange;

            requestNewImageIcon();
            try {
                drawScreen();
            } catch (IOException m) {
                m.printStackTrace();
            }
        });

        canvas.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                doubleClickZoom(e.getX(), e.getY());
            }
        });
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
    public void runAccessibilityDisplayer() {
        Platform.runLater(() -> {
            Stage primaryStage = new Stage();
            primaryStage.setTitle("Accessibility Displayer");
            VBox root = new VBox();

            HBox box = new HBox(10);
            box.setPadding(new Insets(10));
            
            updateButton = new Button("Update");
            zoomInButton = new Button("Zoom In");
            zoomOutButton = new Button("Zoom Out");
            zipCodeField1 = new TextField();
            zipCodeField1.setPrefWidth(190);
            zipCodeField1.setPromptText("Enter a zipcode to center");
            transparencyField.setPromptText("Enter value for transparency from 0.0 to 1.0");
            transparencyField.setPrefWidth(310);
            scoreField = new Label();
            
        MenuBar menuBar = new MenuBar();

        Menu optionsMenu = new Menu("Options");

        ToggleGroup toggleGroup = new ToggleGroup();
        option1.setToggleGroup(toggleGroup);
        option3.setSelected(true);
        option2.setToggleGroup(toggleGroup);
        option3.setToggleGroup(toggleGroup);
        option4.setToggleGroup(toggleGroup);
        option5.setToggleGroup(toggleGroup);
        option6.setToggleGroup(toggleGroup);



        optionsMenu.getItems().addAll(option1, option2, option3, option4, option5, option6);

        menuBar.getMenus().add(optionsMenu);
        box.getChildren().addAll(updateButton, zoomInButton, zoomOutButton, zipCodeField1, scoreField, checkBox, transparencyField, menuBar);

        canvas = new Canvas(MAP_WIDTH, MAP_HEIGHT);
        createActionListeners();
        root.getChildren().addAll(box, canvas);
            Scene scene = new Scene(root, 1100, 650);
            primaryStage.setScene(scene);
            primaryStage.show();
            try {
                drawScreen();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        Data.getData();
        try {
            polygonsMap = (Map<String, List<List<Double[]>>>) FileManager.getInstance().getObject("polygonsMap.ser");
            scoresMap = (HashMap<String, Integer>) FileManager.getInstance().getObject("scores.ser");
            zipCodesAll = extractKeys(polygonsMap);
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        requestNewImageIcon();
        try {
            drawScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void drawScreen() throws IOException {
        if (selectedOption != getSelectedOption()) {
            selectedOption = getSelectedOption();
            String scorePath = getScorePath(selectedOption);
            try {
                scoresMap = (HashMap<String, Integer>) FileManager.getInstance().getObject(scorePath);
            } catch (ClassNotFoundException | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        // Load the background image
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double transparencyValue = 0.5;
        try {
            if (transparencyField.getText() != null && transparencyField.getText().length() > 0) {
                transparencyValue = Double.parseDouble(transparencyField.getText());
            }
            } catch (NumberFormatException e) {
                ExceptionManager.showInstantError("Invalid Format", "Problem", "Please enter valid number format in transparency field", AlertType.ERROR);
            }
    
        if (checkBox.isSelected()) {
            gc.clearRect(0, 0, MAP_WIDTH, MAP_HEIGHT);
        }
        else {
            URL url = new URL(URL);
            Image image = new Image(url.toString());
            gc.drawImage(image, 0, 0);
        }

        // Draw the polygons
        Color lineColor = Color.BLACK;
        double startX = 0;
        double startY = 0;
        List<Double> xPoints = new ArrayList<>();
        List<Double> yPoints = new ArrayList<>();

        for (String postCode : polygonsMap.keySet()) {
            List<List<Double[]>> polygons = polygonsMap.get(postCode);
            Color polygonColor;
            String[] split = null;
            //Set polygon color according to postal code score
            if (scoresMap.get(postCode) != null) {
                int score = scoresMap.get(postCode);
                split = colours.get(getColorIndexForScore(score, scoresMap, colours)).split(",");
                if (postCode.equals(zoomedPostcode)) {
                    scoreField.setText("score: " + score);
                    // scoreField.setText("score: " +score + " " + Integer.parseInt(split[0]) + " " + Integer.parseInt(split[1]));
                }
                polygonColor = Color.rgb(Integer.parseInt(split[0]), Integer.parseInt(split[1]), 0, transparencyValue);
            } else {
                polygonColor = Color.rgb(255, 255, 255, 0.64);
            }

            gc.setStroke(lineColor);
            gc.setLineWidth(this.zoomedPostcode != null && this.zoomedPostcode.equals(postCode) ? 2 : 0.5);

            if (polygons == null || polygons.get(0).isEmpty()) {
                if(postCode.equals("6211BM") || postCode.equals("6227CC")) {
                    ArrayList<Double> latLong = Data.getLatLong(postCode);
                    int[] xY = adjust(latLong.get(1), latLong.get(0), centerLongitude, centerLatitude, zoomLevel);
                    gc.setFill(Color.rgb(Integer.parseInt(split[0]), Integer.parseInt(split[1]), 0, transparencyValue));
                    if (!(zoomedPostcode == null)) {
                        if (zoomedPostcode.equals("6211BM") || zoomedPostcode.equals("6227CC")) {
                            gc.strokeOval((int) (xY[0] + MAP_WIDTH / 2 - 5), (int) (xY[1] + MAP_HEIGHT / 2 - 5), 10, 10);
                        }
                    }
                    gc.fillOval((int) (xY[0] + MAP_WIDTH / 2 - 5), (int) (xY[1] + MAP_HEIGHT / 2 -5), 10, 10);

                }
                continue;
            }

            //Draw darker outline on highlighted postal code


            //Loop through all the polygons of one postal code
            //Loop through the coordinate points of postal codes, draw lines through them
            //And at them to xPoints and yPoints to draw the polygon
            for (List<Double[]> polygon : polygons) {
                xPoints.clear();
                yPoints.clear();

                for (int i = 0; i < polygon.size(); i++) {
                    Double[] coordinates = polygon.get(i);
                    double lat = coordinates[0];
                    double lon = coordinates[1];

                    int[] xY = adjust(lon, lat, centerLongitude, centerLatitude, zoomLevel);

                    xPoints.add(xY[0] + MAP_WIDTH / 2);
                    yPoints.add(xY[1] + MAP_HEIGHT / 2);

                    if (i > 0) {
                        gc.strokeLine(startX + MAP_WIDTH / 2, startY + MAP_HEIGHT / 2, xY[0] + MAP_WIDTH / 2, xY[1] + MAP_HEIGHT / 2);
                    }

                    startX = xY[0];
                    startY = xY[1];
                }

                // xPoints.add(xPoints.get(0));
                // yPoints.add(yPoints.get(0));
                
                //https://stackoverflow.com/questions/71495980/java-8-stream-add-1-to-each-element-and-remove-if-element-is-5-in-the-list
                //https://stackoverflow.com/questions/718554/how-to-convert-an-arraylist-containing-integers-to-primitive-int-array
                double[] xArray = xPoints.stream().mapToDouble(Double::doubleValue).toArray();
                double[] yArray = yPoints.stream().mapToDouble(Double::doubleValue).toArray();

                gc.setFill(polygonColor);
                gc.fillPolygon(xArray, yArray, xArray.length);
            }
        }
        Image canvasImage = canvas.snapshot(null, null);
        mapView.setImage(canvasImage);
    }

        // X,Y ... location in degrees
    // xcenter,ycenter ... center of the map in degrees (same value as in
    // the google static maps URL)
    // zoomlevel (same value as in the google static maps URL)
    // xr, yr and the returned Point ... position of X,Y in pixels relative
    // to the center of the bitmap

    // https://stackoverflow.com/questions/23898964/getting-pixel-coordinated-from-google-static-maps

    private int[] adjust(double x, double y, double xcenter, double ycenter, int zoomlevel) {
        int xr = (lToX(x) - lToX(xcenter)) >> (21 - zoomlevel);
        int yr = (lToY(y) - lToY(ycenter)) >> (21 - zoomlevel);
        return new int[]{xr, yr};
    }

    private int lToX(double x) {
        return (int) (Math.round(offset + radius * x * Math.PI / 180));
    }

    private int lToY(double y) {
        return (int) (Math.round(offset - radius * Math.log((1 + Math.sin(y * Math.PI / 180)) / (1 - Math.sin(y * Math.PI / 180))) / 2));
    }

    private void doubleClickZoom(double x, double y) {
        double lonPerPixel = 360 / (256 * Math.pow(2, zoomLevel));
        double latPerPixel = 360 / (256 * Math.pow(2, zoomLevel));

        this.centerLongitude += (x - MAP_WIDTH / 2) * lonPerPixel;
        this.centerLatitude -= (y - MAP_HEIGHT / 2) * latPerPixel;

        zoomLevel++;
        requestNewImageIcon();
        try {
            drawScreen();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void centerToZipCode() {
        String zipCode = zipCodeField1.getText();
        zoomedPostcode = zipCode;
        ArrayList<Double> latLong;
        if (zipCode.isEmpty()) {
            return;
        }
        try {
            latLong = Data.getLatLong(zipCode);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        zoomLevel = 18;
        if (latLong == null) {
            return;
        }
        centerLongitude = latLong.get(1);
        centerLatitude = latLong.get(0);
    }

    private String requestNewImageIcon() {
        StringBuilder mapUrlBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/staticmap");
        mapUrlBuilder.append("?center=").append(centerLatitude).append(",").append(centerLongitude);
        mapUrlBuilder.append("&zoom=").append(zoomLevel);
        mapUrlBuilder.append("&size=600x600");
        mapUrlBuilder.append("&scale=").append(1);
        mapUrlBuilder.append("&key=").append(API_KEY);
        URL = mapUrlBuilder.toString();
        return URL;
    }

    private ArrayList<String> getGradientColors() {
        ArrayList<String> colors = new ArrayList<>();
        int steps = 256;

        for (int i = 0; i < steps; i++) {
            int r = 255;
            int g = i;
            int b = 0;
            colors.add(r + "," + g + "," + b);
        }

        for (int i = 0; i < steps; i++) {
            int r = 255 - i;
            int g = 255;
            int b = i;
            colors.add(r + "," + g + "," + b);
        }

        return colors;
    }


    //https://stackoverflow.com/questions/16203880/get-array-of-maps-keys
    private String[] extractKeys(Map<String, List<List<Double[]>>> polygonsMap) {
        Set<String> keys = polygonsMap.keySet();
        return keys.toArray(new String[0]);
    }
}
