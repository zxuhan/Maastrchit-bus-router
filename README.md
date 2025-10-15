# BCS25-Project-1-2: Phase 2 Public Transport Routing

Group 25 Team Members: Tristan Dormans, Mehmet Levent Koca, Alen Quiroz Engel, Vlad Creciun, Xuhan Zhuang, Joel Kumi, Bruno Torrijo

## IMPORTANT TO BE ABLE TO RUN THE APPLICATION
1. Set up a JavaFX environment on your IDEA to be able to run the application GUI.

2. If you have an error saying "JavaFX runtime components missing", you also need to set up the VM options in your run configurations as:<br>
--module-path "your physical path to JavaFX lib" --add-modules javafx.controls,javafx.fxml

3. Install mySQL in your computer and create a new local user<br>
(1) Run `mysql -u root` in your commandline<br>
(2) When you see a prompt like "mysql>", run following one by one:<br>
    `CREATE USER 'DACS2024'@'localhost' IDENTIFIED BY 'DACS2024';`<br>
    `GRANT ALL PRIVILEGES ON *.* TO 'DACS2024'@'localhost' WITH GRANT OPTION;`<br>
    `FLUSH PRIVILEGES;`<br>

4. Create a new mySQL connection with your local database application(recommending DBeaver).<br>
URL = jdbc:mysql://localhost:3306/gtfs<br>
USER = DACS2024<br>
PASSWORD = DACS2024<br>

5. When you already set up your local database connection, import two data files:<br>
(1)Import maas_stops_time.csv file as Table "maas_stops_time"<br>
(2)Import shapes.csv file as Table "shapes"<br>

***** HOW TO RUN *****

Launch the application by running the MapLauncher.java file. 

Now, there are multiple things you can do:
1) The '+' and '-' buttons
    These buttons are used for zooming the map in and out, if the user desires to have a closer or farther away view of the map, 
    they will be able to control the zoom in amount with these buttons

2) Zip Code text fields
    There are two text fields labeled "Enter Zip Code 1" and "Enter Zip Code 2", these are for the user to insert their desired points 
    in the map. By inserting 2 Zip Codes, the user will be able to clculate the distance between the two points in the map.

4) Information on Screen
    After the 'Search' button is clicked, the user is presented with different pieces of information on screen:
    The visualised bus route, displayed with a red line.
    Route information is displayed on the right of the map image, including departure, walking times, transfer information and total travel time.

5) Accessibility Displayer
    Press the button 'Show accessibility of postal codes' to open up a new window. In this window you will be presented with a map containing colored coded postal codes of Maastricht according to their social-economic accessibility. It is required to press the 'Update' button any time a value is changed in the other fields of this GUI. The options button will open up a menu with different score maps, please press the 'update' button after selecting an option. The other GUI buttons speak for themselves.


6) To exit the program simply press the cross on the top right of the GUI, or alternatively press 'ALT' + 'F4'. 

Thank you,
Group 25.

25st of June, 2024.
